package seedu.tuitionbook.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.tuitionbook.logic.Logic;
import seedu.tuitionbook.logic.LogicManager;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.storage.GuardianLinkRepair;
import seedu.tuitionbook.storage.JsonTuitionBookStorage;
import seedu.tuitionbook.storage.JsonUserPrefsStorage;
import seedu.tuitionbook.storage.StorageManager;

/**
 * Tests the dialogs visible when the application starts with a contact-data notice.
 */
public class UiManagerStartupTest {

    private static final String INVALID_JSON_GUIDANCE = "The data file is not in a format TuitionBook can read. "
            + "Check the preserved file for mistakes such as a missing comma, quote or bracket, and save it as "
            + "UTF-8 text before trying again.";
    private static final String PERMISSION_GUIDANCE = "TuitionBook did not have permission to read the data file. "
            + "Restore read permission on the preserved file before trying again.";
    private static final String FILE_SYSTEM_GUIDANCE = "The data file could not be read because of a file-system "
            + "error. Check access to the preserved file before trying again.";
    private static final String INVALID_CONTACT_GUIDANCE = "The file contains invalid contact data. "
            + "Contact 3 in the file: Missing \"name\" value.";

    @TempDir
    public Path testFolder;

    @BeforeAll
    public static void initializeJavaFx() {
        JavaFxTestUtil.initialize();
    }

    @Test
    public void missingFile_showsInformationDialog() throws Exception {
        assertStartupDialog(StartupNotice.missingDataFile(), "No data file found",
                "No contact data file was found",
                "TuitionBook started with an empty contact list. Add a contact to get started.",
                "information");
    }

    @Test
    public void loadFailure_showsWarningWithRecoveryAction() throws Exception {
        Path backupPath = testFolder.resolve("contacts.json.backup.1");
        assertStartupDialog(StartupNotice.loadFailedBackedUp(backupPath), "Contact data could not be loaded",
                "Data file could not be loaded", "The data file could not be read.",
                "warning");
    }

    @Test
    public void permissionLoadFailure_showsPermissionRecoveryAction() throws Exception {
        Path backupPath = testFolder.resolve("contacts.json.backup");
        assertStartupDialog(StartupNotice.loadFailedBackedUp(backupPath, PERMISSION_GUIDANCE),
                "Contact data could not be loaded", "Data file could not be loaded", PERMISSION_GUIDANCE,
                "warning");
    }

    @Test
    public void invalidJsonLoadFailure_showsJsonRecoveryAction() throws Exception {
        Path backupPath = testFolder.resolve("contacts.json.backup");
        assertStartupDialog(StartupNotice.loadFailedBackedUp(backupPath, INVALID_JSON_GUIDANCE),
                "Contact data could not be loaded", "Data file could not be loaded", INVALID_JSON_GUIDANCE,
                "warning");
    }

    @Test
    public void invalidContactLoadFailure_showsSpecificProblemInMainContent() throws Exception {
        Path backupPath = testFolder.resolve("contacts.json.backup");
        assertStartupDialog(StartupNotice.loadFailedBackedUp(backupPath, INVALID_CONTACT_GUIDANCE),
                "Contact data could not be loaded", "Data file could not be loaded", INVALID_CONTACT_GUIDANCE,
                "warning");
    }

    @Test
    public void fileSystemLoadFailure_showsFileSystemRecoveryAction() throws Exception {
        Path backupPath = testFolder.resolve("contacts.json.backup");
        assertStartupDialog(StartupNotice.loadFailedBackedUp(backupPath, FILE_SYSTEM_GUIDANCE),
                "Contact data could not be loaded", "Data file could not be loaded", FILE_SYSTEM_GUIDANCE,
                "warning");
    }

    @Test
    public void loadFailure_backupFailureShowsWarningWithBlockedSavingAction() throws Exception {
        assertStartupDialog(StartupNotice.loadFailedBackupFailed(), "Contact data could not be loaded",
                "Data file could not be loaded", "The data file could not be read.",
                "warning");
    }

