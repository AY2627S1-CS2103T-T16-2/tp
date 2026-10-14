package seedu.tuitionbook.commons.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
