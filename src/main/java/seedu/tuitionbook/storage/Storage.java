package seedu.tuitionbook.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import seedu.tuitionbook.commons.exceptions.DataLoadingException;
import seedu.tuitionbook.model.ReadOnlyTuitionBook;
import seedu.tuitionbook.model.ReadOnlyUserPrefs;
import seedu.tuitionbook.model.UserPrefs;

/**
 * API of the Storage component
 */
public interface Storage {

    /**
     * Returns the file path of the UserPrefs data file.
     */
    Path getUserPrefsFilePath();

    /**
     * Returns UserPrefs data from storage.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if the loading of data from preference file failed.
     */
    Optional<UserPrefs> readUserPrefs() throws DataLoadingException;

    /**
     * Saves the given {@link seedu.tuitionbook.model.ReadOnlyUserPrefs} to the storage.
     * @param userPrefs cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException;

    /**
     * Returns the file path of the TuitionBook data file.
     */
    Path getTuitionBookFilePath();

    /**
     * Returns TuitionBook data as a {@link ReadOnlyTuitionBook}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    Optional<ReadOnlyTuitionBook> readTuitionBook() throws DataLoadingException;

    /**
     * Saves the given {@link ReadOnlyTuitionBook} to the storage.
     * @param tuitionBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    void saveTuitionBook(ReadOnlyTuitionBook tuitionBook) throws IOException;

}
