package seedu.tuitionbook.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import seedu.tuitionbook.commons.core.ApplicationPathGuard;
import seedu.tuitionbook.commons.exceptions.DataLoadingException;
import seedu.tuitionbook.commons.util.JsonUtil;
import seedu.tuitionbook.model.ReadOnlyUserPrefs;
import seedu.tuitionbook.model.UserPrefs;

/**
 * A class to access UserPrefs stored on the hard disk as a JSON file
 */
public class JsonUserPrefsStorage {

    private final Path filePath;
    private final ApplicationPathGuard pathGuard;

    public JsonUserPrefsStorage(Path filePath) {
        this(filePath, filePath.toAbsolutePath().normalize().getParent());
    }

    /** Creates preferences storage whose managed paths must remain inside {@code applicationHome}. */
    public JsonUserPrefsStorage(Path filePath, Path applicationHome) {
        this.filePath = filePath;
        pathGuard = new ApplicationPathGuard(applicationHome);
    }

    public Path getUserPrefsFilePath() {
        return filePath;
    }

    /**
     * Returns UserPrefs data from storage.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if the loading of data from preference file failed.
     */
    public Optional<UserPrefs> readUserPrefs() throws DataLoadingException {
        return readUserPrefs(filePath);
    }

    /**
     * Similar to {@link #readUserPrefs()}
     * @param prefsFilePath location of the data. Cannot be null.
     * @throws DataLoadingException if the file format is not as expected.
     */
    public Optional<UserPrefs> readUserPrefs(Path prefsFilePath) throws DataLoadingException {
        try {
            pathGuard.verify(prefsFilePath);
        } catch (IOException exception) {
            throw new DataLoadingException(exception);
        }
        return JsonUtil.readJsonFile(prefsFilePath, UserPrefs.class);
    }

    /**
     * Saves the given {@link seedu.tuitionbook.model.ReadOnlyUserPrefs} to the storage.
     * @param userPrefs cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
        pathGuard.verify(filePath);
        JsonUtil.saveJsonFile(userPrefs, filePath);
    }

}
