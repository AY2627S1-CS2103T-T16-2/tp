package seedu.tuitionbook.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static seedu.tuitionbook.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.tuitionbook.commons.core.GuiSettings;
import seedu.tuitionbook.commons.exceptions.DataLoadingException;
import seedu.tuitionbook.model.ReadOnlyUserPrefs;
import seedu.tuitionbook.model.UserPrefs;

public class JsonUserPrefsStorageTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonUserPrefsStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readUserPrefs_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readUserPrefs(null));
    }

    private Optional<UserPrefs> readUserPrefs(String userPrefsFileInTestDataFolder) throws DataLoadingException {
        Path prefsFilePath = addToTestDataPathIfNotNull(userPrefsFileInTestDataFolder);
        return new JsonUserPrefsStorage(prefsFilePath).readUserPrefs(prefsFilePath);
    }

    @Test
    public void readUserPrefs_missingFile_emptyResult() throws DataLoadingException {
        assertFalse(readUserPrefs("NonExistentFile.json").isPresent());
    }

    @Test
    public void readUserPrefs_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readUserPrefs("NotJsonFormatUserPrefs.json"));
    }

    private Path addToTestDataPathIfNotNull(String userPrefsFileInTestDataFolder) {
        return userPrefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(userPrefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void readUserPrefs_fileInOrder_successfullyRead() throws DataLoadingException {
        UserPrefs expected = getTypicalUserPrefs();
        UserPrefs actual = readUserPrefs("TypicalUserPref.json").get();
        assertEquals(expected, actual);
    }

    @Test
    public void readUserPrefs_valuesMissingFromFile_defaultValuesUsed() throws DataLoadingException {
        UserPrefs actual = readUserPrefs("EmptyUserPrefs.json").get();
        assertEquals(new UserPrefs(), actual);
    }

    @Test
    public void readUserPrefs_nullGuiSettings_exceptionThrown() throws IOException {
        Path prefsFile = testFolder.resolve("NullGuiSettings.json");
        Files.writeString(prefsFile, "{\"guiSettings\":null}");

        assertThrows(DataLoadingException.class, () -> new JsonUserPrefsStorage(prefsFile).readUserPrefs());
    }

    @Test
    public void readUserPrefs_extraValuesInFile_extraValuesIgnored() throws DataLoadingException {
        UserPrefs expected = getTypicalUserPrefs();
        UserPrefs actual = readUserPrefs("ExtraValuesUserPref.json").get();

        assertEquals(expected, actual);
    }

    private UserPrefs getTypicalUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1000, 500, 300, 100));
        return userPrefs;
    }

    @Test
    public void savePrefs_nullPrefs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveUserPrefs(null, "SomeFile.json"));
    }

    @Test
    public void saveUserPrefs_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveUserPrefs(new UserPrefs(), null));
    }

    /**
     * Saves {@code userPrefs} at the specified {@code prefsFileInTestDataFolder} filepath.
     */
    private void saveUserPrefs(UserPrefs userPrefs, String prefsFileInTestDataFolder) {
        try {
            new JsonUserPrefsStorage(addToTestDataPathIfNotNull(prefsFileInTestDataFolder))
                    .saveUserPrefs(userPrefs);
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file", ioe);
        }
    }

    @Test
    public void saveUserPrefs_allInOrder_success() throws DataLoadingException, IOException {

        UserPrefs original = new UserPrefs();
        original.setGuiSettings(new GuiSettings(1200, 200, 0, 2));

        Path pefsFilePath = testFolder.resolve("TempPrefs.json");
        JsonUserPrefsStorage jsonUserPrefsStorage = new JsonUserPrefsStorage(pefsFilePath);

        //Try writing when the file doesn't exist
        jsonUserPrefsStorage.saveUserPrefs(original);
        UserPrefs readBack = jsonUserPrefsStorage.readUserPrefs().get();
        assertEquals(original, readBack);

        //Try saving when the file exists
        original.setGuiSettings(new GuiSettings(5, 5, 5, 5));
        jsonUserPrefsStorage.saveUserPrefs(original);
        readBack = jsonUserPrefsStorage.readUserPrefs().get();
        assertEquals(original, readBack);
    }

    @Test
    public void saveUserPrefs_writeFailure_preservesExistingFileAndCleansUpTemporaryFile() throws Exception {
        Path prefsFilePath = testFolder.resolve("AtomicPrefs.json");
        String originalContents = "{\"guiSettings\":{}}";
        Files.writeString(prefsFilePath, originalContents);
        Path[] temporaryFile = new Path[1];
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(prefsFilePath) {
            @Override
            protected void writeJsonFile(ReadOnlyUserPrefs userPrefs, Path targetFile) throws IOException {
                temporaryFile[0] = targetFile;
                Files.writeString(targetFile, "partial JSON");
                throw new IOException("simulated write failure");
            }
        };

        assertThrows(IOException.class, () -> storage.saveUserPrefs(getTypicalUserPrefs()));

        assertEquals(originalContents, Files.readString(prefsFilePath));
        assertNotNull(temporaryFile[0]);
        assertFalse(Files.exists(temporaryFile[0]));
    }

    @Test
    public void saveUserPrefs_runtimeFailure_preservesExistingFileAndCleansUpTemporaryFile() throws Exception {
        Path prefsFilePath = testFolder.resolve("AtomicPrefs.json");
        String originalContents = "{\"guiSettings\":{}}";
        Files.writeString(prefsFilePath, originalContents);
        Path[] temporaryFile = new Path[1];
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(prefsFilePath) {
            @Override
            protected void writeJsonFile(ReadOnlyUserPrefs userPrefs, Path targetFile) throws IOException {
                temporaryFile[0] = targetFile;
                Files.writeString(targetFile, "partial JSON");
                throw new IllegalStateException("simulated runtime failure");
            }
        };

        assertThrows(IllegalStateException.class, () -> storage.saveUserPrefs(getTypicalUserPrefs()));

        assertEquals(originalContents, Files.readString(prefsFilePath));
        assertNotNull(temporaryFile[0]);
        assertFalse(Files.exists(temporaryFile[0]));
    }

    @Test
    public void saveUserPrefs_saveAndCleanupFail_preservesSaveFailureAndSuppressesCleanupFailure() {
        Path prefsFilePath = testFolder.resolve("AtomicPrefs.json");
        IOException saveFailure = new IOException("simulated write failure");
        IOException cleanupFailure = new IOException("simulated cleanup failure");
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(prefsFilePath) {
            @Override
            protected void writeJsonFile(ReadOnlyUserPrefs userPrefs, Path targetFile) throws IOException {
                throw saveFailure;
            }

            @Override
            protected void deleteTemporaryFile(Path temporaryFile) throws IOException {
                throw cleanupFailure;
            }
        };

        IOException thrown = org.junit.jupiter.api.Assertions.assertThrows(IOException.class, () ->
                storage.saveUserPrefs(getTypicalUserPrefs()));

        assertSame(saveFailure, thrown);
        assertEquals(1, thrown.getSuppressed().length);
        assertSame(cleanupFailure, thrown.getSuppressed()[0]);
    }

}
