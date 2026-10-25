package seedu.tuitionbook.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.tuitionbook.logic.Logic;
import seedu.tuitionbook.logic.LogicManager;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.storage.JsonTuitionBookStorage;
import seedu.tuitionbook.storage.JsonUserPrefsStorage;
import seedu.tuitionbook.storage.StorageManager;

/**
 * Tests the dialogs visible when the application starts with a contact-data notice.
 */
public class UiManagerStartupTest {

    @TempDir
    public Path testFolder;

    @BeforeAll
    public static void initializeJavaFx() {
        JavaFxTestUtil.initialize();
    }

    @Test
    public void missingFile_showsInformationDialog() throws Exception {
        assertStartupDialog(StartupNotice.missingDataFile(), "No data file found",
                "TuitionBook started with an empty contact list",
                "No contact data was found at " + dataFilePath() + ". Add contacts to get started.",
                "information");
    }

    @Test
    public void loadFailure_showsWarningWithRecoveryAction() throws Exception {
        Path backupPath = testFolder.resolve("contacts.json.backup.1");
        assertStartupDialog(StartupNotice.loadFailedBackedUp(backupPath), "Contact data could not be loaded",
                "TuitionBook started with an empty contact list",
                "Your data file could not be read, so it was moved unchanged to " + backupPath
                        + ". Your original data is not lost. TuitionBook will save new contacts to " + dataFilePath()
                        + ". To restore the original data, fix the JSON in the backup, rename it to " + dataFilePath()
                        + " (replacing the new file), then restart TuitionBook. Contacts added after recovery "
                        + "are in the new file and must be re-entered; the files cannot be merged automatically.",
                "warning");
    }

    @Test
    public void loadFailure_backupFailureShowsWarningWithBlockedSavingAction() throws Exception {
        assertStartupDialog(StartupNotice.loadFailedBackupFailed(), "Contact data could not be loaded",
                "TuitionBook started with an empty contact list, but saving is disabled",
                "Your data file could not be read and could not be moved to a backup. It remains at " + dataFilePath()
                        + ". To protect it, TuitionBook cannot save contacts. Correct or move this file, then restart "
                        + "TuitionBook.",
                "warning");
    }

    @Test
    public void oneRepairedLink_showsSingularWarning() throws Exception {
        assertStartupDialog(StartupNotice.guardianLinksRepaired(1), "Guardian links repaired",
                "1 guardian link was repaired",
                "All valid contacts were retained. Review invalid guardianId values in " + dataFilePath()
                        + " before entering a contact-changing command; the next save removes invalid links.",
                "warning");
    }

    @Test
    public void multipleRepairedLinks_showsPluralWarning() throws Exception {
        assertStartupDialog(StartupNotice.guardianLinksRepaired(2), "Guardian links repaired",
                "2 guardian links were repaired",
                "All valid contacts were retained. Review invalid guardianId values in " + dataFilePath()
                        + " before entering a contact-changing command; the next save removes invalid links.",
                "warning");
    }

    @Test
    public void repairedLinks_relativeDataFilePath_showsAbsolutePathInWarning() throws Exception {
        Path relativeDataFilePath = Path.of("data", "contacts.json");
        assertStartupDialog(relativeDataFilePath, StartupNotice.guardianLinksRepaired(1), "Guardian links repaired",
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
                    commandTextField(owner).isFocused()));
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
                assertEquals(content, pane.getContentText());
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
            assertTrue(JavaFxTestUtil.waitForFxCondition(() -> commandTextField(owner.get()).isFocused()),
                    "Command field did not receive focus after the startup dialog closed");
        } finally {
            if (owner.get() != null) {
                JavaFxTestUtil.runOnFxThread(owner.get()::close);
            }
        }
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
        if (notice.type() == StartupNotice.Type.LOAD_FAILED_BACKED_UP) {
            return "Data file was moved unchanged to "
                    + notice.malformedFileBackupPath().toAbsolutePath().normalize() + ".\n"
                    + "New contacts save to " + displayedDataFilePath
                    + ". Fix the backup JSON and replace this file to restore it.\n"
                    + "Restart TuitionBook; contacts added after recovery must be re-entered.";
        }
        return "Data file could not be backed up and remains at " + displayedDataFilePath + ".\n"
                + "Saving is disabled. Correct or move the file, then restart TuitionBook.";
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
