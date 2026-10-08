package seedu.tuitionbook.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.tuitionbook.commons.core.LogsCenter;
import seedu.tuitionbook.commons.exceptions.DataLoadingException;
import seedu.tuitionbook.model.ReadOnlyTuitionBook;
import seedu.tuitionbook.model.ReadOnlyUserPrefs;
import seedu.tuitionbook.model.UserPrefs;

/**
 * Manages storage of TuitionBook data in local storage.
 */
public class StorageManager implements Storage {

    private static final Logger logger = LogsCenter.getLogger(StorageManager.class);
    private JsonTuitionBookStorage tuitionBookStorage;
    private JsonUserPrefsStorage userPrefsStorage;

    /**
     * Creates a {@code StorageManager} with the given TuitionBook and user prefs storage.
     */
    public StorageManager(JsonTuitionBookStorage tuitionBookStorage, JsonUserPrefsStorage userPrefsStorage) {
        this.tuitionBookStorage = tuitionBookStorage;
        this.userPrefsStorage = userPrefsStorage;
    }

    // ================ UserPrefs methods ==============================

    @Override
    public Path getUserPrefsFilePath() {
        return userPrefsStorage.getUserPrefsFilePath();
    }

    @Override
    public Optional<UserPrefs> readUserPrefs() throws DataLoadingException {
        return userPrefsStorage.readUserPrefs();
    }

    @Override
    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
        userPrefsStorage.saveUserPrefs(userPrefs);
    }


    // ================ TuitionBook methods ==============================

    @Override
    public Path getTuitionBookFilePath() {
        return tuitionBookStorage.getTuitionBookFilePath();
    }

    @Override
    public Optional<ReadOnlyTuitionBook> readTuitionBook() throws DataLoadingException {
        logger.fine("Attempting to read data from file: " + tuitionBookStorage.getTuitionBookFilePath());
        return tuitionBookStorage.readTuitionBook();
    }

    @Override
    public void saveTuitionBook(ReadOnlyTuitionBook tuitionBook) throws IOException {
        logger.fine("Attempting to write to data file: " + tuitionBookStorage.getTuitionBookFilePath());
        tuitionBookStorage.saveTuitionBook(tuitionBook);
    }

}
