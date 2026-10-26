package seedu.tuitionbook;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import seedu.tuitionbook.commons.core.ApplicationInstanceGuard;
import seedu.tuitionbook.commons.core.ApplicationInstanceGuard.AlreadyRunningException;
import seedu.tuitionbook.commons.core.ApplicationPaths;
import seedu.tuitionbook.commons.core.LogsCenter;

/**
 * The main entry point to the application.
 *
 * This is a workaround for the following error when MainApp is made the
 * entry point of the application:
 *
 *     Error: JavaFX runtime components are missing, and are required to run this application
 *
 * The reason is that MainApp extends Application. In that case, the
 * LauncherHelper will check for the javafx.graphics module to be present
 * as a named module. We don't use JavaFX via the module system so it can't
 * find the javafx.graphics module, and so the launch is aborted.
 *
 * By having a separate main class (Main) that doesn't extend Application
 * to be the entry point of the application, we avoid this issue.
 */
public class Main {

    static final String ALREADY_RUNNING_TITLE = "TuitionBook is already running";
    static final String LOCK_FAILED_TITLE = "TuitionBook could not start safely";

    public static void main(String[] args) {
        runWithInstanceGuard(ApplicationPaths.getApplicationHome(), () -> launchApplication(args),
                Main::showStartupError);
    }

    private static void launchApplication(String[] args) {
        launchApplication(args, Application::launch);
    }

    static void launchApplication(String[] args, ApplicationLauncher applicationLauncher) {
        Logger logger = LogsCenter.getLogger(Main.class);
        logger.warning("The warnings about a 'restricted method in java.lang.System' "
            + "and 'enabling native access' appearing below (if any) can be ignored.");
        applicationLauncher.launch(MainApp.class, args);
    }

    /**
     * Runs {@code guardedStartup} while holding the application-home guard.
     * Package-private so startup rejection and cleanup can be tested without launching JavaFX.
     */
    static void runWithInstanceGuard(Path applicationHome, Runnable guardedStartup,
            StartupErrorReporter errorReporter) {
        ApplicationInstanceGuard instanceGuard;
        try {
            instanceGuard = ApplicationInstanceGuard.acquire(applicationHome);
        } catch (AlreadyRunningException e) {
            String message = "Another TuitionBook window is already using this application folder. "
                    + "Use or close the existing window before launching TuitionBook again. "
                    + "To run an independent copy, place the JAR in a different folder.";
            reportStartupError(errorReporter, ALREADY_RUNNING_TITLE, message, e);
            return;
        } catch (IOException e) {
            String message = "TuitionBook could not lock its application folder at "
                    + applicationHome.toAbsolutePath().normalize() + ". Check that the folder is writable and that "
                    + "you have permission to create and lock " + ApplicationInstanceGuard.LOCK_FILE_NAME
                    + ", then try again. TuitionBook was not started, to protect your contacts and preferences.";
            reportStartupError(errorReporter, LOCK_FAILED_TITLE, message, e);
            return;
        }

        runGuardedStartup(instanceGuard::close, guardedStartup);
    }

    static void runGuardedStartup(GuardHandle guard, Runnable guardedStartup) {
        try (guard) {
            guardedStartup.run();
        } catch (IOException e) {
            System.err.println("TuitionBook could not release its application-folder lock cleanly: " + e);
        }
    }

    private static void reportStartupError(StartupErrorReporter errorReporter, String title, String message,
            Exception cause) {
        System.err.println(title + ": " + message);
        System.err.println("Cause: " + cause);
        errorReporter.show(title, message);
    }

    /** Shows a startup failure without constructing {@link MainApp} or its storage. */
    private static void showStartupError(String title, String message) {
        showStartupError(title, message, Platform::startup);
    }

    static void showStartupError(String title, String message, PlatformStarter platformStarter) {
        try {
            platformStarter.start(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle(title);
                alert.setHeaderText(title);
                alert.setContentText(message);
                alert.setResizable(true);
                alert.showAndWait();
                Platform.exit();
            });
        } catch (RuntimeException e) {
            System.err.println("The graphical startup error could not be displayed: " + e);
        }
    }

    @FunctionalInterface
    interface StartupErrorReporter {
        void show(String title, String message);
    }

    @FunctionalInterface
    interface ApplicationLauncher {
        void launch(Class<? extends Application> applicationClass, String... args);
    }

    @FunctionalInterface
    interface GuardHandle extends AutoCloseable {
        @Override
        void close() throws IOException;
    }

    @FunctionalInterface
    interface PlatformStarter {
        void start(Runnable runnable);
    }
}
