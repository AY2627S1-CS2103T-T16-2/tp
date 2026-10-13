package seedu.tuitionbook.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.scene.control.DialogPane;
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
        assertStartupDialog(StartupNotice.loadFailed(), "Contact data could not be loaded",
                "TuitionBook started with an empty contact list",
                "Saving is disabled to protect the existing data file at " + dataFilePath()
                        + ". Correct or move the file, then restart TuitionBook.", "warning");
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
    public void cleanData_showsNoStartupDialog() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            Stage owner = new Stage();
            try {
                new UiManager(logic(), dataFilePath(), Optional.empty()).start(owner);
                assertTrue(owner.isShowing());
                assertNull(findDialogPane());
            } finally {
                owner.close();
            }
        });
    }

    private void assertStartupDialog(StartupNotice notice, String title, String header, String content,
                                     String styleClass) throws Exception {
        AtomicReference<Stage> owner = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch inspected = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                owner.set(stage);
                new UiManager(logic(), dataFilePath(), Optional.of(notice)).start(stage);
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
                pane.getScene().getWindow().hide();
            } catch (Throwable throwable) {
                failure.set(throwable);
            } finally {
                if (owner.get() != null) {
                    owner.get().close();
                }
                inspected.countDown();
            }
        });

        assertTrue(inspected.await(10, TimeUnit.SECONDS), "Startup dialog was not inspected in time");
        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }
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

    private Logic logic() {
        return new LogicManager(new ModelManager(), new StorageManager(
                new JsonTuitionBookStorage(dataFilePath()),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));
    }

    private Path dataFilePath() {
        return testFolder.resolve("contacts.json");
    }
}
