package seedu.tuitionbook.commons.core;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/** Separate-JVM test helper that holds a guard until its parent process signals release. */
public final class ApplicationInstanceGuardProcess {

    private static final Duration RELEASE_TIMEOUT = Duration.ofSeconds(30);

    private ApplicationInstanceGuardProcess() {
    }

    public static void main(String[] args) throws Exception {
        Path applicationHome = Path.of(args[0]);
        Path readySignal = Path.of(args[1]);
        Path releaseSignal = Path.of(args[2]);

        try (ApplicationInstanceGuard ignored = ApplicationInstanceGuard.acquire(applicationHome);
                WatchService watchService = FileSystems.getDefault().newWatchService()) {
            applicationHome.register(watchService, ENTRY_CREATE, ENTRY_MODIFY);
            Files.writeString(readySignal, "ready");
            awaitRelease(releaseSignal, watchService);
        }
    }

    private static void awaitRelease(Path releaseSignal, WatchService watchService) throws Exception {
        if (Files.exists(releaseSignal)) {
            return;
        }
        long deadline = System.nanoTime() + RELEASE_TIMEOUT.toNanos();
        while (System.nanoTime() < deadline) {
            WatchKey key = watchService.poll(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
            if (key == null) {
                break;
            }
            for (WatchEvent<?> event : key.pollEvents()) {
                if (releaseSignal.getFileName().equals(event.context()) && Files.exists(releaseSignal)) {
                    key.reset();
                    return;
                }
            }
            if (!key.reset()) {
                break;
            }
        }
        throw new IllegalStateException("Timed out waiting for the release signal");
    }
}
