package seedu.tuitionbook.commons.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.tuitionbook.testutil.SymlinkTestUtil;

public class LogsCenterTest {

    private static final String DISTINCTIVE_LOG_MESSAGE = "literal FileHandler tokens remain in the JAR home";

    @TempDir
    public Path testFolder;

    @Test
    public void addFileHandler_unusablePath_keepsExistingHandlerAndReportsFailure() throws Exception {
        Logger testLogger = Logger.getLogger("LogsCenterTest." + UUID.randomUUID());
        testLogger.setUseParentHandlers(false);
        testLogger.setLevel(Level.ALL);
        RecordingHandler recordingHandler = new RecordingHandler();
        testLogger.addHandler(recordingHandler);
        Path fileInPlaceOfDirectory = Files.createFile(testFolder.resolve("not-a-directory"));
        Path logFile = fileInPlaceOfDirectory.resolve("tuitionbook.log");

        try {
            boolean added = assertDoesNotThrow(() -> LogsCenter.addFileHandler(testLogger, logFile));

            assertFalse(added);
            assertTrue(List.of(testLogger.getHandlers()).contains(recordingHandler));
            assertTrue(recordingHandler.records.stream().anyMatch(record ->
                    record.getLevel() == Level.WARNING
                            && record.getMessage().contains(logFile.toString())
                            && record.getThrown() != null));
        } finally {
            closeAndDetachHandlers(testLogger);
        }
    }

    @Test
    public void addFileHandler_literalPatternTokens_writesInsideRequestedDirectory() throws Exception {
        Logger testLogger = Logger.getLogger("LogsCenterTest." + UUID.randomUUID());
        testLogger.setUseParentHandlers(false);
        testLogger.setLevel(Level.ALL);
        Path logDirectory = Files.createDirectories(
                testFolder.resolve("logs %t %h %g %u existing 100% spaces 测试"));
        Path logFile = logDirectory.resolve("tuitionbook.log");

        try {
            assertTrue(LogsCenter.addFileHandler(testLogger, logFile));
            testLogger.info(DISTINCTIVE_LOG_MESSAGE);
            for (Handler handler : testLogger.getHandlers()) {
                handler.flush();
            }

            List<Path> generatedLogs;
            try (Stream<Path> files = Files.list(logDirectory)) {
                generatedLogs = files
                        .filter(Files::isRegularFile)
                        .filter(path -> !path.getFileName().toString().endsWith(".lck"))
                        .toList();
            }
            assertEquals(1, generatedLogs.size());
            assertEquals(logDirectory, generatedLogs.get(0).getParent());
            assertTrue(Files.readString(generatedLogs.get(0)).contains(DISTINCTIVE_LOG_MESSAGE));
        } finally {
            closeAndDetachHandlers(testLogger);
        }
    }

    @Test
    public void addFileHandler_externalRotationSymlink_rejectedWithoutChangingTarget() throws Exception {
        Logger testLogger = Logger.getLogger("LogsCenterTest." + UUID.randomUUID());
        testLogger.setUseParentHandlers(false);
        testLogger.setLevel(Level.ALL);
        RecordingHandler recordingHandler = new RecordingHandler();
        testLogger.addHandler(recordingHandler);
        Path applicationHome = Files.createDirectory(testFolder.resolve("application-home"));
        Path outsideFile = testFolder.resolve("outside.log");
        Files.writeString(outsideFile, "outside contents");
        SymlinkTestUtil.createSymbolicLink(applicationHome.resolve("tuitionbook.log.0"), outsideFile);

        try {
            assertFalse(LogsCenter.addFileHandler(
                    testLogger, applicationHome.resolve("tuitionbook.log"), applicationHome));
            assertEquals("outside contents", Files.readString(outsideFile));
            assertTrue(recordingHandler.records.stream().anyMatch(record ->
                    record.getLevel() == Level.WARNING && record.getThrown() != null));
        } finally {
            closeAndDetachHandlers(testLogger);
        }
    }

    @Test
    public void fileHandler_rotationPathChangedToOutsideSymlink_revalidatesBeforePublishing() throws Exception {
        Logger testLogger = Logger.getLogger("LogsCenterTest." + UUID.randomUUID());
        testLogger.setUseParentHandlers(false);
        testLogger.setLevel(Level.ALL);
        RecordingHandler recordingHandler = new RecordingHandler();
        testLogger.addHandler(recordingHandler);
        Path applicationHome = Files.createDirectory(testFolder.resolve("application-home"));
        Path logFile = applicationHome.resolve("tuitionbook.log");
        Path outsideFile = testFolder.resolve("outside.log");
        Files.writeString(outsideFile, "outside contents");

        try {
            assertTrue(LogsCenter.addFileHandler(testLogger, logFile, applicationHome));
            Path rotatedFile = applicationHome.resolve("tuitionbook.log.1");
            assertFalse(Files.exists(rotatedFile));
            SymlinkTestUtil.createSymbolicLink(rotatedFile, outsideFile);

            assertDoesNotThrow(() -> testLogger.info("must not reach outside target"));

            assertEquals("outside contents", Files.readString(outsideFile));
            assertTrue(Files.isSymbolicLink(rotatedFile));
            assertFalse(Files.readString(applicationHome.resolve("tuitionbook.log.0"))
                    .contains("must not reach outside target"));
            assertTrue(recordingHandler.records.stream().anyMatch(record ->
                    record.getMessage().equals("must not reach outside target")));
        } finally {
            closeAndDetachHandlers(testLogger);
        }
    }