    @Test
    public void permissionLoadFailure_backedUpShowsPermissionRecoveryAction() throws Exception {
        Path backupPath = testFolder.resolve("contacts.json.backup.1");
        assertStartupDialog(StartupNotice.loadFailedBackedUp(backupPath, PERMISSION_GUIDANCE),
                "Contact data could not be loaded", "Data file could not be loaded", PERMISSION_GUIDANCE,
                "warning");
    }

    @Test
    public void loadFailure_backupFailureWithGuidance_showsGuidanceInDialogAndResult() throws Exception {
        String guidance = "Correct the file before trying again.";
        assertStartupDialog(StartupNotice.loadFailedBackupFailed(guidance),
                "Contact data could not be loaded",
                "Data file could not be loaded", guidance,
                "warning");
    }

    @Test
    public void invalidContactLoadFailure_backupFailureShowsProblemAndDisabledSaving() throws Exception {
        assertStartupDialog(StartupNotice.loadFailedBackupFailed(INVALID_CONTACT_GUIDANCE),
                "Contact data could not be loaded", "Data file could not be loaded", INVALID_CONTACT_GUIDANCE,
                "warning");
    }

    @Test
    public void permissionLoadFailure_backupFailureShowsPermissionRecoveryAction() throws Exception {
        assertStartupDialog(StartupNotice.loadFailedBackupFailed(PERMISSION_GUIDANCE),
                "Contact data could not be loaded",
                "Data file could not be loaded", PERMISSION_GUIDANCE,
                "warning");
    }

    @Test
    public void oneRepairedLink_showsSingularWarning() throws Exception {
        assertStartupDialog(StartupNotice.guardianLinksRepaired(List.of(repair("Sam Student"))),
                "Guardian links repaired",
                "1 guardian link was repaired",
                "All valid contacts were retained. Review invalid guardianId values in " + dataFilePath()
                        + " before entering a contact-changing command; the next save removes invalid links.",
                "warning");
    }

    @Test
    public void repairedLink_withoutOriginalGuardianId_showsDiagnosticWithoutGuardianId() throws Exception {
        assertStartupDialog(StartupNotice.guardianLinksRepaired(List.of(repairWithoutGuardianId("Malformed Link"))),
                "Guardian links repaired",
                "1 guardian link was repaired",
                "All valid contacts were retained. Review invalid guardianId values in " + dataFilePath()
                        + " before entering a contact-changing command; the next save removes invalid links.",
                "warning");
    }

    @Test
    public void multipleRepairedLinks_showsPluralWarning() throws Exception {
        assertStartupDialog(StartupNotice.guardianLinksRepaired(List.of(repair("Sam Student"),
                        repair("Grace Guardian"))), "Guardian links repaired",
                "2 guardian links were repaired",
                "All valid contacts were retained. Review invalid guardianId values in " + dataFilePath()
                        + " before entering a contact-changing command; the next save removes invalid links.",
                "warning");
    }

    @Test
    public void manyRepairedLinks_areShownInScrollableDetailsArea() throws Exception {
        List<GuardianLinkRepair> repairs = IntStream.range(0, 30)
                .mapToObj(index -> repair("Student " + index))
                .toList();
        assertStartupDialog(StartupNotice.guardianLinksRepaired(repairs), "Guardian links repaired",
                "30 guardian links were repaired", "", "warning");
    }

    @Test
    public void repairedLinks_relativeDataFilePath_showsAbsolutePathInWarning() throws Exception {
        Path relativeDataFilePath = Path.of("data", "contacts.json");
        assertStartupDialog(relativeDataFilePath,
                StartupNotice.guardianLinksRepaired(List.of(repair("Sam Student"))), "Guardian links repaired",
                "1 guardian link was repaired",
                "All valid contacts were retained. Review invalid guardianId values in "
                        + relativeDataFilePath.toAbsolutePath().normalize()
                        + " before entering a contact-changing command; the next save removes invalid links.",
                "warning");
    }

