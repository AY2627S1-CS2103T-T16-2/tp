package seedu.tuitionbook.ui;

import java.awt.GraphicsEnvironment;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assumptions;

import javafx.application.Platform;

/**
 * Shared JavaFX lifecycle and synchronization helpers for UI tests.
 *
 * <p>The toolkit is process-wide and may only be started once. UI test classes
 * should call {@link #initialize()} in their {@code @BeforeAll} method and use
 * {@link #runOnFxThread(ThrowingRunnable)} for UI actions. The toolkit is kept
 * alive for the whole test process; individual test classes must not call
 * {@link Platform#exit()}.</p>
 */
final class JavaFxTestUtil {
    private static final long TIMEOUT_SECONDS = 10;
    private static final Object INITIALIZATION_LOCK = new Object();
    private static volatile boolean initialized;

    private JavaFxTestUtil() {
    }

    /**
     * Starts the shared JavaFX toolkit, unless another UI test has already done so.
     */
    static void initialize() {
        Assumptions.assumeFalse(isHeadlessEnvironment(),
                "JavaFX tests require a graphical display");

        synchronized (INITIALIZATION_LOCK) {
            if (initialized) {
                return;
            }

            CountDownLatch started = new CountDownLatch(1);
            try {
                Platform.startup(started::countDown);
            } catch (IllegalStateException alreadyStarted) {
                // Another test class owns startup; runLater below will synchronize with it.
                Platform.setImplicitExit(false);
                initialized = true;
                return;
            }

            await(started, "JavaFX toolkit startup");
            Platform.setImplicitExit(false);
            initialized = true;
        }
    }

    /**
     * Returns whether the current environment cannot provide a JavaFX display.
     */
    static boolean isHeadlessEnvironment() {
        boolean linuxWithoutDisplay = System.getProperty("os.name").toLowerCase().contains("linux")
                && System.getenv("DISPLAY") == null;
        return GraphicsEnvironment.isHeadless() || linuxWithoutDisplay;
    }

    /**
     * Runs an action on the JavaFX application thread and propagates failures to the test thread.
     */
    static void runOnFxThread(ThrowingRunnable action) throws Exception {
        initialize();

        CountDownLatch finished = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable throwable) {
                failure.set(throwable);
            } finally {
                finished.countDown();
            }
        });
        await(finished, "JavaFX action");
        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }
    }

    /**
     * Runs an action that returns a value on the JavaFX application thread.
     */
    static <T> T callOnFxThread(Callable<T> action) throws Exception {
        AtomicReference<T> result = new AtomicReference<>();
        runOnFxThread(() -> result.set(action.call()));
        return result.get();
    }

    private static void await(CountDownLatch latch, String operation) {
        try {
            if (!latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new AssertionError(operation + " did not complete within "
                        + TIMEOUT_SECONDS + " seconds");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for " + operation, exception);
        }
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }
}
