package seedu.tuitionbook.commons.core;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
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
     * Creates a logger named 'ab3', containing console and rotating-file handlers.
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

        // add a rotating handler to log to files
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
        return addFileHandler(targetLogger, logFile, applicationHome, BasicFileAttributes::fileKey);
    }

    /** Adds a rotating handler using the supplied filesystem-identity reader. */
    static boolean addFileHandler(Logger targetLogger, Path logFile, Path applicationHome,
            FileIdentityReader fileIdentityReader) {
        return addFileHandler(targetLogger, logFile, applicationHome,
                MAX_FILE_SIZE_IN_BYTES, MAX_FILE_COUNT, fileIdentityReader,
                ContainedFileHandler::tryAcquireLock);
    }

    /** Adds a rotating handler using supplied filesystem-identity and lock-acquisition operations. */
    static boolean addFileHandler(Logger targetLogger, Path logFile, Path applicationHome,
            FileIdentityReader fileIdentityReader, FileLockAcquirer fileLockAcquirer) {
        return addFileHandler(targetLogger, logFile, applicationHome,
                MAX_FILE_SIZE_IN_BYTES, MAX_FILE_COUNT, fileIdentityReader, fileLockAcquirer);
    }

    /** Adds a rotating handler with explicit rotation limits. Package-private for deterministic tests. */
    static boolean addFileHandler(Logger targetLogger, Path logFile, Path applicationHome,
            int maxFileSizeInBytes, int maxFileCount) {
        return addFileHandler(targetLogger, logFile, applicationHome,
                maxFileSizeInBytes, maxFileCount, BasicFileAttributes::fileKey,
                ContainedFileHandler::tryAcquireLock);
    }

    private static boolean addFileHandler(Logger targetLogger, Path logFile, Path applicationHome,
            int maxFileSizeInBytes, int maxFileCount, FileIdentityReader fileIdentityReader,
            FileLockAcquirer fileLockAcquirer) {
        try {
            verifyLogPaths(logFile, applicationHome);
            ContainedFileHandler containedFileHandler = new ContainedFileHandler(
                    logFile, applicationHome, maxFileSizeInBytes, maxFileCount,
                    fileIdentityReader, fileLockAcquirer);
            containedFileHandler.setFormatter(new SimpleFormatter());
            containedFileHandler.setLevel(Level.ALL);
            targetLogger.addHandler(containedFileHandler);
            return true;
        } catch (IOException | SecurityException e) {
            targetLogger.log(Level.WARNING, "Unable to create log file at " + logFile
                    + "; console logging will continue.", e);
            return false;
        }
    }

    @FunctionalInterface
    interface FileIdentityReader {
        Object read(BasicFileAttributes attributes);
    }

    @FunctionalInterface
    interface FileLockAcquirer {
        FileLock acquire(FileChannel channel) throws IOException;
    }

    @FunctionalInterface
    interface CloseAction {
        void close() throws IOException;
    }

    /** Runs one cleanup action and preserves an earlier failure when both actions fail. */
    static IOException closeAndPreserveFirstFailure(CloseAction action, IOException earlierFailure) {
        try {
            action.close();
            return earlierFailure;
        } catch (IOException exception) {
            return earlierFailure == null ? exception : earlierFailure;
        }
    }

    /** Converts a cleanup failure to an {@code IOException} unless an earlier cleanup failure already exists. */
    static IOException preserveFirstCleanupFailure(IOException earlierFailure, Exception currentFailure) {
        if (earlierFailure != null) {
            return earlierFailure;
        }
        return currentFailure instanceof IOException ioException ? ioException : new IOException(currentFailure);
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

    /**
     * Rotates contained log files and owns the resources needed to close them without pathname-based JDK cleanup.
     */
    private static final class ContainedFileHandler extends Handler {
        private final Path logFile;
        private final Path applicationHome;
        private final Path activeLogFile;
        private final Path lockFile;
        private final int maxFileSizeInBytes;
        private final int maxFileCount;
        private final FileIdentityReader fileIdentityReader;
        private final FileLockAcquirer fileLockAcquirer;
        private boolean canDeleteLockFile;
        private Object lockFileKey;

        private OutputStream logOutput;
        private FileChannel lockChannel;
        private FileLock fileLock;
        private long currentFileSize;
        private boolean isDisabled;
        private boolean isClosed;

        private ContainedFileHandler(Path logFile, Path applicationHome, int maxFileSizeInBytes,
                int maxFileCount, FileIdentityReader fileIdentityReader,
                FileLockAcquirer fileLockAcquirer) throws IOException {
            this.logFile = logFile.toAbsolutePath().normalize();
            this.applicationHome = applicationHome.toAbsolutePath().normalize();
            this.activeLogFile = getGenerationPath(0);
            this.lockFile = activeLogFile.resolveSibling(activeLogFile.getFileName() + ".lck");
            this.maxFileSizeInBytes = maxFileSizeInBytes;
            this.maxFileCount = maxFileCount;
            this.fileIdentityReader = fileIdentityReader;
            this.fileLockAcquirer = fileLockAcquirer;

            try {
                lockChannel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                BasicFileAttributes lockAttributes = Files.readAttributes(
                        lockFile, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                lockFileKey = fileIdentityReader.read(lockAttributes);
                fileLock = fileLockAcquirer.acquire(lockChannel);
                if (fileLock == null) {
                    throw new IOException("The log-file lock is held by another process");
                }
                canDeleteLockFile = !lockAttributes.isSymbolicLink();
                openLogOutput(true);
            } catch (IOException | RuntimeException exception) {
                IOException closeFailure = closeOpenResources();
                deleteLockFileIfUnchanged(closeFailure);
                if (exception instanceof IOException ioException) {
                    throw ioException;
                }
                throw new IOException("Unable to acquire the log-file lock " + lockFile, exception);
            }
        }

        @Override
        public synchronized void publish(LogRecord record) {
            if (isDisabled || !isLoggable(record)) {
                return;
            }
            try {
                verifyLogPaths(logFile, applicationHome);
                byte[] formattedRecord = getFormatter().format(record).getBytes(StandardCharsets.UTF_8);
                if (currentFileSize > 0 && currentFileSize + formattedRecord.length > maxFileSizeInBytes) {
                    rotateLogs();
                }
                logOutput.write(formattedRecord);
                logOutput.flush();
                currentFileSize += formattedRecord.length;
            } catch (IOException | SecurityException exception) {
                disable(exception);
            }
        }

        @Override
        public synchronized void flush() {
            if (isDisabled) {
                return;
            }
            try {
                verifyLogPaths(logFile, applicationHome);
                logOutput.flush();
            } catch (IOException | SecurityException exception) {
                disable(exception);
            }
        }

        @Override
        public synchronized void close() {
            if (isClosed) {
                return;
            }
            isClosed = true;
            isDisabled = true;
            IOException closeFailure = closeOpenResources();
            deleteLockFileIfUnchanged(closeFailure);
        }

        private static FileLock tryAcquireLock(FileChannel channel) throws IOException {
            try {
                return channel.tryLock();
            } catch (OverlappingFileLockException exception) {
                throw new IOException("The log-file lock is already held in this process", exception);
            }
        }

        private Path getGenerationPath(int generation) {
            return logFile.resolveSibling(logFile.getFileName() + "." + generation);
        }

        private void openLogOutput(boolean append) throws IOException {
            if (append) {
                logOutput = Files.newOutputStream(activeLogFile, StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE, StandardOpenOption.APPEND);
                currentFileSize = Files.size(activeLogFile);
            } else {
                logOutput = Files.newOutputStream(activeLogFile, StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                currentFileSize = 0;
            }
        }

        private void rotateLogs() throws IOException {
            logOutput.close();
            logOutput = null;
            verifyLogPaths(logFile, applicationHome);
            for (int generation = maxFileCount - 2; generation >= 0; generation--) {
                Path source = getGenerationPath(generation);
                if (Files.exists(source, LinkOption.NOFOLLOW_LINKS)) {
                    Files.move(source, getGenerationPath(generation + 1), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            openLogOutput(false);
        }

        private void disable(Exception exception) {
            isDisabled = true;
            IOException closeFailure = closeOpenResources();
            deleteLockFileIfUnchanged(closeFailure);
            System.err.println("Unable to continue file logging at " + logFile
                    + " because its path is unsafe; console logging will continue. " + exception);
        }

        /** Closes only already-open resources, so a changed pathname cannot redirect cleanup. */
        private IOException closeOpenResources() {
            IOException firstFailure = null;
            if (logOutput != null) {
                try {
                    firstFailure = closeAndPreserveFirstFailure(logOutput::close, firstFailure);
                } finally {
                    logOutput = null;
                }
            }
            if (fileLock != null) {
                try {
                    firstFailure = closeAndPreserveFirstFailure(fileLock::release, firstFailure);
                } finally {
                    fileLock = null;
                }
            }
            if (lockChannel != null) {
                try {
                    firstFailure = closeAndPreserveFirstFailure(lockChannel::close, firstFailure);
                } finally {
                    lockChannel = null;
                }
            }
            return firstFailure;
        }

        /**
         * Deletes only the unchanged regular lock file exclusively acquired by this handler. Providers that do not
         * expose a stable file identity retain an inert lock pathname after its channel and OS lock are released.
         */
        private void deleteLockFileIfUnchanged(IOException earlierFailure) {
            if (!canDeleteLockFile) {
                reportCleanupFailure(earlierFailure);
                return;
            }
            try {
                new ApplicationPathGuard(applicationHome).verify(lockFile);
                BasicFileAttributes attributes = Files.readAttributes(
                        lockFile, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                Object currentLockFileKey = fileIdentityReader.read(attributes);
                if (!attributes.isSymbolicLink() && lockFileKey != null
                        && lockFileKey.equals(currentLockFileKey)) {
                    Files.delete(lockFile);
                }
            } catch (IOException | SecurityException exception) {
                earlierFailure = preserveFirstCleanupFailure(earlierFailure, exception);
            }
            reportCleanupFailure(earlierFailure);
        }

        private void reportCleanupFailure(IOException exception) {
            if (exception != null) {
                System.err.println("Unable to finish file-logging cleanup at " + logFile
                        + "; no unverified pathname was removed. " + exception);
            }
        }
    }

}