    @Test
    public void cleanData_focusesCommandFieldAndStartupKeysDoNotOpenHelp() throws Exception {
        Stage owner = JavaFxTestUtil.callOnFxThread(Stage::new);
        try {
            JavaFxTestUtil.runOnFxThread(() ->
                    new UiManager(logic(), dataFilePath(), Optional.empty()).start(owner));

            assertTrue(JavaFxTestUtil.waitForFxCondition(() ->
                    owner.getScene().getFocusOwner() == commandTextField(owner)));
            assertTrue(JavaFxTestUtil.callOnFxThread(owner::isShowing));
            assertNull(JavaFxTestUtil.callOnFxThread(this::findDialogPane));

            AtomicBoolean typingReachedCommandField = new AtomicBoolean();
            JavaFxTestUtil.runOnFxThread(() -> {
                TextField commandField = commandTextField(owner);
                commandField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.A) {
                        typingReachedCommandField.set(true);
                    }
                });
                Event.fireEvent(owner.getScene().getFocusOwner(), keyEvent(KeyEvent.KEY_PRESSED, KeyCode.A, "a"));
                Event.fireEvent(owner.getScene().getFocusOwner(), keyEvent(KeyEvent.KEY_PRESSED, KeyCode.SPACE, " "));
                Event.fireEvent(owner.getScene().getFocusOwner(), keyEvent(KeyEvent.KEY_RELEASED, KeyCode.SPACE, " "));
            });
            assertTrue(typingReachedCommandField.get());
            assertFalse(JavaFxTestUtil.callOnFxThread(() -> isHelpStageShowing(owner)));
        } finally {
            JavaFxTestUtil.runOnFxThread(owner::close);
        }
    }

    @Test
    public void fatalStartupError_beforeMainWindowExists_showsUnownedErrorDialogAndRequestsShutdown()
            throws Exception {
        Stage owner = JavaFxTestUtil.callOnFxThread(Stage::new);
        IllegalStateException startupFailure = new IllegalStateException("MainWindow.fxml is missing");
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean platformExitRequested = new AtomicBoolean();
        AtomicReference<Integer> systemExitStatus = new AtomicReference<>();
        CountDownLatch inspected = new CountDownLatch(1);
        CountDownLatch shutdownRequested = new CountDownLatch(1);
        Runnable platformExit = () -> platformExitRequested.set(true);
        IntConsumer systemExit = exitStatus -> {
            systemExitStatus.set(exitStatus);
            shutdownRequested.countDown();
        };

        try {
            Platform.runLater(() -> {
                try {
                    UiManager uiManager = new UiManager(logic(), dataFilePath(), Optional.empty(), platformExit,
                            systemExit);
                    uiManager.showFatalErrorDialogAndShutdown(owner, "Fatal error during initializing",
                            startupFailure);
                } catch (Throwable throwable) {
                    failure.set(throwable);
                    shutdownRequested.countDown();
                }
            });
            Platform.runLater(() -> {
                try {
                    DialogPane pane = findDialogPane();
                    Stage dialogStage = (Stage) pane.getScene().getWindow();
                    assertEquals("Fatal error during initializing", dialogStage.getTitle());
                    assertNull(dialogStage.getOwner());
                    assertEquals("MainWindow.fxml is missing", pane.getHeaderText());
                    TextArea details = (TextArea) pane.getContent();
                    assertEquals(startupFailure.toString(), details.getText());
                    assertFalse(details.isEditable());
                    assertTrue(details.isWrapText());
                    assertTrue(pane.getStyleClass().contains("error"));
                    pane.getScene().getWindow().hide();
                } catch (Throwable throwable) {
                    failure.set(throwable);
                } finally {
                    inspected.countDown();
                }
            });

            assertTrue(inspected.await(10, TimeUnit.SECONDS), "Fatal startup dialog was not inspected in time");
            assertTrue(shutdownRequested.await(10, TimeUnit.SECONDS), "Fatal startup shutdown was not requested");
            if (failure.get() != null) {
                throw new AssertionError(failure.get());
            }
            assertTrue(platformExitRequested.get());
            assertEquals(1, systemExitStatus.get());
        } finally {
            JavaFxTestUtil.runOnFxThread(owner::close);
        }
    }

    @Test
    public void fatalStartupError_withoutExceptionMessage_showsExceptionType() throws Exception {
        Stage owner = JavaFxTestUtil.callOnFxThread(Stage::new);
        NullPointerException startupFailure = new NullPointerException();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch inspected = new CountDownLatch(1);

        try {
            Platform.runLater(() -> {
                try {
                    UiManager uiManager = new UiManager(logic(), dataFilePath(), Optional.empty());
                    uiManager.showFatalErrorDialog(null, "Fatal error during initializing", startupFailure);
                } catch (Throwable throwable) {
                    failure.set(throwable);
                }
            });
            Platform.runLater(() -> {
                try {
                    DialogPane pane = findDialogPane();
                    Stage dialogStage = (Stage) pane.getScene().getWindow();
                    assertNull(dialogStage.getOwner());
                    assertEquals("NullPointerException", pane.getHeaderText());
                    TextArea details = (TextArea) pane.getContent();
                    assertEquals(startupFailure.toString(), details.getText());
                    assertFalse(details.isEditable());
                    assertTrue(details.isWrapText());
                    pane.getScene().getWindow().hide();
                } catch (Throwable throwable) {
                    failure.set(throwable);
                } finally {
                    inspected.countDown();
                }
            });

            assertTrue(inspected.await(10, TimeUnit.SECONDS), "Fatal startup dialog was not inspected in time");
            if (failure.get() != null) {
                throw new AssertionError(failure.get());
            }
        } finally {
            JavaFxTestUtil.runOnFxThread(owner::close);
        }
    }

    private void assertStartupDialog(StartupNotice notice, String title, String header, String content,
                                     String styleClass) throws Exception {
        assertStartupDialog(dataFilePath(), notice, title, header, content, styleClass);
    }

    private void assertStartupDialog(Path dataFilePath, StartupNotice notice, String title, String header,
                                     String content, String styleClass) throws Exception {
        AtomicReference<Stage> owner = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch inspected = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                owner.set(stage);
                new UiManager(logic(), dataFilePath, Optional.of(notice)).start(stage);
            } catch (Throwable throwable) {
                failure.set(throwable);
            }
        });
        Platform.runLater(() -> {
            try {
                DialogPane pane = findDialogPane();
                assertTrue(owner.get().isShowing());
                assertEquals(title, ((Stage) pane.getScene().getWindow()).getTitle());
                assertEquals(owner.get(), ((Stage) pane.getScene().getWindow()).getOwner());
                assertEquals(header, pane.getHeaderText());
                if (notice.type() == StartupNotice.Type.GUARDIAN_LINKS_REPAIRED) {
                    TextArea details = (TextArea) pane.getContent();
                    Stage dialogStage = (Stage) pane.getScene().getWindow();
                    assertFalse(details.isEditable());
                    assertTrue(details.isWrapText());
                    assertTrue(dialogStage.isResizable());
                    assertEquals(450, dialogStage.getMinWidth());
                    assertEquals(150, dialogStage.getMinHeight());
                    assertEquals(Math.min(12, Math.max(4, notice.guardianLinkRepairs().size())),
                            details.getPrefRowCount());
                    for (GuardianLinkRepair repair : notice.guardianLinkRepairs()) {
                        assertTrue(details.getText().contains(repair.personName()));
                        assertTrue(details.getText().contains(repair.personId().toString()));
                        assertTrue(details.getText().contains(repair.reason().getDescription()));
                        if (repair.guardianId() != null) {
                            assertTrue(details.getText().contains(repair.guardianId()));
                        }
                    }
                } else if (notice.type() == StartupNotice.Type.LOAD_FAILED_BACKED_UP
                        || notice.type() == StartupNotice.Type.LOAD_FAILED_BACKUP_FAILED) {
                    assertLoadFailureDialogLayout(pane, notice, content, dataFilePath);
                } else if (notice.type() == StartupNotice.Type.MISSING_DATA_FILE) {
                    assertMissingDataFileDialogLayout(pane, content, dataFilePath);
                } else {
                    assertEquals(content, pane.getContentText());
                }
                assertTrue(pane.getStyleClass().contains(styleClass));
                assertFalse(pane.getButtonTypes().isEmpty());
                if (notice.type() == StartupNotice.Type.LOAD_FAILED_BACKED_UP
                        || notice.type() == StartupNotice.Type.LOAD_FAILED_BACKUP_FAILED) {
                    TextArea resultDisplay = (TextArea) owner.get().getScene().lookup("#resultDisplay");
                    assertEquals(getExpectedResultMessage(notice), resultDisplay.getText());
                    assertTrue(resultDisplay.isWrapText());
                }
                pane.getScene().getWindow().hide();
            } catch (Throwable throwable) {
                failure.set(throwable);
            } finally {
                inspected.countDown();
            }
        });

        assertTrue(inspected.await(10, TimeUnit.SECONDS), "Startup dialog was not inspected in time");
        try {
            if (failure.get() != null) {
                throw new AssertionError(failure.get());
            }
            assertTrue(JavaFxTestUtil.waitForFxCondition(() ->
                            owner.get().getScene().getFocusOwner() == commandTextField(owner.get())),
                    "Command field was not selected as the scene focus owner after the startup dialog closed");
        } finally {
            if (owner.get() != null) {
                JavaFxTestUtil.runOnFxThread(owner.get()::close);
            }
        }
    }

    private void assertMissingDataFileDialogLayout(DialogPane pane, String expectedBody,
                                                   Path configuredDataFilePath) {
        Label body = (Label) pane.lookup("#missingDataFileBody");
        Stage dialogStage = (Stage) pane.getScene().getWindow();

        assertTrue(dialogStage.isResizable());
        assertTrue(pane.getStyleClass().contains("startup-missing-data-dialog"));
        assertEquals(expectedBody, body.getText());
        assertTrue(body.isWrapText());
        assertFalse(body.getFont().getStyle().toLowerCase().contains("bold"));
        assertFalse(body.getText().contains(configuredDataFilePath.toAbsolutePath().normalize().toString()));
    }

    private void assertLoadFailureDialogLayout(DialogPane pane, StartupNotice notice, String expectedCause,
                                               Path configuredDataFilePath) {
        Stage dialogStage = (Stage) pane.getScene().getWindow();
        Label body = (Label) pane.lookup("#loadFailureBody");
        Label outcome = (Label) pane.lookup("#loadFailureOutcome");
        TitledPane recoveryDetails = (TitledPane) pane.lookup("#recoveryDetails");
        Label recoverySteps = (Label) pane.lookup("#recoverySteps");

        assertTrue(dialogStage.isResizable());
        assertTrue(dialogStage.getMinWidth() > 0);
        assertTrue(dialogStage.getMinHeight() > 0);
        assertTrue(pane.getStyleClass().contains("startup-load-failure-dialog"));
        assertTrue(body.isWrapText());
        assertTrue(body.getText().contains(expectedCause));
        assertTrue(body.getText().contains("started with an empty contact list"));
        assertFalse(body.getFont().getStyle().toLowerCase().contains("bold"));
        assertTrue(outcome.isWrapText());
        assertEquals("Recovery details", recoveryDetails.getText());
        assertFalse(recoveryDetails.isExpanded());
        assertTrue(recoveryDetails.getContent() instanceof ScrollPane);
        ScrollPane detailsScroller = (ScrollPane) recoveryDetails.getContent();
        assertTrue(detailsScroller.isFitToWidth());
        assertTrue(recoverySteps.isWrapText());
        assertTrue(recoverySteps.getText().contains("1. Close TuitionBook"));
        assertNull(pane.lookup("#loadFailureDetails"));

        Path displayedDataFilePath = configuredDataFilePath.toAbsolutePath().normalize();
        if (notice.type() == StartupNotice.Type.LOAD_FAILED_BACKED_UP) {
            TextField preservedPath = (TextField) pane.lookup("#preservedFilePath");
            TextField newDataPath = (TextField) pane.lookup("#newDataFilePath");
            assertReadOnlySelectablePath(preservedPath,
                    notice.malformedFileBackupPath().toAbsolutePath().normalize());
            assertReadOnlySelectablePath(newDataPath, displayedDataFilePath);
            assertEquals("Preserved original file", preservedPath.getAccessibleText());
            assertEquals("New data file", newDataPath.getAccessibleText());
            assertTrue(outcome.getText().contains("preserved unchanged"));
            assertTrue(outcome.getText().contains("cannot be merged automatically"));
            assertTrue(recoverySteps.getText().contains("preserve any new data file"));
            assertTrue(recoverySteps.getText().contains(
                    "6. Re-enter any contacts you added since the problem started."));
            assertFalse(body.getText().contains(displayedDataFilePath.toString()));
            assertFalse(outcome.getText().contains(displayedDataFilePath.toString()));
            assertFalse(recoverySteps.getText().contains(displayedDataFilePath.toString()));
        } else {
            TextField originalPath = (TextField) pane.lookup("#originalFilePath");
            assertReadOnlySelectablePath(originalPath, displayedDataFilePath);
            assertEquals("Original file remains at", originalPath.getAccessibleText());
            assertTrue(outcome.getText().contains("Saving is disabled"));
            assertTrue(recoverySteps.getText().contains("Correct or move the original data file"));
            assertFalse(body.getText().contains(displayedDataFilePath.toString()));
            assertFalse(outcome.getText().contains(displayedDataFilePath.toString()));
            assertFalse(recoverySteps.getText().contains(displayedDataFilePath.toString()));
        }

        recoveryDetails.setExpanded(true);
        pane.applyCss();
        pane.layout();
        assertTrue(detailsScroller.isVisible());
        assertTrue(detailsScroller.getHeight() > 0);
    }

    private void assertReadOnlySelectablePath(TextField pathField, Path expectedPath) {
        assertEquals(expectedPath.toString(), pathField.getText());
        assertFalse(pathField.isEditable());
        assertTrue(pathField.isFocusTraversable());
        pathField.selectAll();
        assertEquals(expectedPath.toString(), pathField.getSelectedText());
    }

    private TextField commandTextField(Stage owner) {
        return (TextField) owner.getScene().lookup("#commandTextField");
    }

    private KeyEvent keyEvent(javafx.event.EventType<KeyEvent> type, KeyCode code, String character) {
        return new KeyEvent(type, character, character, code, false, false, false, false);
    }

    private boolean isHelpStageShowing(Stage owner) {
        return Window.getWindows().stream()
                .filter(window -> window != owner)
                .filter(Stage.class::isInstance)
                .map(Stage.class::cast)
                .anyMatch(stage -> stage.isShowing() && "Help".equals(stage.getTitle()));
    }

    private DialogPane findDialogPane() {
        return Window.getWindows().stream()
                .filter(Window::isShowing)
                .map(Window::getScene)
                .filter(scene -> scene != null && scene.getRoot() instanceof DialogPane)
                .map(scene -> (DialogPane) scene.getRoot())
                .findFirst()
                .orElse(null);
    }

    private String getExpectedResultMessage(StartupNotice notice) {
        Path displayedDataFilePath = dataFilePath().toAbsolutePath().normalize();
        String description = notice.loadFailureGuidance() == null
                ? "The data file could not be read."
                : notice.loadFailureGuidance();
        if (notice.type() == StartupNotice.Type.LOAD_FAILED_BACKED_UP) {
            String correction = notice.loadFailureGuidance() == null
                    ? "Fix the mistake in the backup file."
                    : "Resolve the reported problem in the preserved file.";
            return description + "\nOriginal file was preserved unchanged at "
                    + notice.malformedFileBackupPath().toAbsolutePath().normalize() + ".\nNew contacts save to "
                    + displayedDataFilePath
                    + " and cannot be merged automatically with the preserved data.\nBefore restoring, close "
                    + "TuitionBook and preserve any new data file. " + correction
                    + " Replace the new data file with the corrected preserved file, then restart TuitionBook.";
        }
        return description + "\nThe original file could not be backed up and remains at " + displayedDataFilePath
                + ".\nSaving is disabled. Close TuitionBook, correct or move the original file, then restart "
                + "TuitionBook.";
    }

    private GuardianLinkRepair repair(String personName) {
        return new GuardianLinkRepair(personName, UUID.randomUUID(), "missing-guardian-id",
                GuardianLinkRepair.Reason.MISSING_GUARDIAN);
    }

    private GuardianLinkRepair repairWithoutGuardianId(String personName) {
        return new GuardianLinkRepair(personName, UUID.randomUUID(), null,
                GuardianLinkRepair.Reason.MALFORMED_GUARDIAN_ID);
    }

    private Logic logic() {
        return new LogicManager(new ModelManager(), new StorageManager(
                new JsonTuitionBookStorage(dataFilePath()),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
    }

    private Path dataFilePath() {
        return testFolder.resolve("contacts.json");
    }
}
