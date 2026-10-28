package seedu.tuitionbook.ui;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.tuitionbook.MainApp;
import seedu.tuitionbook.commons.core.LogsCenter;
import seedu.tuitionbook.commons.util.StringUtil;
import seedu.tuitionbook.logic.Logic;
import seedu.tuitionbook.storage.GuardianLinkRepair;

/**
 * The manager of the UI component.
 */
public class UiManager implements Ui {

    public static final String ALERT_DIALOG_PANE_FIELD_ID = "alertDialogPane";

    private static final Logger logger = LogsCenter.getLogger(UiManager.class);
    private static final String ICON_APPLICATION = "/images/address_book_32.png";
    private static final double REPAIR_DIALOG_MIN_WIDTH = 450;
    private static final double REPAIR_DIALOG_MIN_HEIGHT = 150;
    private static final double LOAD_FAILURE_DIALOG_MIN_WIDTH = 480;
    private static final double LOAD_FAILURE_DIALOG_MIN_HEIGHT = 230;
    private static final double LOAD_FAILURE_DIALOG_PREF_WIDTH = 600;
    private static final double LOAD_FAILURE_DETAILS_MIN_HEIGHT = 80;
    private static final double LOAD_FAILURE_DETAILS_PREF_HEIGHT = 220;
    private static final double MAX_PLAUSIBLE_DECORATION = 200;
    private static final int MAX_LAYOUT_REFIT_ATTEMPTS = 10;
    private static final String DEFAULT_LOAD_FAILURE_CAUSE = "Your data file could not be read.";

    private Logic logic;
    private Path dataFilePath;
    private Optional<StartupNotice> startupNotice;
    private final Runnable platformExit;
    private final IntConsumer systemExit;
    private MainWindow mainWindow;

    /**
     * Creates a {@code UiManager} with the given logic, data file path, and optional startup notice.
     *
     * @param logic executes user commands
     * @param dataFilePath path shown in the status bar and startup notices
     * @param startupNotice contact-data recovery notice to show at startup, or empty for a clean load
     */
    public UiManager(Logic logic, Path dataFilePath, Optional<StartupNotice> startupNotice) {
        this(logic, dataFilePath, startupNotice, Platform::exit, System::exit);
    }

    /** Creates a {@code UiManager} with replaceable exit operations for testing fatal startup failures. */
    UiManager(Logic logic, Path dataFilePath, Optional<StartupNotice> startupNotice,
              Runnable platformExit, IntConsumer systemExit) {
        this.logic = logic;
        this.dataFilePath = dataFilePath;
        this.startupNotice = startupNotice;
        this.platformExit = platformExit;
        this.systemExit = systemExit;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting UI...");

        //Set the application icon.
        primaryStage.getIcons().add(getImage(ICON_APPLICATION));

        try {
            mainWindow = new MainWindow(primaryStage, logic, dataFilePath);
            mainWindow.show(); //This should be called before creating other UI parts
            mainWindow.fillInnerParts();
            startupNotice.ifPresent(this::showStartupNotice);
            Platform.runLater(mainWindow::focusCommandBox);

        } catch (Throwable e) {
            logger.severe(StringUtil.getDetails(e));
            showFatalErrorDialogAndShutdown(primaryStage, "Fatal error during initializing", e);
        }
    }

    private Image getImage(String imagePath) {
        return new Image(MainApp.class.getResourceAsStream(imagePath));
    }

    private void showStartupNotice(StartupNotice notice) {
        Path displayedDataFilePath = displayedPath(dataFilePath);
        switch (notice.type()) {
            case MISSING_DATA_FILE -> showMissingDataFileNotice();
            case LOAD_FAILED_BACKED_UP -> showLoadFailureNotice(notice, displayedDataFilePath,
                    backedUpResultMessage(notice, displayedDataFilePath));
            case LOAD_FAILED_BACKUP_FAILED -> showLoadFailureNotice(notice, displayedDataFilePath,
                    backupFailedResultMessage(notice, displayedDataFilePath));
            case GUARDIAN_LINKS_REPAIRED -> {
                int count = notice.repairedGuardianLinkCount();
                String header = count + (count == 1 ? " guardian link was repaired" : " guardian links were repaired");
                showGuardianRepairNotice(header, notice.guardianLinkRepairs(), displayedDataFilePath);
            }
            default -> throw new AssertionError("Unexpected startup notice: " + notice.type());
        }
    }

