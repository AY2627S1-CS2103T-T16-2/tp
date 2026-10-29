package seedu.tuitionbook.ui;

import static seedu.tuitionbook.model.Model.SHOW_ALL_PERSONS_REFRESH_COMMAND;

import java.awt.Point;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.tuitionbook.commons.core.GuiSettings;
import seedu.tuitionbook.commons.core.LogsCenter;
import seedu.tuitionbook.logic.Logic;
import seedu.tuitionbook.logic.commands.CommandResult;
import seedu.tuitionbook.logic.commands.exceptions.CommandException;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;
import seedu.tuitionbook.model.PersonListViewState;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";
    private static final KeyCombination HELP_KEY = new KeyCodeCombination(KeyCode.F1);
    private static final double QUERY_TOOLTIP_MAX_WIDTH = 600;

    private final Logger logger = LogsCenter.getLogger(getClass());
    private final Tooltip contactViewQueryTooltip = new Tooltip();

    private Stage primaryStage;
    private Logic logic;
    private Path dataFilePath;

    // Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;
    private DetailDisplay detailDisplay;
    private ResultDisplay resultDisplay;
    private HelpWindow helpWindow;
    private CommandBox commandBox;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    @FXML
    private Label contactViewQuery;

    @FXML
    private Label contactViewCounts;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage}, {@code Logic},
     * and the data file path to show in the status bar.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.dataFilePath = dataFilePath;

        contactViewQueryTooltip.setWrapText(true);
        contactViewQueryTooltip.setMaxWidth(QUERY_TOOLTIP_MAX_WIDTH);
        contactViewQuery.setTooltip(contactViewQueryTooltip);

        // Configure the UI
        restoreWindowBounds(logic.getGuiSettings());

        setAccelerators();

        helpWindow = new HelpWindow();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in a later version of the SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField and
         * ResultDisplay contains a TextArea, some accelerators (e.g., F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we capture F1 at the window level so the shortcut works even when
         * a text input has focus. This also keeps shortcut handling independent from
         * the controls displayed in the menu bar.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (HELP_KEY.match(event)) {
                handleHelp();
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        personListPanel = new PersonListPanel(
                logic.getFilteredPersonList(), logic.getPersonList(), logic::getPersonListViewState);
        personListPanelPlaceholder.getChildren().add(personListPanel.getRoot());

        detailDisplay = new DetailDisplay();
        personListPanelPlaceholder.getChildren().add(detailDisplay.getRoot());
        showPersonList();

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(dataFilePath);
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
        updateContactViewStatus();
    }

    /** Describes the current working set without counting retained rows as query matches. */
    private void updateContactViewStatus() {
        PersonListViewState view = logic.getPersonListViewState();
        boolean filtered = !SHOW_ALL_PERSONS_REFRESH_COMMAND.equals(view.refreshCommand());
        contactViewQuery.setVisible(filtered);
        contactViewQuery.setManaged(filtered);
        contactViewQuery.setText("Query: " + view.refreshCommand());
        contactViewQueryTooltip.setText(contactViewQuery.getText());

        int total = logic.getPersonList().size();
        if (filtered) {
            int shown = logic.getFilteredPersonList().size();
            long matching = logic.getFilteredPersonList().stream().filter(view.predicate()).count();
            contactViewCounts.setText("Shown: " + shown + " of " + total + " contacts · Matching: " + matching
                    + " · Retained: " + (shown - matching));
        } else {
            contactViewCounts.setText("All contacts: " + total);
        }
    }

    /** Gives keyboard focus to the command input after startup has completed. */
    void focusCommandBox() {
        commandBox.requestFocus();
    }

    /**
     * Restores the saved size and position, fitting the window within the current screens when necessary.
     */
    private void restoreWindowBounds(GuiSettings guiSettings) {
        List<Rectangle2D> screenBounds = Screen.getScreens().stream().map(Screen::getVisualBounds).toList();
        restoreWindowBounds(guiSettings, screenBounds, Screen.getPrimary().getVisualBounds());
    }

    /** Restores saved bounds using the supplied screens, which permits deterministic multi-screen testing. */
    void restoreWindowBounds(GuiSettings guiSettings, List<Rectangle2D> screenBounds,
            Rectangle2D primaryScreenBounds) {
        Point savedPosition = guiSettings.getWindowCoordinates();
        if (savedPosition == null) {
            primaryStage.setHeight(Math.min(Math.max(primaryStage.getMinHeight(), guiSettings.getWindowHeight()),
                    primaryScreenBounds.getHeight()));
            primaryStage.setWidth(Math.min(Math.max(primaryStage.getMinWidth(), guiSettings.getWindowWidth()),
                    primaryScreenBounds.getWidth()));
            return;
        }

        Rectangle2D savedBounds = new Rectangle2D(savedPosition.getX(), savedPosition.getY(),
                Math.max(primaryStage.getMinWidth(), guiSettings.getWindowWidth()),
                Math.max(primaryStage.getMinHeight(), guiSettings.getWindowHeight()));
        Rectangle2D targetScreen = screenBounds.stream()
                .filter(screen -> screen.contains(savedBounds.getMinX(), savedBounds.getMinY()))
                .findFirst()
                .or(() -> screenBounds.stream().filter(screen -> screen.intersects(savedBounds)).findFirst())
                .orElse(primaryScreenBounds);
        Rectangle2D restoredBounds = fitWindowWithinScreen(savedBounds, targetScreen);
        primaryStage.setWidth(restoredBounds.getWidth());
        primaryStage.setHeight(restoredBounds.getHeight());
        primaryStage.setX(restoredBounds.getMinX());
        primaryStage.setY(restoredBounds.getMinY());
    }

    /** Fits a saved window rectangle within a current screen's visual bounds. */
    static Rectangle2D fitWindowWithinScreen(Rectangle2D savedBounds, Rectangle2D targetScreen) {
        double width = Math.min(savedBounds.getWidth(), targetScreen.getWidth());
        double height = Math.min(savedBounds.getHeight(), targetScreen.getHeight());
        double x = clamp(savedBounds.getMinX(), targetScreen.getMinX(), targetScreen.getMaxX() - width);
        double y = clamp(savedBounds.getMinY(), targetScreen.getMinY(), targetScreen.getMaxY() - height);
        return new Rectangle2D(x, y, width, height);
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(value, maximum));
    }

    /**
     * Opens the help window or focuses on it if it's already opened.
     */
    @FXML
    public void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        primaryStage.hide();
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    /** Shows startup information in the result display until the user enters a command. */
    void showStartupMessage(String message) {
        resultDisplay.setFeedbackToUser(message);
    }

    private void showPersonList() {
        personListPanel.getRoot().setManaged(true);
        personListPanel.getRoot().setVisible(true);
        detailDisplay.getRoot().setManaged(false);
        detailDisplay.getRoot().setVisible(false);
    }

    private void showDetails(String details) {
        detailDisplay.setDetails(details);
        personListPanel.getRoot().setManaged(false);
        personListPanel.getRoot().setVisible(false);
        detailDisplay.getRoot().setManaged(true);
        detailDisplay.getRoot().setVisible(true);
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.tuitionbook.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());
            commandResult.getDetailsToShow().ifPresentOrElse(this::showDetails, () -> {
                showPersonList();
                commandResult.getPersonIdToScrollTo().ifPresent(personId ->
                        Platform.runLater(() -> personListPanel.scrollToPerson(personId)));
            });

            if (commandResult.isShowHelp()) {
                handleHelp();
            }

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("An error occurred while executing command: " + commandText);
            resultDisplay.setFeedbackToUser(e.getMessage());
            throw e;
        } finally {
            // All post-startup view mutations currently use this command-box callback. Any future
            // non-command mutation path must also refresh the indicator after its state settles.
            updateContactViewStatus();
        }
    }
}
