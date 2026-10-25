package seedu.tuitionbook;

import java.io.PrintStream;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.logging.Logger;

import javafx.application.Application;
import seedu.tuitionbook.commons.core.LogsCenter;
import seedu.tuitionbook.commons.exceptions.StoragePathException;

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
    private static Logger logger = LogsCenter.getLogger(Main.class);

    public static void main(String[] args) {
        logger.warning("The warnings about a 'restricted method in java.lang.System' "
            + "and 'enabling native access' appearing below (if any) can be ignored.");
        run(args, MainApp::verifyStartupPaths,
                launchArgs -> Application.launch(MainApp.class, launchArgs), System::exit, System.err);
    }

    /** Runs startup preflight before launching JavaFX. Package-private for testing without exiting the JVM. */
    static void run(String[] args, StartupPathVerifier verifier, Consumer<String[]> launcher,
            IntConsumer exit, PrintStream errorStream) {
        try {
            verifier.verify();
        } catch (StoragePathException exception) {
            errorStream.println("TuitionBook could not start. " + exception.getMessage());
            exit.accept(1);
            return;
        }
        launcher.accept(args);
    }

    @FunctionalInterface
    interface StartupPathVerifier {
        void verify() throws StoragePathException;
    }
}
