package seedu.tuitionbook.ui;

import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import seedu.tuitionbook.MainApp;
import seedu.tuitionbook.commons.core.LogsCenter;
import seedu.tuitionbook.commons.util.StringUtil;
import seedu.tuitionbook.logic.Logic;

/**
 * The manager of the UI component.
 */
public class UiManager implements Ui {

    public static final String ALERT_DIALOG_PANE_FIELD_ID = "alertDialogPane";

    private static final Logger logger = LogsCenter.getLogger(UiManager.class);
    private static final String ICON_APPLICATION = "/images/address_book_32.png";

    private Logic logic;
    private Path dataFilePath;
    private Optional<StartupNotice> startupNotice;
    private MainWindow mainWindow;

    /**
     * Creates a {@code UiManager} with the given logic, data file path, and optional startup notice.
     *
     * @param logic executes user commands
     * @param dataFilePath path shown in the status bar and startup notices
     * @param startupNotice contact-data recovery notice to show at startup, or empty for a clean load
     */
    public UiManager(Logic logic, Path dataFilePath, Optional<StartupNotice> startupNotice) {
        this.logic = logic;
        this.dataFilePath = dataFilePath;
        this.startupNotice = startupNotice;
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
            showFatalErrorDialogAndShutdown("Fatal error during initializing", e);
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
                showAlertDialogAndWait(AlertType.WARNING, "Guardian links repaired", header,
                        "All valid contacts were retained. Review invalid guardianId values in "
                                + displayedDataFilePath
                                + " before entering a contact-changing command; the next save removes invalid links.");
            }
            default -> throw new AssertionError("Unexpected startup notice: " + notice.type());
        }
    }

    private Path displayedPath(Path path) {
        return path.toAbsolutePath().normalize();
    }

    private String backedUpDialogMessage(StartupNotice notice, Path displayedDataFilePath) {
        if (notice.loadFailureGuidance() == null) {
            return "Your data file could not be read, so it was moved unchanged to "
                    + displayedPath(notice.malformedFileBackupPath())
                    + ". Your original data is not lost. TuitionBook will save new contacts to "
                    + displayedDataFilePath + ". To restore the original data, fix the JSON in the backup, "
                    + "rename it to " + displayedDataFilePath + " (replacing the new file), "
                    + "then restart TuitionBook. Contacts added after recovery are in the new file and must be "
                    + "re-entered; the files cannot be merged automatically.";
        }
        return notice.loadFailureGuidance() + " The data file was moved unchanged to "
                + displayedPath(notice.malformedFileBackupPath()) + ". Your original data is not lost. "
                + "TuitionBook will save new contacts to " + displayedDataFilePath + ". After resolving the reported "
                + "problem, rename the preserved file to " + displayedDataFilePath
                + " (replacing the new file), then restart TuitionBook. Contacts added after recovery are in the new "
                + "file and must be re-entered; the files cannot be merged automatically.";
    }

    private String backedUpResultMessage(StartupNotice notice, Path displayedDataFilePath) {
        if (notice.loadFailureGuidance() == null) {
            return "Data file was moved unchanged to " + displayedPath(notice.malformedFileBackupPath()) + ".\n"
                    + "New contacts save to " + displayedDataFilePath
                    + ". Fix the backup JSON and replace this file to restore it.\n"
                    + "Restart TuitionBook; contacts added after recovery must be re-entered.";
        }
        return notice.loadFailureGuidance() + "\nData file was moved unchanged to "
                + displayedPath(notice.malformedFileBackupPath()) + ".\nNew contacts save to " + displayedDataFilePath
                + ". Resolve the reported problem and replace this file to restore the preserved contacts.\n"
                + "Restart TuitionBook; contacts added after recovery must be re-entered.";
    }

    private String backupFailedDialogMessage(StartupNotice notice, Path displayedDataFilePath) {
        if (notice.loadFailureGuidance() == null) {
            return "Your data file could not be read and could not be moved to a backup. It remains at "
                    + displayedDataFilePath
                    + ". To protect it, TuitionBook cannot save contacts. Correct or move this file, then restart "
                    + "TuitionBook.";
        }
        String reason = notice.loadFailureGuidance();
        return reason + " It could not be moved to a backup and remains at " + displayedDataFilePath
                + ". To protect it, TuitionBook cannot save contacts. Resolve the reported problem or move the file, "
                + "then restart TuitionBook.";
    }

    private String backupFailedResultMessage(StartupNotice notice, Path displayedDataFilePath) {
        if (notice.loadFailureGuidance() == null) {
            return "Data file could not be backed up and remains at " + displayedDataFilePath + ".\n"
                    + "Saving is disabled. Correct or move the file, then restart TuitionBook.";
        }
        String reason = notice.loadFailureGuidance();
        return reason + "\nThe file could not be backed up and remains at " + displayedDataFilePath
                + ".\nSaving is disabled. Resolve the reported problem or move the file, then restart TuitionBook.";
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
        alert.initOwner(owner);
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
    private void showFatalErrorDialogAndShutdown(String title, Throwable e) {
        logger.severe(title + " " + e.getMessage() + StringUtil.getDetails(e));
        showAlertDialogAndWait(Alert.AlertType.ERROR, title, e.getMessage(), e.toString());
        Platform.exit();
        System.exit(1);
    }

}
