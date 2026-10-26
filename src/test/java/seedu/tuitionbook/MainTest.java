package seedu.tuitionbook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.tuitionbook.commons.core.ApplicationInstanceGuard;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.ReadOnlyUserPrefs;
import seedu.tuitionbook.storage.JsonTuitionBookStorage;
import seedu.tuitionbook.storage.JsonUserPrefsStorage;
import seedu.tuitionbook.storage.StorageManager;

public class MainTest {

    @TempDir
    public Path testFolder;

    @Test
    public void runWithInstanceGuard_competingInstance_rejectsBeforeStorageInitialization() throws Exception {
        Path dataFile = Files.createDirectories(testFolder.resolve("data")).resolve("tuitionbook.json");
        Path prefsFile = testFolder.resolve("preferences.json");
        Path logFile = testFolder.resolve("tuitionbook.log");
        String originalData = "{\"persons\":[]}";
        String originalPrefs = "{\"guiSettings\":{\"windowWidth\":800.0}}";
        String originalLog = "existing log";
        Files.writeString(dataFile, originalData);
        Files.writeString(prefsFile, originalPrefs);
        Files.writeString(logFile, originalLog);
        AtomicBoolean startupWasRun = new AtomicBoolean();
        AtomicReference<String> reportedTitle = new AtomicReference<>();
        AtomicReference<String> reportedMessage = new AtomicReference<>();

        try (ApplicationInstanceGuard ignored = ApplicationInstanceGuard.acquire(testFolder)) {
            Main.runWithInstanceGuard(testFolder, () -> {
                startupWasRun.set(true);
                throw new AssertionError("Rejected startup must not initialize storage");
            }, (title, message) -> {
                reportedTitle.set(title);
                reportedMessage.set(message);
            });
        }

        assertFalse(startupWasRun.get());
        assertEquals(Main.ALREADY_RUNNING_TITLE, reportedTitle.get());
        assertTrue(reportedMessage.get().contains("existing window"));
        assertEquals(originalData, Files.readString(dataFile));
        assertEquals(originalPrefs, Files.readString(prefsFile));
        assertEquals(originalLog, Files.readString(logFile));
    }

    @Test
    public void runWithInstanceGuard_acquisitionIoFailure_isNotReportedAsContention() throws Exception {
        Path fileInsteadOfHome = Files.createFile(testFolder.resolve("not-a-directory"));
        AtomicBoolean startupWasRun = new AtomicBoolean();
        AtomicReference<String> reportedTitle = new AtomicReference<>();
        AtomicReference<String> reportedMessage = new AtomicReference<>();

        Main.runWithInstanceGuard(fileInsteadOfHome, () -> startupWasRun.set(true), (title, message) -> {
            reportedTitle.set(title);
            reportedMessage.set(message);
        });

        assertFalse(startupWasRun.get());
        assertEquals(Main.LOCK_FAILED_TITLE, reportedTitle.get());
        assertTrue(reportedMessage.get().contains("permission"));
        assertFalse(reportedMessage.get().contains("already"));
    }

    @Test
    public void runWithInstanceGuard_failedStartup_releasesGuard() throws Exception {
        Runnable failingStartup = () -> {
            throw new IllegalStateException("simulated startup failure");
        };

        assertThrows(IllegalStateException.class, () ->
                Main.runWithInstanceGuard(testFolder, failingStartup, (title, message) -> {
                    throw new AssertionError("Acquisition should have succeeded");
                }));

        try (ApplicationInstanceGuard ignored = ApplicationInstanceGuard.acquire(testFolder)) {
            assertTrue(Files.exists(testFolder.resolve(ApplicationInstanceGuard.LOCK_FILE_NAME)));
        }
    }

    @Test
    public void runWithInstanceGuard_preferenceSaveFailure_releasesGuard() throws Exception {
        MainApp app = new MainApp();
        app.storage = new StorageManager(new JsonTuitionBookStorage(testFolder.resolve("contacts.json")),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")) {
                    @Override
                    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
                        throw new IOException("simulated preference save failure");
                    }
                });
        app.model = new ModelManager();

        Main.runWithInstanceGuard(testFolder, app::stop, (title, message) -> {
            throw new AssertionError("Acquisition should have succeeded");
        });

        try (ApplicationInstanceGuard ignored = ApplicationInstanceGuard.acquire(testFolder)) {
            assertTrue(Files.exists(testFolder.resolve(ApplicationInstanceGuard.LOCK_FILE_NAME)));
        }
    }
}
