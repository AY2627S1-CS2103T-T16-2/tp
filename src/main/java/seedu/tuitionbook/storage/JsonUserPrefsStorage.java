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
import seedu.tuitionbook.commons.util.JsonUtil;
import seedu.tuitionbook.model.ReadOnlyUserPrefs;
import seedu.tuitionbook.model.UserPrefs;

/**
 * A class to access UserPrefs stored on the hard disk as a JSON file
 */
public class JsonUserPrefsStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonUserPrefsStorage.class);

    private Path filePath;

    public JsonUserPrefsStorage(Path filePath) {
        this.filePath = filePath;
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
        Optional<UserPrefs> userPrefs = JsonUtil.readJsonFile(prefsFilePath, UserPrefs.class);
        if (userPrefs.isPresent() && userPrefs.get().getGuiSettings() == null) {
            throw new DataLoadingException(new IOException("GUI settings must not be null."));
        }
        return userPrefs;
    }

    /**
     * Saves the given {@link seedu.tuitionbook.model.ReadOnlyUserPrefs} to the storage.
     * @param userPrefs cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
        requireNonNull(userPrefs);
        requireNonNull(filePath);

        Path absoluteFilePath = filePath.toAbsolutePath();
        Files.createDirectories(absoluteFilePath.getParent());
        Path temporaryFile = Files.createTempFile(absoluteFilePath.getParent(),
                absoluteFilePath.getFileName() + ".", ".tmp");
        IOException saveFailure = null;
        try {
            writeJsonFile(userPrefs, temporaryFile);
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
            logger.log(Level.WARNING, "Could not remove temporary preference file at " + temporaryFile,
                    cleanupFailure);
            if (saveFailure == null) {
                throw cleanupFailure;
            }
            saveFailure.addSuppressed(cleanupFailure);
        }
    }

    protected void writeJsonFile(ReadOnlyUserPrefs userPrefs, Path targetFile) throws IOException {
        JsonUtil.saveJsonFile(userPrefs, targetFile);
    }

    protected void replaceFile(Path source, Path target) throws IOException {
        Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    protected void deleteTemporaryFile(Path temporaryFile) throws IOException {
        Files.deleteIfExists(temporaryFile);
    }

}
