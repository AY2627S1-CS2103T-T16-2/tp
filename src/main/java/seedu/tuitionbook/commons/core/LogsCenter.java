package seedu.tuitionbook.commons.core;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Configures and manages loggers and handlers, including their logging level.
 * Named {@link Logger}s can be obtained from this class.<br>
 * These loggers have been configured to output messages to the console and a {@code .log} file by default,
 *   at the {@code INFO} level. A new {@code .log} file with a new numbering will be created after the log
 *   file reaches 5MB in size, up to a maximum of 5 files.<br>
 */
public class LogsCenter {
    // Change this to a lower level (e.g., Level.FINE) to enable more detailed log messages
    private static final Level LOG_LEVEL = Level.INFO;

    private static final int MAX_FILE_COUNT = 5;
    private static final int MAX_FILE_SIZE_IN_BYTES = (int) (Math.pow(2, 20) * 5); // 5MB
    private static final Path LOG_FILE = ApplicationPaths.resolve("tuitionbook.log");
    private static final Logger logger; // logger for this class
    private static Logger baseLogger; // to be used as the parent of all other loggers created by this class.

    // This static block ensures essential loggers are created early
    static {
        setBaseLogger();
        logger = LogsCenter.getLogger(LogsCenter.class);
    }

    /**
     * Creates a logger with the given name prefixed by the {@code baseLogger}'s name so that the created logger
     * becomes a descendant of the {@code baseLogger}. Furthermore, the returned logger will have the same log handlers
     * as the {@code baseLogger}.
     */
    public static Logger getLogger(String name) {
        // Java organizes loggers into a hierarchy based on their names (using '.' as a separator, similar to how Java
        // packages form a hierarchy). Furthermore, loggers without a level inherit the level of their parent logger.
        // By prefixing names of all loggers with baseLogger's name + ".", we make the baseLogger the parent of all
        // loggers. This allows us to change the level of all loggers simply by changing the baseLogger level.
        Logger logger = Logger.getLogger(baseLogger.getName() + "." + name);
        removeHandlers(logger);
        logger.setUseParentHandlers(true);
        return logger;
    }

    /**
     * Creates a Logger for the given class name.
     */
    public static <T> Logger getLogger(Class<T> clazz) {
        requireNonNull(clazz);
        return getLogger(clazz.getSimpleName());
    }

    /**
     * Removes all handlers from the {@code logger}.
     */
    private static void removeHandlers(Logger logger) {
        Arrays.stream(logger.getHandlers())
                .forEach(logger::removeHandler);
    }

    /**
     * Creates a logger named 'ab3', containing a {@code ConsoleHandler} and a {@code FileHandler}.
     * Sets it as the {@code baseLogger}, to be used as the parent logger of all other loggers.
     */
    private static void setBaseLogger() {
        baseLogger = Logger.getLogger("ab3");
        baseLogger.setUseParentHandlers(false);
        removeHandlers(baseLogger);
        // set the level of the baseLogger, which will be inherited by other loggers
        baseLogger.setLevel(LOG_LEVEL);

        // Level.ALL is used as the level for the handlers because the baseLogger filters the log messages by level
        // already; there is no need to control log message level of the handlers.

        // add a ConsoleHandler to log to the console
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        baseLogger.addHandler(consoleHandler);

        // add a FileHandler to log to a file
        addFileHandler(baseLogger, LOG_FILE, ApplicationPaths.getApplicationHome());
    }

    /**
     * Adds the rotating file handler, leaving existing handlers usable if the file cannot be opened.
     *
     * @return true if the file handler was added
     */
    static boolean addFileHandler(Logger targetLogger, Path logFile) {
        return addFileHandler(targetLogger, logFile, logFile.toAbsolutePath().normalize().getParent());
    }

    /** Adds a rotating handler only when its log, rotation, and lock paths are contained in the application home. */
    static boolean addFileHandler(Logger targetLogger, Path logFile, Path applicationHome) {
        try {
            verifyLogPaths(logFile, applicationHome);
            String escapedLogFilePattern = logFile.toString().replace("%", "%%");
            FileHandler fileHandler = new FileHandler(escapedLogFilePattern,
                    MAX_FILE_SIZE_IN_BYTES, MAX_FILE_COUNT, true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);
            ContainedFileHandler containedFileHandler = new ContainedFileHandler(
                    fileHandler, logFile, applicationHome);
            containedFileHandler.setLevel(Level.ALL);
            targetLogger.addHandler(containedFileHandler);
            return true;
        } catch (IOException | SecurityException e) {
            targetLogger.log(Level.WARNING, "Unable to create log file at " + logFile
                    + "; console logging will continue.", e);
            return false;
        }
    }

    private static void verifyLogPaths(Path logFile, Path applicationHome) throws IOException {
        ApplicationPathGuard pathGuard = new ApplicationPathGuard(applicationHome);
        Path logDirectory = logFile.toAbsolutePath().normalize().getParent();
        pathGuard.verify(logDirectory);
        pathGuard.verify(logFile);
        for (int generation = 0; generation < MAX_FILE_COUNT; generation++) {
            Path rotatedFile = logFile.resolveSibling(logFile.getFileName() + "." + generation);
            pathGuard.verify(rotatedFile);
            pathGuard.verify(rotatedFile.resolveSibling(rotatedFile.getFileName() + ".lck"));
        }
        String generatedFilePrefix = logFile.getFileName() + ".";
        try (DirectoryStream<Path> generatedFiles = Files.newDirectoryStream(logDirectory,
                path -> path.getFileName().toString().startsWith(generatedFilePrefix))) {
            for (Path generatedFile : generatedFiles) {
                pathGuard.verify(generatedFile);
            }
        }
    }

    /** Revalidates rotation and lock paths before each publish without logging failures recursively. */
    private static final class ContainedFileHandler extends Handler {
        private final FileHandler delegate;
        private final Path logFile;
        private final Path applicationHome;
        private boolean isDisabled;

        private ContainedFileHandler(FileHandler delegate, Path logFile, Path applicationHome) {
            this.delegate = delegate;
            this.logFile = logFile;
            this.applicationHome = applicationHome;
        }

        @Override
        public void publish(LogRecord record) {
            if (isDisabled || !isLoggable(record)) {
                return;
            }
            try {
                verifyLogPaths(logFile, applicationHome);
                delegate.publish(record);
            } catch (IOException | SecurityException exception) {
                isDisabled = true;
                delegate.close();
                System.err.println("Unable to continue file logging at " + logFile
                        + " because its path is unsafe; console logging will continue. " + exception);
            }
        }

        @Override
        public void flush() {
            delegate.flush();
        }

        @Override
        public void close() {
            delegate.close();
        }
    }

}