    private void showGuardianRepairNotice(String header, List<GuardianLinkRepair> repairs,
                                           Path displayedDataFilePath) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.getDialogPane().getStylesheets().add("view/DarkTheme.css");
        alert.initOwner(mainWindow.getPrimaryStage());
        alert.setTitle("Guardian links repaired");
        alert.setHeaderText(header);

        TextArea details = new TextArea();
        details.setEditable(false);
        details.setWrapText(true);
        details.setPrefRowCount(Math.min(12, Math.max(4, repairs.size())));
        details.setPrefColumnCount(70);
        details.setText(buildGuardianRepairDetails(repairs, displayedDataFilePath));
        alert.getDialogPane().setContent(details);
        alert.getDialogPane().setId(ALERT_DIALOG_PANE_FIELD_ID);
        alert.setResizable(true);
        alert.setOnShown(event -> {
            Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
            dialogStage.setMinWidth(REPAIR_DIALOG_MIN_WIDTH);
            dialogStage.setMinHeight(REPAIR_DIALOG_MIN_HEIGHT);
        });
        alert.showAndWait();
    }

    private String buildGuardianRepairDetails(List<GuardianLinkRepair> repairs,
                                              Path displayedDataFilePath) {
        StringBuilder details = new StringBuilder("All valid contacts were retained.\n\n");
        for (GuardianLinkRepair repair : repairs) {
            details.append("• ").append(repair.personName())
                    .append(" [").append(repair.personId()).append("] — ")
                    .append(repair.reason().getDescription());
            if (repair.guardianId() != null) {
                details.append(" (guardianId: ").append(repair.guardianId()).append(")");
            }
            details.append('\n');
        }
        details.append("\nReview the invalid guardianId values in ").append(displayedDataFilePath)
                .append(" before entering a contact-changing command. The next save removes the invalid links.");
        return details.toString();
    }

    private Path displayedPath(Path path) {
        return path.toAbsolutePath().normalize();
    }

    private void showMissingDataFileNotice() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.getDialogPane().getStylesheets().add("view/DarkTheme.css");
        alert.initOwner(mainWindow.getPrimaryStage());
        alert.setTitle("No data file found");
        alert.setHeaderText("No contact data file was found");
        Label body = createWrappedLabel("TuitionBook started with an empty contact list. Add a contact to get started.",
                "missingDataFileBody", "startup-missing-data-body");
        alert.getDialogPane().setContent(body);
        alert.getDialogPane().setId(ALERT_DIALOG_PANE_FIELD_ID);
        alert.getDialogPane().getStyleClass().add("startup-missing-data-dialog");
        alert.getDialogPane().setPrefWidth(LOAD_FAILURE_DIALOG_MIN_WIDTH);
        alert.setResizable(true);
        alert.showAndWait();
    }

    private String backedUpResultMessage(StartupNotice notice, Path displayedDataFilePath) {
        return loadFailureCause(notice) + "\nBackup file: "
                + displayedPath(notice.malformedFileBackupPath()) + ".\nNew contacts save to " + displayedDataFilePath
                + " and cannot be merged automatically with the backup file.\nFix the problem in the backup file. "
                + "If a new data file exists, copy it somewhere safe, then replace it with the corrected backup. "
                + "Enter reload while TuitionBook remains open, or close and restart TuitionBook. Re-enter contacts "
                + "that existed only in the separately saved new file as needed.";
    }

    private String backupFailedResultMessage(StartupNotice notice, Path displayedDataFilePath) {
        return loadFailureCause(notice) + "\nThe original file could not be backed up and remains at "
                + displayedDataFilePath
                + ".\nSaving is disabled. Correct or move the original file, then enter reload while TuitionBook "
                + "remains open, or close and restart TuitionBook.";
    }

    private String loadFailureCause(StartupNotice notice) {
        return notice.loadFailureGuidance() == null ? DEFAULT_LOAD_FAILURE_CAUSE : notice.loadFailureGuidance();
    }

    private void showLoadFailureNotice(StartupNotice notice, Path displayedDataFilePath, String resultMessage) {
        mainWindow.showStartupMessage(resultMessage);
        Alert alert = new Alert(AlertType.WARNING);
        alert.getDialogPane().getStylesheets().add("view/DarkTheme.css");
        alert.initOwner(mainWindow.getPrimaryStage());
        alert.setTitle("Contact data could not be loaded");
        alert.setHeaderText(loadFailureHeader(notice));
        alert.getDialogPane().setContent(buildLoadFailureContent(notice, displayedDataFilePath));
        alert.getDialogPane().setId(ALERT_DIALOG_PANE_FIELD_ID);
        alert.getDialogPane().getStyleClass().add("startup-load-failure-dialog");
        alert.getDialogPane().setPrefWidth(LOAD_FAILURE_DIALOG_PREF_WIDTH);
        alert.setResizable(true);
        alert.setOnShown(event -> {
            Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
            TitledPane recoveryDetails = (TitledPane) alert.getDialogPane().lookup("#recoveryDetails");
            Rectangle2D visualBounds = activeVisualBounds(dialogStage);
            dialogStage.setWidth(Math.min(LOAD_FAILURE_DIALOG_PREF_WIDTH, visualBounds.getWidth()));
            dialogStage.sizeToScene();
            fitLoadFailureDialogWithin(dialogStage, visualBounds);
            recoveryDetails.expandedProperty().addListener((observable, wasExpanded, isExpanded) ->
                    Platform.runLater(() -> {
                        dialogStage.sizeToScene();
                        fitLoadFailureDialogWithin(dialogStage, activeVisualBounds(dialogStage));
                    }));
            dialogStage.widthProperty().addListener((observable, oldWidth, newWidth) ->
                    Platform.runLater(() -> fitLoadFailureDialogWithin(dialogStage,
                            activeVisualBounds(dialogStage))));
            dialogStage.heightProperty().addListener((observable, oldHeight, newHeight) -> {
                if (newHeight.doubleValue() < dialogStage.getMinHeight() - 1) {
                    // Restore the floor immediately: some platforms do not clamp programmatic
                    // resizes to the minimum size, and a later refit recomputes the exact fit.
                    dialogStage.setHeight(dialogStage.getMinHeight());
                    Platform.runLater(() -> fitLoadFailureDialogWithin(dialogStage,
                            activeVisualBounds(dialogStage)));
                }
            });
        });
        alert.showAndWait();
    }

    private String loadFailureHeader(StartupNotice notice) {
        return notice.type() == StartupNotice.Type.LOAD_FAILED_BACKED_UP
                ? "Your contacts are safe in a backup"
                : "Saving is paused to protect your data file";
    }

    private VBox buildLoadFailureContent(StartupNotice notice, Path displayedDataFilePath) {
        String bodyText = loadFailureCause(notice) + "\n\nTuitionBook started with an empty contact list.";
        Label body = createWrappedLabel(bodyText,
                "loadFailureBody", "startup-load-failure-body");
        Label outcome = createWrappedLabel(loadFailureOutcome(notice),
                "loadFailureOutcome", "startup-load-failure-body");
        TitledPane recoveryDetails = createRecoveryDetails(notice, displayedDataFilePath);
        VBox.setVgrow(recoveryDetails, Priority.ALWAYS);

        VBox content = new VBox(12, body, outcome, recoveryDetails);
        content.setPrefWidth(LOAD_FAILURE_DIALOG_PREF_WIDTH);
        content.getStyleClass().add("startup-load-failure-content");
        return content;
    }

    private String loadFailureOutcome(StartupNotice notice) {
        if (notice.type() == StartupNotice.Type.LOAD_FAILED_BACKED_UP) {
            return "The original file is preserved unchanged in a backup file. If you add contacts now, "
                    + "TuitionBook saves them in a separate new data file. Those contacts cannot be merged "
                    + "automatically with the backup file, so keep a copy of the new data file before restoring it.";
        }
        return "The original file could not be moved to a backup file. Saving is disabled to prevent overwriting it.";
    }

    private TitledPane createRecoveryDetails(StartupNotice notice, Path displayedDataFilePath) {
        VBox details = new VBox(8);
        details.setPadding(new Insets(10));
        details.getStyleClass().add("startup-load-failure-details");

        Label steps = createWrappedLabel(recoverySteps(notice),
                "recoverySteps", "startup-load-failure-instructions");
        details.getChildren().add(steps);
        if (notice.type() == StartupNotice.Type.LOAD_FAILED_BACKED_UP) {
            details.getChildren().add(createPathEntry("Backup file",
                    displayedPath(notice.malformedFileBackupPath()), "backupFilePath"));
            details.getChildren().add(createPathEntry("New data file", displayedDataFilePath, "newDataFilePath"));
        } else {
            details.getChildren().add(createPathEntry("Original file remains at",
                    displayedDataFilePath, "originalFilePath"));
        }

        ScrollPane scroller = new ScrollPane(details);
        scroller.setId("recoveryDetailsScroll");
        scroller.setFitToWidth(true);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setMinHeight(LOAD_FAILURE_DETAILS_MIN_HEIGHT);
        scroller.getStyleClass().add("startup-load-failure-scroll");

        TitledPane recoveryDetails = new TitledPane("Recovery details", scroller);
        recoveryDetails.setId("recoveryDetails");
        recoveryDetails.setAnimated(false);
        recoveryDetails.setExpanded(false);
        return recoveryDetails;
    }

    private String recoverySteps(StartupNotice notice) {
        if (notice.type() == StartupNotice.Type.LOAD_FAILED_BACKED_UP) {
            return "1. Fix the problem described above in the backup file. Do not enter contact-changing commands "
                    + "during recovery.\n"
                    + "2. If there is a new data file, copy it somewhere safe.\n"
                    + "3. The backup and new data file cannot be merged automatically.\n"
                    + "4. Replace the new data file with the corrected backup file.\n"
                    + "5. Either enter reload while TuitionBook remains open, or close and restart TuitionBook.\n"
                    + "6. Re-enter contacts that existed only in the separately saved new file as needed.";
        }
        return "1. The original file remains in place and saving is blocked. Do not enter contact-changing commands "
                + "during recovery.\n"
                + "2. Correct or move the original data file while TuitionBook remains open, or close TuitionBook "
                + "before doing so.\n"
                + "3. If TuitionBook remains open, enter reload. Otherwise, restart TuitionBook.";
    }

    private VBox createPathEntry(String labelText, Path path, String fieldId) {
        Label label = new Label(labelText);
        label.getStyleClass().add("startup-load-failure-path-label");
        TextField pathField = new TextField(path.toString());
        pathField.setId(fieldId);
        pathField.setEditable(false);
        pathField.setFocusTraversable(true);
        pathField.setMaxWidth(Double.MAX_VALUE);
        pathField.setAccessibleText(labelText);
        pathField.getStyleClass().add("startup-load-failure-path");
        return new VBox(4, label, pathField);
    }

    private static Rectangle2D activeVisualBounds(Window window) {
        List<Screen> screens = Screen.getScreensForRectangle(window.getX(), window.getY(),
                Math.max(1, window.getWidth()), Math.max(1, window.getHeight()));
        return (screens.isEmpty() ? Screen.getPrimary() : screens.getFirst()).getVisualBounds();
    }

    /** Keeps the startup load-failure dialog and its recovery viewport inside the supplied screen bounds. */
    static void fitLoadFailureDialogWithin(Stage dialogStage, Rectangle2D visualBounds) {
        DialogPane dialogPane = (DialogPane) dialogStage.getScene().getRoot();
        TitledPane recoveryDetails = (TitledPane) dialogPane.lookup("#recoveryDetails");
        ScrollPane recoveryScroller = (ScrollPane) recoveryDetails.getContent();
        dialogPane.applyCss();
        dialogPane.layout();

        // Size against the stage's target width: during a resize the pane still has its old width,
        // so measuring the pane directly underestimates how tall the wrapped labels become.
        boolean sceneLaidOut = dialogStage.getScene().getWidth() > 1 && dialogStage.getScene().getHeight() > 1;
        int refitAttempts = (Integer) dialogStage.getProperties().getOrDefault("loadFailureRefitAttempts", 0);
        if (!sceneLaidOut && refitAttempts < MAX_LAYOUT_REFIT_ATTEMPTS
                && dialogStage.getProperties().put("loadFailureRefitQueued", Boolean.TRUE) == null) {
            // Decorations cannot be measured yet, so this fit sizes the window as though it had no
            // decorations, leaving it one title bar too short. Refit until the scene is laid out,
            // giving up after a bounded number of attempts so an unshown stage cannot loop forever.
            dialogStage.getProperties().put("loadFailureRefitAttempts", refitAttempts + 1);
            Platform.runLater(() -> {
                dialogStage.getProperties().remove("loadFailureRefitQueued");
                fitLoadFailureDialogWithin(dialogStage, activeVisualBounds(dialogStage));
            });
        }
        double decorationWidth = rememberedDecoration(dialogStage, "loadFailureDecorationWidth",
                dialogStage.getWidth() - dialogStage.getScene().getWidth(), sceneLaidOut);
        double decorationHeight = rememberedDecoration(dialogStage, "loadFailureDecorationHeight",
                dialogStage.getHeight() - dialogStage.getScene().getHeight(), sceneLaidOut);
        double targetWidth = Math.min(dialogStage.getWidth(), visualBounds.getWidth());
        double targetContentWidth = Math.max(1, targetWidth - decorationWidth);

        double measuredViewportWidth = recoveryScroller.getViewportBounds().getWidth();
        double viewportWidth = measuredViewportWidth > 0 ? measuredViewportWidth
                : Math.max(1, targetContentWidth - 60);
        double contentHeight = recoveryScroller.getContent().prefHeight(viewportWidth);
        double detailsHeight = Math.max(LOAD_FAILURE_DETAILS_MIN_HEIGHT,
                Math.min(LOAD_FAILURE_DETAILS_PREF_HEIGHT, contentHeight + 2));
        recoveryScroller.setMinHeight(LOAD_FAILURE_DETAILS_MIN_HEIGHT);
        recoveryScroller.setPrefViewportHeight(detailsHeight);
        recoveryScroller.setMaxHeight(detailsHeight);
        dialogPane.applyCss();
        dialogPane.layout();
        double desiredHeight = dialogPane.prefHeight(targetContentWidth) + decorationHeight;
        double requiredMinimumHeight = Math.max(LOAD_FAILURE_DIALOG_MIN_HEIGHT, desiredHeight);
        if (recoveryDetails.isExpanded()) {
            recoveryScroller.setPrefViewportHeight(LOAD_FAILURE_DETAILS_MIN_HEIGHT);
            recoveryScroller.setMaxHeight(LOAD_FAILURE_DETAILS_MIN_HEIGHT);
            dialogPane.applyCss();
            dialogPane.layout();
            requiredMinimumHeight = Math.max(LOAD_FAILURE_DIALOG_MIN_HEIGHT,
                    dialogPane.prefHeight(targetContentWidth) + decorationHeight);
            recoveryScroller.setPrefViewportHeight(detailsHeight);
            recoveryScroller.setMaxHeight(detailsHeight);
            dialogPane.applyCss();
            dialogPane.layout();
            desiredHeight = dialogPane.prefHeight(targetContentWidth) + decorationHeight;
        }

        double boundedMinWidth = Math.min(LOAD_FAILURE_DIALOG_MIN_WIDTH, visualBounds.getWidth());
        double boundedMinHeight = Math.min(requiredMinimumHeight, visualBounds.getHeight());
        dialogStage.setMinWidth(boundedMinWidth);
        dialogStage.setMinHeight(boundedMinHeight);
        dialogStage.setMaxWidth(visualBounds.getWidth());
        dialogStage.setMaxHeight(visualBounds.getHeight());
        double targetHeight = Math.min(Math.max(desiredHeight, boundedMinHeight), visualBounds.getHeight());
        dialogStage.setWidth(targetWidth);
        dialogStage.setHeight(targetHeight);
        // Clamp against the just-requested sizes: the size properties update asynchronously on some
        // platforms, so reading them back here could reposition the window using stale, smaller sizes.
        dialogStage.setX(clamp(dialogStage.getX(), visualBounds.getMinX(),
                visualBounds.getMaxX() - targetWidth));
        dialogStage.setY(clamp(dialogStage.getY(), visualBounds.getMinY(),
                visualBounds.getMaxY() - targetHeight));
    }


    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(value, maximum));
    }

    /**
     * Returns the window-decoration size remembered for {@code dialogStage}, measuring it once while the
     * stage and scene sizes agree. Later fits reuse the remembered value because a resize updates the
     * stage size before the scene catches up, which would make a fresh measurement wrong. Measurements
     * taken before the scene is laid out, or that are implausibly large, are not remembered; zero is
     * used until a sane measurement is available.
     */
    private static double rememberedDecoration(Stage dialogStage, String key, double measuredDecoration,
            boolean sceneLaidOut) {
        Object remembered = dialogStage.getProperties().get(key);
        if (remembered instanceof Double value) {
            return value;
        }
        if (!sceneLaidOut || !Double.isFinite(measuredDecoration) || measuredDecoration < 0
                || measuredDecoration > MAX_PLAUSIBLE_DECORATION) {
            return 0;
        }
        dialogStage.getProperties().put(key, measuredDecoration);
        return measuredDecoration;
    }

    private Label createWrappedLabel(String text, String id, String styleClass) {
        Label label = new Label(text);
        label.setId(id);
        label.setWrapText(true);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.getStyleClass().add(styleClass);
        return label;
    }

    void showAlertDialogAndWait(Alert.AlertType type, String title, String headerText, String contentText) {
        showAlertDialogAndWait(mainWindow.getPrimaryStage(), type, title, headerText, contentText);
    }

    /**
     * Shows an alert dialog on {@code owner} with the given parameters.
     * This method only returns after the user has closed the alert dialog.
     */
    private static void showAlertDialogAndWait(Stage owner, AlertType type, String title, String headerText,
                                               String contentText) {
        final Alert alert = new Alert(type);
        alert.getDialogPane().getStylesheets().add("view/DarkTheme.css");
        initializeOwnerIfAvailable(alert, owner);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.getDialogPane().setId(ALERT_DIALOG_PANE_FIELD_ID);
        alert.showAndWait();
    }

    /**
     * Shows an error alert dialog with {@code title} and error message, {@code e},
     * and exits the application after the user has closed the alert dialog.
     */
    void showFatalErrorDialogAndShutdown(Stage owner, String title, Throwable e) {
        showFatalErrorDialog(owner, title, e);
        platformExit.run();
        systemExit.accept(1);
    }

    /** Shows the fatal startup error without depending on the main window being initialized. */
    void showFatalErrorDialog(Stage owner, String title, Throwable e) {
        String errorMessage = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        logger.severe(title + " " + errorMessage + StringUtil.getDetails(e));
        showFatalErrorDialogAndWait(owner, title, errorMessage, e.toString());
    }

    /** Shows a fatal error's details in a wrapped, readable control. */
    private static void showFatalErrorDialogAndWait(Stage owner, String title, String headerText,
                                                    String detailsText) {
        final Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.getDialogPane().getStylesheets().add("view/DarkTheme.css");
        initializeOwnerIfAvailable(alert, owner);
        alert.setTitle(title);
        alert.setHeaderText(headerText);

        TextArea details = new TextArea(detailsText);
        details.setEditable(false);
        details.setWrapText(true);
        details.setFocusTraversable(false);
        details.setPrefColumnCount(70);
        details.setPrefRowCount(4);
        alert.getDialogPane().setContent(details);
        alert.getDialogPane().setId(ALERT_DIALOG_PANE_FIELD_ID);
        alert.showAndWait();
    }

    /** Sets the alert owner only when the supplied stage has an initialized scene. */
    private static void initializeOwnerIfAvailable(Alert alert, Stage owner) {
        if (owner != null && owner.getScene() != null) {
            alert.initOwner(owner);
        }
    }

}
