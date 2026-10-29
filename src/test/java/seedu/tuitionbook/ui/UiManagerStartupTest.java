package seedu.tuitionbook.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import seedu.tuitionbook.logic.Logic;
import seedu.tuitionbook.logic.LogicManager;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.person.Email;
import seedu.tuitionbook.storage.GuardianLinkRepair;
import seedu.tuitionbook.storage.JsonTuitionBookStorage;
import seedu.tuitionbook.storage.JsonUserPrefsStorage;
import seedu.tuitionbook.storage.StorageManager;

/**
 * Tests the dialogs visible when the application starts with a contact-data notice.
 */
public class UiManagerStartupTest {

    private static final Rectangle2D COMPACT_VISUAL_BOUNDS = new Rectangle2D(0, 0, 853, 480);
    private static final double LAYOUT_TOLERANCE = 2;
    private static final double EXPECTED_UI_SCALE = 1.5;
    private static final String UI_SCALE_REPORT_DIRECTORY_ENV = "TUITIONBOOK_UI_SCALE_REPORT_DIR";
    private static final String INVALID_JSON_GUIDANCE = "The data file is not in a format TuitionBook can read. "
            + "Check the data file for mistakes such as a missing comma, quote or bracket, and save it as "
            + "UTF-8 text before trying again.";
    private static final String PERMISSION_GUIDANCE = "TuitionBook did not have permission to read the data file. "
            + "Restore read permission on the data file before trying again.";
    private static final String FILE_SYSTEM_GUIDANCE = "The data file could not be read because of a file-system "
            + "error. Check access to the data file before trying again.";
    private static final String INVALID_CONTACT_GUIDANCE = "The file contains invalid contact data. "
            + "Contact 3 in the file: Missing \"name\" value.";
    private static final String LONG_EMAIL_VALIDATION_GUIDANCE = "The file contains invalid contact data. "
            + "Contact 9999 in the file (\"" + "A".repeat(100) + "\"): "
            + Email.MESSAGE_INVALID_NAME_CHARACTERS;

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
                "Your contacts are safe in a backup", "Your data file could not be read.",
                "warning");
    }

    @Test
    public void permissionLoadFailure_showsPermissionRecoveryAction() throws Exception {
        Path backupPath = testFolder.resolve("contacts.json.backup");
        assertStartupDialog(StartupNotice.loadFailedBackedUp(backupPath, PERMISSION_GUIDANCE),
                "Contact data could not be loaded", "Your contacts are safe in a backup", PERMISSION_GUIDANCE,
                "warning");
    }

    @Test
    public void invalidJsonLoadFailure_showsJsonRecoveryAction() throws Exception {
        Path backupPath = testFolder.resolve("contacts.json.backup");
        assertStartupDialog(StartupNotice.loadFailedBackedUp(backupPath, INVALID_JSON_GUIDANCE),
                "Contact data could not be loaded", "Your contacts are safe in a backup", INVALID_JSON_GUIDANCE,
                "warning");
    }

    @Test
    public void invalidContactLoadFailure_showsSpecificProblemInMainContent() throws Exception {
        Path backupPath = testFolder.resolve("contacts.json.backup");
        assertStartupDialog(StartupNotice.loadFailedBackedUp(backupPath, INVALID_CONTACT_GUIDANCE),
                "Contact data could not be loaded", "Your contacts are safe in a backup", INVALID_CONTACT_GUIDANCE,
                "warning");
    }

    @Test
    public void fileSystemLoadFailure_showsFileSystemRecoveryAction() throws Exception {
        Path backupPath = testFolder.resolve("contacts.json.backup");
        assertStartupDialog(StartupNotice.loadFailedBackedUp(backupPath, FILE_SYSTEM_GUIDANCE),
                "Contact data could not be loaded", "Your contacts are safe in a backup", FILE_SYSTEM_GUIDANCE,
                "warning");
    }

    @Test
    public void loadFailure_longBackupPath_keepsFullPathInSeparateParagraph() throws Exception {
        Path backupPath = testFolder.resolve("a".repeat(60)).resolve("b".repeat(60))
                .resolve("contacts.json.backup.12");
        assertStartupDialog(StartupNotice.loadFailedBackedUp(backupPath, PERMISSION_GUIDANCE),
                "Contact data could not be loaded", "Your contacts are safe in a backup", PERMISSION_GUIDANCE,
                "warning");
    }

    @Test
    public void loadFailure_backupFailureShowsWarningWithBlockedSavingAction() throws Exception {
        assertStartupDialog(StartupNotice.loadFailedBackupFailed(), "Contact data could not be loaded",
                "Saving is paused to protect your data file", "Your data file could not be read.",
                "warning");
    }

    @Test
    public void loadFailure_backupFailureWithGuidance_showsGuidanceInDialogAndResult() throws Exception {
        String guidance = "Correct the file before trying again.";
        assertStartupDialog(StartupNotice.loadFailedBackupFailed(guidance),
                "Contact data could not be loaded",
                "Saving is paused to protect your data file", guidance,
                "warning");
    }

    @Test
    public void invalidContactLoadFailure_backupFailureShowsProblemAndDisabledSaving() throws Exception {
        assertStartupDialog(StartupNotice.loadFailedBackupFailed(INVALID_CONTACT_GUIDANCE),
                "Contact data could not be loaded", "Saving is paused to protect your data file",
                INVALID_CONTACT_GUIDANCE,
                "warning");
    }

    @Test
    public void permissionLoadFailure_backupFailureShowsPermissionRecoveryAction() throws Exception {
        assertStartupDialog(StartupNotice.loadFailedBackupFailed(PERMISSION_GUIDANCE),
                "Contact data could not be loaded",
                "Saving is paused to protect your data file", PERMISSION_GUIDANCE,
                "warning");
    }

    @Test
    public void backedUpLoadFailure_expandedDetailsFitCompactVisualBounds() throws Exception {
        Path longDataPath = longDataFilePath();
        Path longBackupPath = longDataPath.resolveSibling(longDataPath.getFileName() + ".backup.123");
        assertExpandedLoadFailureFitsCompactVisualBounds(longDataPath,
                StartupNotice.loadFailedBackedUp(longBackupPath, LONG_EMAIL_VALIDATION_GUIDANCE),
                "successful-backup");
    }

    @Test
    public void backupFailedLoadFailure_expandedDetailsFitCompactVisualBounds() throws Exception {
        assertExpandedLoadFailureFitsCompactVisualBounds(longDataFilePath(),
                StartupNotice.loadFailedBackupFailed(LONG_EMAIL_VALIDATION_GUIDANCE),
                "backup-failure");
    }

    @Test
    public void fitLoadFailureDialogWithin_beforeLayout_appliesBoundsFromTargetWidth() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            ScrollPane scroller = new ScrollPane(new VBox(new Label("1. Close TuitionBook.")));
            TitledPane recoveryDetails = new TitledPane("Recovery details", scroller);
            recoveryDetails.setId("recoveryDetails");
            DialogPane pane = new DialogPane();
            pane.setContent(new VBox(new Label("cause"), recoveryDetails));
            pane.getButtonTypes().add(ButtonType.OK);
            Stage stage = new Stage();
            stage.setScene(new Scene(pane));
            stage.setWidth(1400);
            stage.setHeight(400);

            // Unshown, so neither the scene nor the scroller viewport has a size yet.
            UiManager.fitLoadFailureDialogWithin(stage, new Rectangle2D(0, 0, 1024, 720));

            assertEquals(1024, stage.getMaxWidth());
            assertEquals(720, stage.getMaxHeight());
            assertEquals(480, stage.getMinWidth());
            assertTrue(stage.getWidth() <= 1024);
            assertTrue(stage.getHeight() <= 720);
            assertTrue(stage.getMinHeight() >= 230);
        });
    }

    @Test
    public void fitLoadFailureDialogWithin_syntheticCompactBounds_fitsDeterministically() throws Exception {
        AtomicReference<Stage> owner = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch inspected = new CountDownLatch(1);
        Path backupPath = longDataFilePath().resolveSibling("tuitionbook.json.backup.123");

        Platform.runLater(() -> Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                owner.set(stage);
                new UiManager(logic(), longDataFilePath(), Optional.of(
                        StartupNotice.loadFailedBackedUp(backupPath, LONG_EMAIL_VALIDATION_GUIDANCE))).start(stage);
            } catch (Throwable throwable) {
                failure.set(throwable);
            }
        }));
        Platform.runLater(() -> Platform.runLater(() -> {
            try {
                DialogPane pane = findDialogPane();
                Stage dialogStage = (Stage) pane.getScene().getWindow();
                TitledPane recoveryDetails = (TitledPane) pane.lookup("#recoveryDetails");
                recoveryDetails.setExpanded(true);
                pane.applyCss();
                pane.layout();
                UiManager.fitLoadFailureDialogWithin(dialogStage, COMPACT_VISUAL_BOUNDS);
                pane.applyCss();
                pane.layout();
                assertWindowWithin(dialogStage, COMPACT_VISUAL_BOUNDS);
                assertWrappedLabelFullyAllocated((Label) pane.lookup("#loadFailureBody"));
                assertWrappedLabelFullyAllocated((Label) pane.lookup("#loadFailureOutcome"));
                pane.getScene().getWindow().hide();
            } catch (Throwable throwable) {
                failure.set(throwable);
            } finally {
                inspected.countDown();
            }
        }));

        assertTrue(inspected.await(10, TimeUnit.SECONDS), "Synthetic compact dialog was not inspected in time");
        try {
            if (failure.get() != null) {
                throw new AssertionError(failure.get());
            }
        } finally {
            if (owner.get() != null) {
                JavaFxTestUtil.runOnFxThread(owner.get()::close);
            }
        }
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
        Platform.runLater(() -> Platform.runLater(() -> {
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
        }));

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
        assertEquals(javafx.scene.layout.Region.USE_PREF_SIZE, body.getMinHeight());
        assertTrue(body.getText().contains(expectedCause));
        assertTrue(body.getText().contains("started with an empty contact list"));
        assertFalse(body.getFont().getStyle().toLowerCase().contains("bold"));
        assertTrue(outcome.isWrapText());
        assertEquals(javafx.scene.layout.Region.USE_PREF_SIZE, outcome.getMinHeight());
        assertEquals("Recovery details", recoveryDetails.getText());
        assertFalse(recoveryDetails.isExpanded());
        assertTrue(recoveryDetails.getContent() instanceof ScrollPane);
        ScrollPane detailsScroller = (ScrollPane) recoveryDetails.getContent();
        assertTrue(detailsScroller.isFitToWidth());
        assertTrue(recoverySteps.isWrapText());
        assertTrue(recoverySteps.getText().startsWith("1. "));
        assertNull(pane.lookup("#loadFailureDetails"));

        Path displayedDataFilePath = configuredDataFilePath.toAbsolutePath().normalize();
        if (notice.type() == StartupNotice.Type.LOAD_FAILED_BACKED_UP) {
            TextField backupPath = (TextField) pane.lookup("#backupFilePath");
            TextField newDataPath = (TextField) pane.lookup("#newDataFilePath");
            assertReadOnlySelectablePath(backupPath,
                    notice.malformedFileBackupPath().toAbsolutePath().normalize());
            assertReadOnlySelectablePath(newDataPath, displayedDataFilePath);
            assertEquals("Backup file", backupPath.getAccessibleText());
            assertEquals("New data file", newDataPath.getAccessibleText());
            assertTrue(outcome.getText().contains("preserved unchanged"));
            assertTrue(outcome.getText().contains("cannot be merged automatically"));
            assertTrue(recoverySteps.getText().contains(
                    "2. If there is a new data file, copy it somewhere safe."));
            assertTrue(recoverySteps.getText().contains(
                    "1. Fix the problem described above in the backup file."));
            assertTrue(recoverySteps.getText().contains(
                    "5. Either enter reload while TuitionBook remains open, or close and restart TuitionBook."));
            assertTrue(recoverySteps.getText().contains(
                    "6. Re-enter contacts that existed only in the separately saved new file as needed."));
            assertFalse(body.getText().contains(displayedDataFilePath.toString()));
            assertFalse(outcome.getText().contains(displayedDataFilePath.toString()));
            assertFalse(recoverySteps.getText().contains(displayedDataFilePath.toString()));
        } else {
            TextField originalPath = (TextField) pane.lookup("#originalFilePath");
            assertReadOnlySelectablePath(originalPath, displayedDataFilePath);
            assertEquals("Original file remains at", originalPath.getAccessibleText());
            assertTrue(outcome.getText().contains("Saving is disabled"));
            assertTrue(recoverySteps.getText().contains("Correct or move the original data file"));
            assertTrue(recoverySteps.getText().contains("If TuitionBook remains open, enter reload"));
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

    private void assertExpandedLoadFailureFitsCompactVisualBounds(Path configuredDataFilePath,
                                                                  StartupNotice notice,
                                                                  String artifactPrefix) throws Exception {
        AtomicReference<Stage> owner = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch inspected = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                owner.set(stage);
                new UiManager(logic(), configuredDataFilePath, Optional.of(notice)).start(stage);
            } catch (Throwable throwable) {
                failure.set(throwable);
            }
        });
        Platform.runLater(() -> Platform.runLater(() -> {
            try {
                DialogPane pane = findDialogPane();
                Stage dialogStage = (Stage) pane.getScene().getWindow();
                TitledPane recoveryDetails = (TitledPane) pane.lookup("#recoveryDetails");
                ScrollPane detailsScroller = (ScrollPane) recoveryDetails.getContent();
                Rectangle2D verificationBounds = Screen.getPrimary().getVisualBounds();

                runAfterDialogSettles(dialogStage, 30, () -> {
                    try {
                        pane.applyCss();
                        pane.layout();
                        assertActualDialogState(pane, dialogStage, recoveryDetails, detailsScroller,
                                verificationBounds, false);
                        if (isScaleVerificationEnabled()) {
                            assertScaledDisplayEnvironment(dialogStage);
                            recordScaleMeasurements(pane, dialogStage, recoveryDetails, detailsScroller,
                                    artifactPrefix, "collapsed");
                            snapshotDialog(pane, artifactPrefix + "-collapsed.png");
                        }

                        recoveryDetails.setExpanded(true);
                        // The production expansion listener refits the dialog asynchronously. Wait until that
                        // correction lands, so this user-path test fails if the listener is removed or broken.
                        runAfterDialogSettles(dialogStage, 30, () ->
                                inspectExpandedAndResizedDialog(pane, dialogStage, recoveryDetails,
                                        detailsScroller, verificationBounds, notice, artifactPrefix,
                                        failure, inspected));
                    } catch (Throwable throwable) {
                        failure.set(throwable);
                        inspected.countDown();
                    }
                });
            } catch (Throwable throwable) {
                failure.set(throwable);
                inspected.countDown();
            }
        }));

        assertTrue(inspected.await(10, TimeUnit.SECONDS), "Compact startup dialog was not inspected in time");
        try {
            if (failure.get() != null) {
                throw new AssertionError(failure.get());
            }
        } finally {
            if (owner.get() != null) {
                JavaFxTestUtil.runOnFxThread(owner.get()::close);
            }
        }
    }

    private void inspectExpandedAndResizedDialog(DialogPane pane, Stage dialogStage,
                                                  TitledPane recoveryDetails, ScrollPane detailsScroller,
                                                  Rectangle2D verificationBounds, StartupNotice notice,
                                                  String artifactPrefix, AtomicReference<Throwable> failure,
                                                  CountDownLatch inspected) {
        try {
            pane.applyCss();
            pane.layout();
            assertActualDialogState(pane, dialogStage, recoveryDetails, detailsScroller,
                    verificationBounds, true);
            assertContentAwareRecoveryViewport(detailsScroller);
            if (isScaleVerificationEnabled()) {
                recordScaleMeasurements(pane, dialogStage, recoveryDetails, detailsScroller,
                        artifactPrefix, "expanded");
                snapshotDialog(pane, artifactPrefix + "-expanded.png");
            }

            dialogStage.setWidth(536);
            dialogStage.setHeight(420);
            // The dialog refits itself after a programmatic shrink, but the correction and the
            // scene's catch-up to the window size take a few pulses on some platforms.
            runAfterDialogSettles(dialogStage, 30, () -> {
                try {
                    pane.applyCss();
                    pane.layout();
                    assertActualDialogState(pane, dialogStage, recoveryDetails, detailsScroller,
                            verificationBounds, true);
                    assertContentAwareRecoveryViewport(detailsScroller);
                    assertTrue(dialogStage.getMinWidth() <= verificationBounds.getWidth());
                    assertTrue(dialogStage.getMinHeight() <= verificationBounds.getHeight());

                    if (notice.type() == StartupNotice.Type.LOAD_FAILED_BACKED_UP) {
                        assertPathReachable(detailsScroller, (TextField) pane.lookup("#backupFilePath"));
                        assertPathReachable(detailsScroller, (TextField) pane.lookup("#newDataFilePath"));
                    } else {
                        assertPathReachable(detailsScroller, (TextField) pane.lookup("#originalFilePath"));
                    }
                    if (isScaleVerificationEnabled()) {
                        recordScaleMeasurements(pane, dialogStage, recoveryDetails, detailsScroller,
                                artifactPrefix, "resized-smaller");
                    }
                    pane.getScene().getWindow().hide();
                } catch (Throwable throwable) {
                    failure.set(throwable);
                } finally {
                    inspected.countDown();
                }
            });
        } catch (Throwable throwable) {
            failure.set(throwable);
            inspected.countDown();
        }
    }

    private void assertContentAwareRecoveryViewport(ScrollPane detailsScroller) {
        assertTrue(detailsScroller.getViewportBounds().getWidth() > 0);
        assertTrue(detailsScroller.getViewportBounds().getHeight() >= 50);
        assertTrue(detailsScroller.getViewportBounds().getHeight()
                <= detailsScroller.getContent().getLayoutBounds().getHeight() + LAYOUT_TOLERANCE);
        assertEquals(ScrollPane.ScrollBarPolicy.NEVER, detailsScroller.getHbarPolicy());
        assertTrue(detailsScroller.getContent().getLayoutBounds().getWidth()
                <= detailsScroller.getViewportBounds().getWidth() + LAYOUT_TOLERANCE);
    }

    private void assertScaledDisplayEnvironment(Stage dialogStage) {
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        String requestedScale = System.getProperty("glass.gtk.uiScale");
        assertEquals(EXPECTED_UI_SCALE, Double.parseDouble(requestedScale), 0.01);

        boolean outputScaleApplied = Math.abs(dialogStage.getOutputScaleX() - EXPECTED_UI_SCALE) < 0.1
                && Math.abs(dialogStage.getOutputScaleY() - EXPECTED_UI_SCALE) < 0.1;
        boolean logicalScreenApplied = Math.abs(screenBounds.getWidth() - 1280 / EXPECTED_UI_SCALE) < 10
                && Math.abs(screenBounds.getHeight() - 720 / EXPECTED_UI_SCALE) < 10;
        boolean logicalVisualApplied = Math.abs(visualBounds.getWidth() - 1280 / EXPECTED_UI_SCALE) < 10
                && Math.abs(visualBounds.getHeight() - 720 / EXPECTED_UI_SCALE) < 10;
        assertTrue(outputScaleApplied || logicalScreenApplied || logicalVisualApplied, () ->
                "Requested 1.5 scale was not reflected by output scale or logical screen bounds: outputScale="
                        + dialogStage.getOutputScaleX() + "x" + dialogStage.getOutputScaleY()
                        + ", screen=" + format(screenBounds) + ", visual=" + format(visualBounds));
    }

    private void assertActualDialogState(DialogPane pane, Stage dialogStage, TitledPane recoveryDetails,
                                         ScrollPane detailsScroller, Rectangle2D visualBounds, boolean expanded) {
        assertWindowWithin(dialogStage, visualBounds);
        Label body = (Label) pane.lookup("#loadFailureBody");
        Label outcome = (Label) pane.lookup("#loadFailureOutcome");
        Button okButton = (Button) pane.lookupButton(ButtonType.OK);
        assertTrue(body.isVisible());
        assertTrue(outcome.isVisible());
        assertNodeWithinScene(body, pane);
        assertNodeWithinScene(outcome, pane);
        assertWrappedLabelFullyAllocated(body);
        assertWrappedLabelFullyAllocated(outcome);
        assertTrue(okButton.isVisible());
        assertTrue(okButton.isFocusTraversable());
        assertNodeWithinScene(okButton, pane);
        assertFalse(isDescendantOf(okButton, detailsScroller));
        okButton.requestFocus();
        assertEquals(okButton, pane.getScene().getFocusOwner());
        assertEquals(expanded, recoveryDetails.isExpanded());
        if (expanded) {
            assertTrue(detailsScroller.isVisible());
            assertTrue(detailsScroller.getViewportBounds().getWidth() > 0);
            assertTrue(detailsScroller.getViewportBounds().getHeight() >= 50);
        }
    }

    private void assertWrappedLabelFullyAllocated(Label label) {
        double preferredWrappedHeight = label.prefHeight(label.getWidth());
        assertTrue(label.getHeight() + LAYOUT_TOLERANCE >= preferredWrappedHeight, () ->
                label.getId() + " is vertically clipped: allocated=" + label.getHeight()
                        + ", preferred=" + preferredWrappedHeight + ", width=" + label.getWidth());
    }

    private void recordScaleMeasurements(DialogPane pane, Stage dialogStage, TitledPane recoveryDetails,
                                         ScrollPane detailsScroller, String artifactPrefix, String state)
            throws IOException {
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        Button okButton = (Button) pane.lookupButton(ButtonType.OK);
        Label body = (Label) pane.lookup("#loadFailureBody");
        Label outcome = (Label) pane.lookup("#loadFailureOutcome");
        Node viewport = detailsScroller.lookup(".viewport");
        String report = String.format(
                "%s %s%n"
                        + "glass.gtk.uiScale=%s%n"
                        + "screen.bounds=%s%n"
                        + "screen.visualBounds=%s%n"
                        + "dialog.stage=[x=%.2f,y=%.2f,width=%.2f,height=%.2f]%n"
                        + "dialog.outputScale=[x=%.2f,y=%.2f]%n"
                        + "dialog.scene=[width=%.2f,height=%.2f]%n"
                        + "body=[allocatedHeight=%.2f,preferredWrappedHeight=%.2f,width=%.2f]%n"
                        + "outcome=[allocatedHeight=%.2f,preferredWrappedHeight=%.2f,width=%.2f]%n"
                        + "okButton.screenBounds=%s%n"
                        + "recoveryDetails.screenBounds=%s%n"
                        + "recoveryViewport.screenBounds=%s%n%n",
                artifactPrefix, state,
                System.getProperty("glass.gtk.uiScale"),
                format(screenBounds),
                format(visualBounds),
                dialogStage.getX(), dialogStage.getY(), dialogStage.getWidth(), dialogStage.getHeight(),
                dialogStage.getOutputScaleX(), dialogStage.getOutputScaleY(),
                pane.getScene().getWidth(), pane.getScene().getHeight(),
                body.getHeight(), body.prefHeight(body.getWidth()), body.getWidth(),
                outcome.getHeight(), outcome.prefHeight(outcome.getWidth()), outcome.getWidth(),
                format(okButton.localToScreen(okButton.getBoundsInLocal())),
                format(recoveryDetails.localToScreen(recoveryDetails.getBoundsInLocal())),
                viewport == null ? "not-visible" : format(viewport.localToScreen(viewport.getBoundsInLocal())));
        Files.createDirectories(scaleReportDirectory());
        Files.writeString(scaleReportDirectory().resolve("ui-scale-metrics.txt"), report,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        System.out.print(report);
    }

    private void snapshotDialog(DialogPane pane, String fileName) throws IOException {
        WritableImage snapshot = pane.getScene().snapshot(null);
        int width = (int) snapshot.getWidth();
        int height = (int) snapshot.getHeight();
        int[] pixels = new int[width * height];
        snapshot.getPixelReader().getPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(),
                pixels, 0, width);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        Files.createDirectories(scaleReportDirectory());
        ImageIO.write(image, "png", scaleReportDirectory().resolve(fileName).toFile());
    }

    private boolean isScaleVerificationEnabled() {
        String reportDirectory = System.getenv(UI_SCALE_REPORT_DIRECTORY_ENV);
        return reportDirectory != null && !reportDirectory.isBlank();
    }

    private Path scaleReportDirectory() {
        return Path.of(System.getenv(UI_SCALE_REPORT_DIRECTORY_ENV));
    }

    private String format(Rectangle2D bounds) {
        return String.format("[minX=%.2f,minY=%.2f,width=%.2f,height=%.2f]",
                bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
    }

    private String format(Bounds bounds) {
        if (bounds == null) {
            return "not-visible";
        }
        return String.format("[minX=%.2f,minY=%.2f,width=%.2f,height=%.2f]",
                bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
    }

    /**
     * Runs {@code inspection} once the dialog's self-correction has settled: the stage respects its
     * minimum height and the scene has caught up with the stage size. Platforms apply programmatic
     * window changes asynchronously, so the dialog's refit can take a few pulses to land. Gives up
     * after {@code remainingAttempts} pulses and runs the inspection anyway so assertions still report.
     */
    private void runAfterDialogSettles(Stage stage, int remainingAttempts, Runnable inspection) {
        Platform.runLater(() -> {
            DialogPane pane = (DialogPane) stage.getScene().getRoot();
            double decorationWidth = rememberedDecoration(stage, "loadFailureDecorationWidth");
            double decorationHeight = rememberedDecoration(stage, "loadFailureDecorationHeight");
            boolean settled = stage.getHeight() >= stage.getMinHeight() - LAYOUT_TOLERANCE
                    && Math.abs(stage.getScene().getWidth() + decorationWidth - stage.getWidth())
                            <= LAYOUT_TOLERANCE
                    && Math.abs(stage.getScene().getHeight() + decorationHeight - stage.getHeight())
                            <= LAYOUT_TOLERANCE
                    && pane.minHeight(stage.getScene().getWidth())
                            <= stage.getScene().getHeight() + LAYOUT_TOLERANCE;
            if (!settled && remainingAttempts > 0) {
                // Space the retries out: chained runLater calls can all run within the same frame,
                // before the platform delivers the window's new size to the scene.
                PauseTransition pause = new PauseTransition(Duration.millis(25));
                pause.setOnFinished(event -> runAfterDialogSettles(stage, remainingAttempts - 1, inspection));
                pause.play();
                return;
            }
            inspection.run();
        });
    }

    private double rememberedDecoration(Stage stage, String key) {
        Object value = stage.getProperties().get(key);
        return value instanceof Double decoration ? decoration : 0;
    }

    private void assertWindowWithin(Stage stage, Rectangle2D visualBounds) {
        assertTrue(stage.getX() >= visualBounds.getMinX() - LAYOUT_TOLERANCE, () ->
                "window left edge outside " + describeWindowAgainstBounds(stage, visualBounds));
        assertTrue(stage.getY() >= visualBounds.getMinY() - LAYOUT_TOLERANCE, () ->
                "window top edge outside " + describeWindowAgainstBounds(stage, visualBounds));
        assertTrue(stage.getX() + stage.getWidth() <= visualBounds.getMaxX() + LAYOUT_TOLERANCE, () ->
                "window right edge outside " + describeWindowAgainstBounds(stage, visualBounds));
        assertTrue(stage.getY() + stage.getHeight() <= visualBounds.getMaxY() + LAYOUT_TOLERANCE, () ->
                "window bottom edge outside " + describeWindowAgainstBounds(stage, visualBounds));
    }

    private String describeWindowAgainstBounds(Stage stage, Rectangle2D visualBounds) {
        DialogPane pane = (DialogPane) stage.getScene().getRoot();
        TitledPane recoveryDetails = (TitledPane) pane.lookup("#recoveryDetails");
        ScrollPane scroller = (ScrollPane) recoveryDetails.getContent();
        return String.format("visual bounds %s: stage=[x=%.2f,y=%.2f,width=%.2f,height=%.2f,"
                        + "minHeight=%.2f,maxHeight=%.2f], scene=[width=%.2f,height=%.2f], "
                        + "decorations=[width=%s,height=%s], panePref=[%.2f@sceneWidth], expanded=%s, "
                        + "scroller=[min=%.2f,pref=%.2f,max=%.2f,height=%.2f]",
                format(visualBounds), stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight(),
                stage.getMinHeight(), stage.getMaxHeight(),
                stage.getScene().getWidth(), stage.getScene().getHeight(),
                stage.getProperties().get("loadFailureDecorationWidth"),
                stage.getProperties().get("loadFailureDecorationHeight"),
                pane.prefHeight(stage.getScene().getWidth()), recoveryDetails.isExpanded(),
                scroller.getMinHeight(), scroller.getPrefViewportHeight(), scroller.getMaxHeight(),
                scroller.getHeight());
    }

    private void assertNodeWithinScene(Node node, DialogPane pane) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        assertTrue(bounds.getMinX() >= -LAYOUT_TOLERANCE, () -> node.getId() + " starts outside scene: " + bounds);
        assertTrue(bounds.getMinY() >= -LAYOUT_TOLERANCE, () -> node.getId() + " starts outside scene: " + bounds);
        assertTrue(bounds.getMaxX() <= pane.getScene().getWidth() + LAYOUT_TOLERANCE, () ->
                node.getId() + " exceeds scene width: " + bounds);
        assertTrue(bounds.getMaxY() <= pane.getScene().getHeight() + LAYOUT_TOLERANCE, () ->
                node.getId() + " exceeds scene height: " + bounds);
    }

    private void assertPathReachable(ScrollPane scroller, TextField pathField) {
        assertFalse(pathField.isEditable());
        assertTrue(pathField.isFocusTraversable());
        pathField.selectAll();
        assertEquals(pathField.getText(), pathField.getSelectedText());
        assertTrue(pathField.getWidth() <= scroller.getViewportBounds().getWidth() + LAYOUT_TOLERANCE);

        Node content = scroller.getContent();
        Bounds pathInContent = content.sceneToLocal(pathField.localToScene(pathField.getBoundsInLocal()));
        double scrollableHeight = content.getLayoutBounds().getHeight() - scroller.getViewportBounds().getHeight();
        double targetOffset = Math.max(0,
                Math.min(scrollableHeight, pathInContent.getMaxY() - scroller.getViewportBounds().getHeight()));
        scroller.setVvalue(scrollableHeight <= 0 ? 0 : targetOffset / scrollableHeight);
        scroller.layout();

        Node viewport = scroller.lookup(".viewport");
        Bounds pathOnScreen = pathField.localToScene(pathField.getBoundsInLocal());
        Bounds viewportOnScreen = viewport.localToScene(viewport.getBoundsInLocal());
        assertTrue(pathOnScreen.intersects(viewportOnScreen));
    }

    private boolean isDescendantOf(Node node, Node ancestor) {
        for (Node current = node.getParent(); current != null; current = current.getParent()) {
            if (current == ancestor) {
                return true;
            }
        }
        return false;
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
                ? "Your data file could not be read."
                : notice.loadFailureGuidance();
        if (notice.type() == StartupNotice.Type.LOAD_FAILED_BACKED_UP) {
            return description + "\nBackup file: "
                    + notice.malformedFileBackupPath().toAbsolutePath().normalize() + ".\nNew contacts save to "
                    + displayedDataFilePath
                    + " and cannot be merged automatically with the backup file.\nFix the problem in the backup "
                    + "file. If a new data file exists, copy it somewhere safe, then replace it with the corrected "
                    + "backup. Enter reload while TuitionBook remains open, or close and restart TuitionBook. "
                    + "Re-enter contacts that existed only in the separately saved new file as needed.";
        }
        return description + "\nThe original file could not be backed up and remains at " + displayedDataFilePath
                + ".\nSaving is disabled. Correct or move the original file, then enter reload while TuitionBook "
                + "remains open, or close and restart TuitionBook.";
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

    private Path longDataFilePath() {
        return testFolder.resolve("a-very-long-folder-name-for-contact-data-that-must-not-widen-the-dialog")
                .resolve("another-long-folder-name-used-to-check-horizontal-overflow")
                .resolve("tuitionbook.json");
    }
}
