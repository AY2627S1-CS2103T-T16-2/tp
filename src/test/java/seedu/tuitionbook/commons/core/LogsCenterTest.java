package seedu.tuitionbook.commons.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class LogsCenterTest {

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

        boolean added = assertDoesNotThrow(() -> LogsCenter.addFileHandler(testLogger, logFile));

        assertFalse(added);
        assertTrue(List.of(testLogger.getHandlers()).contains(recordingHandler));
        assertTrue(recordingHandler.records.stream().anyMatch(record ->
                record.getLevel() == Level.WARNING
                        && record.getMessage().contains(logFile.toString())
                        && record.getThrown() != null));
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
