package seedu.tuitionbook.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.tuitionbook.commons.core.LogsCenter;
import seedu.tuitionbook.commons.exceptions.DataLoadingException;
import seedu.tuitionbook.commons.exceptions.IllegalValueException;
import seedu.tuitionbook.commons.util.JsonUtil;
import seedu.tuitionbook.model.ReadOnlyTuitionBook;

/**
 * A class to access TuitionBook data stored as a JSON file on the hard disk.
 */
public class JsonTuitionBookStorage {

    public static final String MESSAGE_WRITE_BLOCKED = "Saving is disabled because the existing data file at %s "
            + "could not be loaded. Correct or move the data file, then run reload or restart TuitionBook"
            + " before saving.";

    private static final Logger logger = LogsCenter.getLogger(JsonTuitionBookStorage.class);

    private Path filePath;
    private boolean isWriteBlocked;

    public JsonTuitionBookStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getTuitionBookFilePath() {
        return filePath;
    }

    /**
     * Returns TuitionBook data as a {@link ReadOnlyTuitionBook}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyTuitionBook> readTuitionBook() throws DataLoadingException {
        return readTuitionBook(filePath);
    }

    /**
     * Similar to {@link #readTuitionBook()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyTuitionBook> readTuitionBook(Path filePath) throws DataLoadingException {
        return readTuitionBookWithResult(filePath).map(TuitionBookLoadResult::tuitionBook);
    }

    /**
     * Returns the loaded data and guardian-link repair count, or an empty optional if the file is missing.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<TuitionBookLoadResult> readTuitionBookWithResult() throws DataLoadingException {
        return readTuitionBookWithResult(filePath);
    }

    /**
     * Returns the load result for {@code filePath}, or an empty optional if that file is missing.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<TuitionBookLoadResult> readTuitionBookWithResult(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);
        boolean isConfiguredFile = isConfiguredFilePath(filePath);

        try {
            Optional<JsonSerializableTuitionBook> jsonTuitionBook = JsonUtil.readJsonFileRejectingUnknownFields(
                    filePath, JsonSerializableTuitionBook.class);
            if (jsonTuitionBook.isEmpty()) {
                if (isConfiguredFile) {
                    isWriteBlocked = false;
                }
                return Optional.empty();
            }

            try {
                TuitionBookLoadResult result = jsonTuitionBook.get().toModelTypeWithResult();
                if (isConfiguredFile) {
                    isWriteBlocked = false;
                }
                return Optional.of(result);
            } catch (IllegalValueException ive) {
                logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
                throw new DataLoadingException(ive);
            }
        } catch (DataLoadingException dle) {
            if (isConfiguredFile) {
                throw new DataLoadingException(dle, preserveMalformedFile().orElse(null));
            }
            throw dle;
        }
    }

    /**
     * Moves an unreadable configured data file to an unused sibling backup file.
     * Saving remains blocked if the original file cannot be preserved.
     */
    private Optional<Path> preserveMalformedFile() {
        Path backupPath = getAvailableBackupPath();
        try {
            moveFile(filePath, backupPath);
            isWriteBlocked = false;
            logger.warning("Moved unreadable data file from " + filePath + " to " + backupPath + ".");
            return Optional.of(backupPath);
        } catch (IOException ioe) {
            isWriteBlocked = true;
            logger.warning("Could not preserve unreadable data file at " + filePath + ": " + ioe);
            return Optional.empty();
        }
    }

    /** Moves {@code source} to {@code target}; overridden in tests to simulate a failed recovery. */
    protected void moveFile(Path source, Path target) throws IOException {
        Files.move(source, target);
    }

    /** Returns an unused sibling backup path, starting with {@code <file>.backup}. */
    private Path getAvailableBackupPath() {
        Path backupPath = filePath.resolveSibling(filePath.getFileName() + ".backup");
        int suffix = 1;
        while (Files.exists(backupPath)) {
            backupPath = filePath.resolveSibling(filePath.getFileName() + ".backup." + suffix++);
        }
        return backupPath;
    }

    /**
     * Saves the given {@link ReadOnlyTuitionBook} to the storage.
     * @param tuitionBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveTuitionBook(ReadOnlyTuitionBook tuitionBook) throws IOException {
        saveTuitionBook(tuitionBook, filePath);
    }

    /**
     * Similar to {@link #saveTuitionBook(ReadOnlyTuitionBook)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveTuitionBook(ReadOnlyTuitionBook tuitionBook, Path filePath) throws IOException {
        requireNonNull(tuitionBook);
        requireNonNull(filePath);

        if (isWriteBlocked && isConfiguredFilePath(filePath)) {
            throw new SaveBlockedException(String.format(MESSAGE_WRITE_BLOCKED, this.filePath));
        }

        Path absoluteFilePath = filePath.toAbsolutePath();
        Files.createDirectories(absoluteFilePath.getParent());
        Path temporaryFile = Files.createTempFile(absoluteFilePath.getParent(),
                absoluteFilePath.getFileName() + ".", ".tmp");
        IOException saveFailure = null;
        try {
            writeJsonFile(new JsonSerializableTuitionBook(tuitionBook), temporaryFile);
            replaceFile(temporaryFile, absoluteFilePath);
        } catch (IOException ioe) {
            saveFailure = ioe;
            throw ioe;
        } finally {
            cleanUpTemporaryFile(temporaryFile, saveFailure);
        }
    }

    private void cleanUpTemporaryFile(Path temporaryFile, IOException saveFailure) throws IOException {
        try {
            deleteTemporaryFile(temporaryFile);
        } catch (IOException cleanupFailure) {
            logger.log(Level.WARNING, "Could not remove temporary data file at " + temporaryFile, cleanupFailure);
            if (saveFailure == null) {
                throw cleanupFailure;
            }
            saveFailure.addSuppressed(cleanupFailure);
        }
    }

    protected void writeJsonFile(JsonSerializableTuitionBook tuitionBook, Path filePath) throws IOException {
        JsonUtil.saveJsonFile(tuitionBook, filePath);
    }

    protected void replaceFile(Path source, Path target) throws IOException {
        Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    protected void deleteTemporaryFile(Path temporaryFile) throws IOException {
        Files.deleteIfExists(temporaryFile);
    }

    /**
     * Returns true if {@code filePath} identifies this storage instance's configured address-book file.
     */
    private boolean isConfiguredFilePath(Path filePath) {
        return this.filePath.toAbsolutePath().normalize().equals(filePath.toAbsolutePath().normalize());
    }

}