    @Test
    public void fileHandler_lockPathChangedBeforePublish_rejectedLinkPreservedAndHandlerDisabled() throws Exception {
        Logger testLogger = Logger.getLogger("LogsCenterTest." + UUID.randomUUID());
        testLogger.setUseParentHandlers(false);
        testLogger.setLevel(Level.ALL);
        RecordingHandler recordingHandler = new RecordingHandler();
        testLogger.addHandler(recordingHandler);
        Path applicationHome = Files.createDirectory(testFolder.resolve("application-home"));
        Path logFile = applicationHome.resolve("tuitionbook.log");
        Path lockFile = applicationHome.resolve("tuitionbook.log.0.lck");
        Path outsideFile = testFolder.resolve("outside-lock");
        Files.writeString(outsideFile, "outside contents");

        try {
            assertTrue(LogsCenter.addFileHandler(testLogger, logFile, applicationHome));
            assertTrue(Files.exists(lockFile));
            deleteActiveLockFileOrSkip(lockFile);
            SymlinkTestUtil.createSymbolicLink(lockFile, outsideFile);

            assertDoesNotThrow(() -> testLogger.info("first message remains on console"));
            assertDoesNotThrow(() -> testLogger.info("disabled handler ignores later messages"));
            Handler fileHandler = getFileHandler(testLogger, recordingHandler);
            assertDoesNotThrow(fileHandler::close);
            assertDoesNotThrow(fileHandler::close);

            assertTrue(Files.isSymbolicLink(lockFile));
            assertEquals("outside contents", Files.readString(outsideFile));
            assertTrue(recordingHandler.records.stream().anyMatch(record ->
                    record.getMessage().equals("first message remains on console")));
            assertTrue(recordingHandler.records.stream().anyMatch(record ->
                    record.getMessage().equals("disabled handler ignores later messages")));
        } finally {
            closeAndDetachHandlers(testLogger);
        }
    }

    @Test
    public void fileHandler_lockPathChangedBeforeClose_rejectedLinkPreservedAndCloseIsIdempotent() throws Exception {
        Logger testLogger = Logger.getLogger("LogsCenterTest." + UUID.randomUUID());
        testLogger.setUseParentHandlers(false);
        testLogger.setLevel(Level.ALL);
        RecordingHandler recordingHandler = new RecordingHandler();
        testLogger.addHandler(recordingHandler);
        Path applicationHome = Files.createDirectory(testFolder.resolve("application-home"));
        Path logFile = applicationHome.resolve("tuitionbook.log");
        Path lockFile = applicationHome.resolve("tuitionbook.log.0.lck");
        Path outsideFile = testFolder.resolve("outside-lock");
        Files.writeString(outsideFile, "outside contents");

        try {
            assertTrue(LogsCenter.addFileHandler(testLogger, logFile, applicationHome));
            Handler fileHandler = getFileHandler(testLogger, recordingHandler);
            assertTrue(Files.exists(lockFile));
            deleteActiveLockFileOrSkip(lockFile);
            SymlinkTestUtil.createSymbolicLink(lockFile, outsideFile);

            assertDoesNotThrow(fileHandler::close);
            assertDoesNotThrow(fileHandler::close);

            assertTrue(Files.isSymbolicLink(lockFile));
            assertEquals("outside contents", Files.readString(outsideFile));
        } finally {
            closeAndDetachHandlers(testLogger);
        }
    }

    @Test
    public void fileHandler_normalLoggingRotationAndClose_writesLogsAndRemovesOwnedLock() throws Exception {
        Logger testLogger = Logger.getLogger("LogsCenterTest." + UUID.randomUUID());
        testLogger.setUseParentHandlers(false);
        testLogger.setLevel(Level.ALL);
        Path applicationHome = Files.createDirectory(testFolder.resolve("application-home"));
        Path logFile = applicationHome.resolve("tuitionbook.log");
        Path lockFile = applicationHome.resolve("tuitionbook.log.0.lck");

        assertTrue(LogsCenter.addFileHandler(testLogger, logFile, applicationHome));
        try {
            String largeMessage = "x".repeat(1024 * 1024);
            for (int messageNumber = 0; messageNumber < 6; messageNumber++) {
                testLogger.info(largeMessage);
            }

            assertTrue(Files.isRegularFile(applicationHome.resolve("tuitionbook.log.0")));
            assertTrue(Files.isRegularFile(applicationHome.resolve("tuitionbook.log.1")));
            assertTrue(Files.exists(lockFile));
        } finally {
            closeAndDetachHandlers(testLogger);
        }
        assertFalse(Files.exists(lockFile, LinkOption.NOFOLLOW_LINKS));
    }

    private static Handler getFileHandler(Logger logger, Handler otherHandler) {
        return Stream.of(logger.getHandlers())
                .filter(handler -> handler != otherHandler)
                .findFirst()
                .orElseThrow();
    }

    private static void deleteActiveLockFileOrSkip(Path lockFile) throws IOException {
        try {
            Files.delete(lockFile);
        } catch (FileSystemException exception) {
            if (System.getProperty("os.name").startsWith("Windows")) {
                Assumptions.assumeTrue(false,
                        "The platform does not allow replacing an open lock path: " + exception.getMessage());
            }
            throw exception;
        }
    }

    private static void closeAndDetachHandlers(Logger logger) {
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
            handler.close();
        }
    }

    private static class RecordingHandler extends Handler {
        private final List<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    }
}
