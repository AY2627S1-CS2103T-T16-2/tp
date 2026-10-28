package seedu.tuitionbook.ui;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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
    private static final double LOAD_FAILURE_DIALOG_MIN_HEIGHT = 220;
    private static final double LOAD_FAILURE_DIALOG_PREF_WIDTH = 600;

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
        String cause = notice.loadFailureGuidance() == null
                ? "The data file could not be read."
                : notice.loadFailureGuidance();
        String correction = notice.loadFailureGuidance() == null
                ? "Fix the mistake in the backup file."
                : "Resolve the reported problem in the preserved file.";
        return cause + "\nOriginal file was preserved unchanged at "
                + displayedPath(notice.malformedFileBackupPath()) + ".\nNew contacts save to " + displayedDataFilePath
                + " and cannot be merged automatically with the preserved data.\nBefore restoring, close TuitionBook "
                + "and preserve any new data file. " + correction
                + " Replace the new data file with the corrected preserved file, then restart TuitionBook.";
    }

    private String backupFailedResultMessage(StartupNotice notice, Path displayedDataFilePath) {
        String cause = notice.loadFailureGuidance() == null
                ? "The data file could not be read."
                : notice.loadFailureGuidance();
        return cause + "\nThe original file could not be backed up and remains at "
                + displayedDataFilePath
                + ".\nSaving is disabled. Close TuitionBook, correct or move the original file, then restart "
                + "TuitionBook.";
    }

    private void showLoadFailureNotice(StartupNotice notice, Path displayedDataFilePath, String resultMessage) {
        mainWindow.showStartupMessage(resultMessage);
        Alert alert = new Alert(AlertType.WARNING);
        alert.getDialogPane().getStylesheets().add("view/DarkTheme.css");
        alert.initOwner(mainWindow.getPrimaryStage());
        alert.setTitle("Contact data could not be loaded");
        alert.setHeaderText("Data file could not be loaded");
        alert.getDialogPane().setContent(buildLoadFailureContent(notice, displayedDataFilePath));
        alert.getDialogPane().setId(ALERT_DIALOG_PANE_FIELD_ID);
        alert.getDialogPane().getStyleClass().add("startup-load-failure-dialog");
        alert.getDialogPane().setPrefWidth(LOAD_FAILURE_DIALOG_PREF_WIDTH);
        alert.setResizable(true);
        alert.setOnShown(event -> {
            Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
            dialogStage.setMinWidth(LOAD_FAILURE_DIALOG_MIN_WIDTH);
            dialogStage.setMinHeight(LOAD_FAILURE_DIALOG_MIN_HEIGHT);
        });
        alert.showAndWait();
    }

    private VBox buildLoadFailureContent(StartupNotice notice, Path displayedDataFilePath) {
        String cause = notice.loadFailureGuidance() == null
                ? "The data file could not be read."
                : notice.loadFailureGuidance();
        Label body = createWrappedLabel(cause + "\n\nTuitionBook started with an empty contact list.",
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
            return "The original file was preserved unchanged. If you add contacts now, TuitionBook saves them in "
                    + "a separate new data file. Those contacts cannot be merged automatically with the preserved "
                    + "data, so keep a copy of the new data file before restoring the backup.";
        }
        return "The original file could not be moved to a backup. Saving is disabled to prevent overwriting it.";
    }

    private TitledPane createRecoveryDetails(StartupNotice notice, Path displayedDataFilePath) {
        VBox details = new VBox(8);
        details.setPadding(new Insets(10));
        details.getStyleClass().add("startup-load-failure-details");

        Label steps = createWrappedLabel(recoverySteps(notice),
                "recoverySteps", "startup-load-failure-instructions");
        details.getChildren().add(steps);
        if (notice.type() == StartupNotice.Type.LOAD_FAILED_BACKED_UP) {
            details.getChildren().add(createPathEntry("Preserved original file",
                    displayedPath(notice.malformedFileBackupPath()), "preservedFilePath"));
            details.getChildren().add(createPathEntry("New data file", displayedDataFilePath, "newDataFilePath"));
        } else {
            details.getChildren().add(createPathEntry("Original file remains at",
                    displayedDataFilePath, "originalFilePath"));
        }

        ScrollPane scroller = new ScrollPane(details);
        scroller.setFitToWidth(true);
        scroller.setMinHeight(100);
        scroller.setPrefViewportHeight(220);
        scroller.getStyleClass().add("startup-load-failure-scroll");

        TitledPane recoveryDetails = new TitledPane("Recovery details", scroller);
        recoveryDetails.setId("recoveryDetails");
        recoveryDetails.setAnimated(false);
        recoveryDetails.setExpanded(false);
        return recoveryDetails;
    }

    private String recoverySteps(StartupNotice notice) {
        if (notice.type() == StartupNotice.Type.LOAD_FAILED_BACKED_UP) {
            return "1. Close TuitionBook before replacing any files.\n"
                    + "2. Copy and preserve any new data file in a separate safe location if it exists.\n"
                    + "3. Resolve the reported problem in a copy of the preserved original file.\n"
                    + "4. Replace the new data file with the corrected preserved file.\n"
                    + "5. Restart TuitionBook.\n"
                    + "6. Re-enter any contacts you added since the problem started.";
        }
        return "1. Close TuitionBook before changing the data file.\n"
                + "2. Correct or move the original data file.\n"
                + "3. Restart TuitionBook to retry loading and re-enable saving.";
    }

    private VBox createPathEntry(String labelText, Path path, String fieldId) {
        Label label = new Label(labelText);
        label.getStyleClass().add("startup-load-failure-path-label");
        TextField pathField = new TextField(path.toString());
        pathField.setId(fieldId);
        pathField.setEditable(false);
        pathField.setFocusTraversable(true);
        pathField.setAccessibleText(labelText);
        pathField.getStyleClass().add("startup-load-failure-path");
        return new VBox(4, label, pathField);
    }

    private Label createWrappedLabel(String text, String id, String styleClass) {
        Label label = new Label(text);
        label.setId(id);
        label.setWrapText(true);
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
