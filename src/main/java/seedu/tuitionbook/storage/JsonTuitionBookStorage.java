package seedu.tuitionbook.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.tuitionbook.commons.core.LogsCenter;
import seedu.tuitionbook.commons.exceptions.DataLoadingException;
import seedu.tuitionbook.commons.exceptions.IllegalValueException;
import seedu.tuitionbook.commons.util.FileUtil;
import seedu.tuitionbook.commons.util.JsonUtil;
import seedu.tuitionbook.model.ReadOnlyTuitionBook;

/**
 * A class to access TuitionBook data stored as a JSON file on the hard disk.
 */
public class JsonTuitionBookStorage {

    public static final String MESSAGE_WRITE_BLOCKED = "Saving is disabled because the existing data file at %s "
            + "could not be loaded. Correct the data file and restart or reload TuitionBook before saving.";

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
        requireNonNull(filePath);
        boolean isConfiguredFile = isConfiguredFilePath(filePath);

        try {
            Optional<JsonSerializableTuitionBook> jsonTuitionBook = JsonUtil.readJsonFile(
                    filePath, JsonSerializableTuitionBook.class);
            if (jsonTuitionBook.isEmpty()) {
                if (isConfiguredFile) {
                    isWriteBlocked = false;
                }
                return Optional.empty();
            }

            try {
                ReadOnlyTuitionBook tuitionBook = jsonTuitionBook.get().toModelType();
                if (isConfiguredFile) {
                    isWriteBlocked = false;
                }
                return Optional.of(tuitionBook);
            } catch (IllegalValueException ive) {
                logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
                throw new DataLoadingException(ive);
            }
        } catch (DataLoadingException dle) {
            if (isConfiguredFile) {
                isWriteBlocked = true;
            }
            throw dle;
        }
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
            throw new IOException(String.format(MESSAGE_WRITE_BLOCKED, this.filePath));
        }

        FileUtil.createIfMissing(filePath);
        JsonUtil.saveJsonFile(new JsonSerializableTuitionBook(tuitionBook), filePath);
    }

    /**
     * Returns true if {@code filePath} identifies this storage instance's configured address-book file.
     */
    private boolean isConfiguredFilePath(Path filePath) {
        return this.filePath.toAbsolutePath().normalize().equals(filePath.toAbsolutePath().normalize());
    }

}
