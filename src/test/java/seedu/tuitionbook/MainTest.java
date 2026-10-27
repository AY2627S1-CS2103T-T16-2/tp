package seedu.tuitionbook;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.tuitionbook.commons.core.ApplicationInstanceGuard;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.ReadOnlyUserPrefs;
import seedu.tuitionbook.storage.JsonTuitionBookStorage;
import seedu.tuitionbook.storage.JsonUserPrefsStorage;
import seedu.tuitionbook.storage.StorageManager;
import seedu.tuitionbook.ui.JavaFxTestUtil;

public class MainTest {

    @TempDir
    public Path testFolder;

    @Test
    public void start_acquiresGuardAndLaunchesMainAppWithArguments() throws Exception {
        String[] arguments = {"first", "second"};
        AtomicReference<Class<?>> launchedClass = new AtomicReference<>();
        AtomicReference<String[]> launchedArguments = new AtomicReference<>();

        Main.start(testFolder, arguments, (applicationClass, args) -> {
            launchedClass.set(applicationClass);
            launchedArguments.set(args);
        }, (title, message) -> {
            throw new AssertionError("Acquisition should have succeeded");
        });

        assertEquals(MainApp.class, launchedClass.get());
        assertArrayEquals(arguments, launchedArguments.get());
        try (ApplicationInstanceGuard ignored = ApplicationInstanceGuard.acquire(testFolder)) {
            assertTrue(Files.exists(testFolder.resolve(ApplicationInstanceGuard.LOCK_FILE_NAME)));
        }
    }

    @Test
    public void launchApplication_delegatesToJavaFxLauncherWithMainAppAndArguments() {
        String[] arguments = {"first", "second"};
        AtomicReference<Class<?>> launchedClass = new AtomicReference<>();
        AtomicReference<String[]> launchedArguments = new AtomicReference<>();

        Main.launchApplication(arguments, (applicationClass, args) -> {
            launchedClass.set(applicationClass);
            launchedArguments.set(args);
        });

        assertEquals(MainApp.class, launchedClass.get());
        assertArrayEquals(arguments, launchedArguments.get());
    }

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

    @Test
    public void runGuardedStartup_guardCloseFails_reportsCleanupFailure() {
        AtomicBoolean startupWasRun = new AtomicBoolean();

        String errorOutput = captureStandardError(() -> Main.runGuardedStartup(() -> {
            throw new IOException("simulated close failure");
        }, () -> startupWasRun.set(true)));

        assertTrue(startupWasRun.get());
        assertTrue(errorOutput.contains("could not release its application-folder lock cleanly"));
        assertTrue(errorOutput.contains("simulated close failure"));
    }

    @Test
    public void showStartupError_platformStartupFails_reportsFallbackError() {
        RuntimeException platformFailure = new IllegalStateException("simulated JavaFX failure");

        String errorOutput = captureStandardError(() -> Main.showStartupError("Title", "Message", runnable -> {
            throw platformFailure;
        }));

        assertTrue(errorOutput.contains("graphical startup error could not be displayed"));
        assertTrue(errorOutput.contains("simulated JavaFX failure"));
    }

    @Test
    public void showStartupError_existingToolkit_reportsFallbackError() {
        JavaFxTestUtil.initialize();

        String errorOutput = captureStandardError(() -> Main.showStartupError("Title", "Message"));

        assertTrue(errorOutput.contains("graphical startup error could not be displayed"));
        assertTrue(errorOutput.contains("IllegalStateException"));
    }

    @Test
    public void showStartupError_validDialog_displaysConfiguredAlertAndExits() throws Exception {
        String title = "Unique startup error title";
        String message = "Unique startup error message";
        AtomicBoolean dialogWasShown = new AtomicBoolean();
        AtomicBoolean platformExitWasRun = new AtomicBoolean();
        AtomicReference<String> observedTitle = new AtomicReference<>();
        AtomicReference<String> observedHeader = new AtomicReference<>();
        AtomicReference<String> observedContent = new AtomicReference<>();
        AtomicBoolean observedResizable = new AtomicBoolean();

        JavaFxTestUtil.runOnFxThread(() -> {
            Platform.runLater(() -> Window.getWindows().stream()
                    .filter(Window::isShowing)
                    .filter(window -> window instanceof Stage && title.equals(((Stage) window).getTitle()))
                    .filter(window -> window.getScene() != null
                            && window.getScene().getRoot() instanceof DialogPane)
                    .findFirst()
                    .ifPresent(window -> {
                        DialogPane dialogPane = (DialogPane) window.getScene().getRoot();
                        dialogWasShown.set(true);
                        observedTitle.set(((Stage) window).getTitle());
                        observedHeader.set(dialogPane.getHeaderText());
                        observedContent.set(dialogPane.getContentText());
                        observedResizable.set(((Stage) window).isResizable());
                        window.hide();
                    }));
            Main.showStartupError(title, message, Runnable::run, () -> platformExitWasRun.set(true));
        });

        assertTrue(dialogWasShown.get());
        assertEquals(title, observedTitle.get());
        assertEquals(title, observedHeader.get());
        assertEquals(message, observedContent.get());
        assertTrue(observedResizable.get());
        assertTrue(platformExitWasRun.get());
    }

    private static String captureStandardError(Runnable action) {
        PrintStream originalError = System.err;
        ByteArrayOutputStream capturedError = new ByteArrayOutputStream();
        try (PrintStream replacementError = new PrintStream(capturedError, true, StandardCharsets.UTF_8)) {
            System.setErr(replacementError);
            action.run();
        } finally {
            System.setErr(originalError);
        }
        return capturedError.toString(StandardCharsets.UTF_8);
    }
}
