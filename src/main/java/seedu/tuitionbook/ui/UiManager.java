package seedu.tuitionbook.ui;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
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
    private static final String DEFAULT_LOAD_FAILURE_CAUSE = "Your data file could not be read.";
    private static final String RELOAD_OR_RESTART = "enter reload or restart TuitionBook";

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
            case MISSING_DATA_FILE -> showAlertDialogAndWait(AlertType.INFORMATION, "No data file found",
                    "TuitionBook started with an empty contact list",
                    "No contact data was found at " + displayedDataFilePath + ". Add contacts to get started.");
            case LOAD_FAILED_BACKED_UP -> showLoadFailureNotice("TuitionBook started with an empty contact list",
                    backedUpDialogMessage(notice, displayedDataFilePath),
                    backedUpResultMessage(notice, displayedDataFilePath));
            case LOAD_FAILED_BACKUP_FAILED -> showLoadFailureNotice(
                    "TuitionBook started with an empty contact list, but saving is disabled",
                    backupFailedDialogMessage(notice, displayedDataFilePath),
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

    private String loadFailureCause(StartupNotice notice) {
        return notice.loadFailureGuidance() == null ? DEFAULT_LOAD_FAILURE_CAUSE : notice.loadFailureGuidance();
    }

    private String backedUpDialogMessage(StartupNotice notice, Path displayedDataFilePath) {
        String correction = notice.loadFailureGuidance() == null
                ? "fix the JSON in the preserved file"
                : "resolve the reported problem in the preserved file";
        return loadFailureCause(notice) + "\n\n"
                + "Your original data is not lost. The data file was moved unchanged to "
                + displayedPath(notice.malformedFileBackupPath()) + ".\n\n"
                + "TuitionBook will save new contacts to " + displayedDataFilePath + ". To restore the original data, "
                + correction + ", rename it to " + displayedDataFilePath + " (replacing the new file), then "
                + RELOAD_OR_RESTART + ". Contacts added after recovery are in the new file and must be re-entered; "
                + "the files cannot be merged automatically.";
    }

    private String backedUpResultMessage(StartupNotice notice, Path displayedDataFilePath) {
        String correction = notice.loadFailureGuidance() == null
                ? "Fix the backup JSON"
                : "Resolve the reported problem";
        return loadFailureCause(notice) + "\nData file was moved unchanged to "
                + displayedPath(notice.malformedFileBackupPath()) + ".\nNew contacts save to " + displayedDataFilePath
                + ". " + correction + " and replace this file to restore the preserved contacts, then "
                + RELOAD_OR_RESTART + ".\nContacts added after recovery must be re-entered.";
    }

    private String backupFailedDialogMessage(StartupNotice notice, Path displayedDataFilePath) {
        return loadFailureCause(notice) + "\n\n"
                + "The data file could not be moved to a backup, so it remains at " + displayedDataFilePath + ".\n\n"
                + "To protect it, TuitionBook cannot save contacts. " + backupFailedCorrection(notice) + ", then "
                + RELOAD_OR_RESTART + ".";
    }

    private String backupFailedResultMessage(StartupNotice notice, Path displayedDataFilePath) {
        return loadFailureCause(notice) + "\nThe file could not be backed up and remains at " + displayedDataFilePath
                + ".\nSaving is disabled. " + backupFailedCorrection(notice) + ", then " + RELOAD_OR_RESTART + ".";
    }

    private String backupFailedCorrection(StartupNotice notice) {
        return notice.loadFailureGuidance() == null
                ? "Correct or move the file"
                : "Resolve the reported problem or move the file";
    }

    private void showLoadFailureNotice(String header, String dialogMessage, String resultMessage) {
        mainWindow.showStartupMessage(resultMessage);
        showAlertDialogAndWait(AlertType.WARNING, "Contact data could not be loaded", header, dialogMessage);
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
