package seedu.tuitionbook.commons.core;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.tuitionbook.commons.core.ApplicationInstanceGuard.AlreadyRunningException;

public class ApplicationInstanceGuardTest {

    private static final Duration PROCESS_TIMEOUT = Duration.ofSeconds(15);

    @TempDir
    public Path testFolder;

    @Test
    public void acquire_firstAcquisition_succeedsAndCreatesLockFile() throws Exception {
        try (ApplicationInstanceGuard ignored = ApplicationInstanceGuard.acquire(testFolder)) {
            assertTrue(Files.isRegularFile(testFolder.resolve(ApplicationInstanceGuard.LOCK_FILE_NAME)));
        }
    }

    @Test
    public void acquire_competingAcquisitionForSameFolder_isRejected() throws Exception {
        try (ApplicationInstanceGuard ignored = ApplicationInstanceGuard.acquire(testFolder)) {
            assertThrows(AlreadyRunningException.class, () -> ApplicationInstanceGuard.acquire(testFolder));
        }
    }

    @Test
    public void acquire_differentFolders_succeedIndependently() throws Exception {
        Path firstHome = Files.createDirectory(testFolder.resolve("first"));
        Path secondHome = Files.createDirectory(testFolder.resolve("second"));

        try (ApplicationInstanceGuard ignoredFirst = ApplicationInstanceGuard.acquire(firstHome);
                ApplicationInstanceGuard ignoredSecond = ApplicationInstanceGuard.acquire(secondHome)) {
            assertTrue(Files.isRegularFile(firstHome.resolve(ApplicationInstanceGuard.LOCK_FILE_NAME)));
            assertTrue(Files.isRegularFile(secondHome.resolve(ApplicationInstanceGuard.LOCK_FILE_NAME)));
        }
    }

    @Test
    public void acquire_afterRelease_succeedsAndCloseIsIdempotent() throws Exception {
        ApplicationInstanceGuard firstGuard = ApplicationInstanceGuard.acquire(testFolder);

        firstGuard.close();
        assertDoesNotThrow(firstGuard::close);

        try (ApplicationInstanceGuard ignored = ApplicationInstanceGuard.acquire(testFolder)) {
            assertTrue(Files.exists(testFolder.resolve(ApplicationInstanceGuard.LOCK_FILE_NAME)));
        }
    }

    @Test
    public void acquire_preExistingUnlockedLockFile_succeedsWithoutDeletingFile() throws Exception {
        Path lockFile = testFolder.resolve(ApplicationInstanceGuard.LOCK_FILE_NAME);
        String originalContents = "left over from an earlier run";
        Files.writeString(lockFile, originalContents);

        try (ApplicationInstanceGuard ignored = ApplicationInstanceGuard.acquire(testFolder)) {
            assertEquals(originalContents, Files.readString(lockFile));
        }

        assertTrue(Files.exists(lockFile));
    }

    @Test
    public void acquire_lockFileCannotBeOpened_reportsIoFailureNotContention() throws Exception {
        Path fileInsteadOfHome = Files.createFile(testFolder.resolve("not-a-directory"));

        assertThrows(IOException.class, () -> ApplicationInstanceGuard.acquire(fileInsteadOfHome));
    }

    @Test
    public void acquire_lockHeldBySeparateJvm_isRejectedUntilProcessReleasesIt() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("separate-jvm-home"));
        Path readySignal = applicationHome.resolve("ready.signal");
        Path releaseSignal = applicationHome.resolve("release.signal");
        Process process;
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            applicationHome.register(watchService, ENTRY_CREATE, ENTRY_MODIFY);
            process = startGuardProcess(applicationHome, readySignal, releaseSignal);
            try {
                awaitFile(readySignal, watchService, process);
                assertThrows(AlreadyRunningException.class, () ->
                        ApplicationInstanceGuard.acquire(applicationHome));

                Files.writeString(releaseSignal, "release");
                assertTrue(process.waitFor(PROCESS_TIMEOUT.toSeconds(), TimeUnit.SECONDS), () ->
                        "Guard process did not exit before the timeout");
                assertEquals(0, process.exitValue(), () -> "Guard process failed. Output:\n"
                        + readProcessOutput(process));

                try (ApplicationInstanceGuard ignored = ApplicationInstanceGuard.acquire(applicationHome)) {
                    assertTrue(Files.exists(applicationHome.resolve(ApplicationInstanceGuard.LOCK_FILE_NAME)));
                }
            } finally {
                Files.writeString(releaseSignal, "release");
                if (!process.waitFor(1, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    process.waitFor(5, TimeUnit.SECONDS);
                }
            }
        }
    }

    private static Process startGuardProcess(Path applicationHome, Path readySignal, Path releaseSignal)
            throws IOException, URISyntaxException {
        String classPath = String.join(System.getProperty("path.separator"),
                codeSourcePath(ApplicationInstanceGuardProcess.class).toString(),
                codeSourcePath(ApplicationInstanceGuard.class).toString());
        Path javaExecutable = Path.of(System.getProperty("java.home"), "bin",
                isWindows() ? "java.exe" : "java");
        return new ProcessBuilder(javaExecutable.toString(), "-cp", classPath,
                ApplicationInstanceGuardProcess.class.getName(), applicationHome.toString(),
                readySignal.toString(), releaseSignal.toString())
                .redirectErrorStream(true)
                .start();
    }

    private static Path codeSourcePath(Class<?> clazz) throws URISyntaxException {
        return Path.of(clazz.getProtectionDomain().getCodeSource().getLocation().toURI());
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private static void awaitFile(Path expectedFile, WatchService watchService, Process process) throws Exception {
        if (Files.exists(expectedFile)) {
            return;
        }
        long deadline = System.nanoTime() + PROCESS_TIMEOUT.toNanos();
        while (System.nanoTime() < deadline) {
            if (!process.isAlive()) {
                throw new AssertionError("Guard process exited before signalling readiness. Output:\n"
                        + readProcessOutput(process));
            }
            long remainingNanos = deadline - System.nanoTime();
            WatchKey key = watchService.poll(remainingNanos, TimeUnit.NANOSECONDS);
            if (key == null) {
                break;
            }
            for (WatchEvent<?> event : key.pollEvents()) {
                if (expectedFile.getFileName().equals(event.context()) && Files.exists(expectedFile)) {
                    key.reset();
                    return;
                }
            }
            if (!key.reset()) {
                break;
            }
        }
        throw new AssertionError("Timed out waiting for the guard process. Output:\n" + readProcessOutput(process));
    }

    private static String readProcessOutput(Process process) {
        if (process.isAlive()) {
            return "<process is still running>";
        }
        try {
            return new String(process.getInputStream().readAllBytes());
        } catch (IOException e) {
            return "<unable to read process output: " + e + ">";
        }
    }
}
