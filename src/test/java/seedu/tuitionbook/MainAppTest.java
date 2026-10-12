package seedu.tuitionbook;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.tuitionbook.commons.core.GuiSettings;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.ReadOnlyUserPrefs;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.UserPrefs;
import seedu.tuitionbook.storage.JsonTuitionBookStorage;
import seedu.tuitionbook.storage.JsonUserPrefsStorage;
import seedu.tuitionbook.storage.StorageManager;
import seedu.tuitionbook.ui.StartupNotice;

/**
 * Tests the application startup outcome using disposable contact-data files.
 */
public class MainAppTest {

    @TempDir
    public Path testFolder;

    @Test
    public void loadStartupState_missingFile_loadsSamplesAndShowsNotice() {
        MainApp.StartupState state = MainApp.loadStartupState(storage(), new UserPrefs());

        assertEquals(6, state.model().getTuitionBook().getPersonList().size());
        assertEquals(StartupNotice.missingDataFile(), state.notice().orElseThrow());
    }

    @Test
    public void loadStartupState_existingEmptyFile_keepsEmptyDataWithoutWarning() throws Exception {
        StorageManager storage = storage();
        storage.saveTuitionBook(new TuitionBook());

        MainApp.StartupState state = MainApp.loadStartupState(storage, new UserPrefs());

        assertTrue(state.model().getTuitionBook().getPersonList().isEmpty());
        assertTrue(state.notice().isEmpty());
    }

    @Test
    public void loadStartupState_cleanFile_hasNoRecoveryWarning() throws Exception {
        StorageManager storage = storage();
        storage.saveTuitionBook(seedu.tuitionbook.testutil.TypicalPersons.getTypicalTuitionBook());

        MainApp.StartupState state = MainApp.loadStartupState(storage, new UserPrefs());

        assertFalse(state.model().getTuitionBook().getPersonList().isEmpty());
        assertTrue(state.notice().isEmpty());
    }

    @Test
    public void loadStartupState_invalidGuardianLink_retainsContactsAndReportsRepair() throws Exception {
        Path dataFile = testFolder.resolve("contacts.json");
        Files.writeString(dataFile, """
                {"persons":[
                  {"id":"123e4567-e89b-12d3-a456-426614174000","role":"STUDENT",
                   "guardianId":"123e4567-e89b-12d3-a456-426614174099","name":"Sam Student",
                   "phone":"91234567","tags":[]},
                  {"id":"123e4567-e89b-12d3-a456-426614174001","role":"GUARDIAN",
                   "name":"Grace Guardian","phone":"92345678","tags":[]}
                ]}
                """);

        MainApp.StartupState state = MainApp.loadStartupState(storage(), new UserPrefs());

        assertEquals(2, state.model().getTuitionBook().getPersonList().size());
        assertTrue(state.model().getTuitionBook().getPersonList().get(0).getGuardianId().isEmpty());
        assertEquals(StartupNotice.guardianLinksRepaired(1), state.notice().orElseThrow());
    }

    @Test
    public void loadStartupState_malformedFile_blocksSavingAndPreservesOriginal() throws Exception {
        Path dataFile = testFolder.resolve("contacts.json");
        Files.writeString(dataFile, "{broken");
        StorageManager storage = storage();

        MainApp.StartupState state = MainApp.loadStartupState(storage, new UserPrefs());

        assertTrue(state.model().getTuitionBook().getPersonList().isEmpty());
        assertEquals(StartupNotice.loadFailed(), state.notice().orElseThrow());
        assertThrows(IOException.class, () -> storage.saveTuitionBook(state.model().getTuitionBook()));
        assertEquals("{broken", Files.readString(dataFile));
    }

    @Test
    public void initPrefs_missingFile_savesDefaultPreferences() throws Exception {
        JsonUserPrefsStorage prefsStorage = new JsonUserPrefsStorage(testFolder.resolve("preferences.json"));

        UserPrefs loadedPrefs = new MainApp().initPrefs(prefsStorage);

        assertEquals(new UserPrefs(), loadedPrefs);
        assertEquals(loadedPrefs, prefsStorage.readUserPrefs().orElseThrow());
    }

    @Test
    public void initPrefs_existingFile_retainsPreferences() throws Exception {
        JsonUserPrefsStorage prefsStorage = new JsonUserPrefsStorage(testFolder.resolve("preferences.json"));
        UserPrefs expectedPrefs = new UserPrefs();
        expectedPrefs.setGuiSettings(new GuiSettings(1000, 700, 25, 50));
        prefsStorage.saveUserPrefs(expectedPrefs);

        UserPrefs loadedPrefs = new MainApp().initPrefs(prefsStorage);

        assertEquals(expectedPrefs, loadedPrefs);
        assertEquals(expectedPrefs, prefsStorage.readUserPrefs().orElseThrow());
    }

    @Test
    public void initPrefs_malformedFile_usesAndSavesDefaults() throws Exception {
        Path prefsFile = testFolder.resolve("preferences.json");
        Files.writeString(prefsFile, "{broken");
        JsonUserPrefsStorage prefsStorage = new JsonUserPrefsStorage(prefsFile);

        UserPrefs loadedPrefs = new MainApp().initPrefs(prefsStorage);

        assertEquals(new UserPrefs(), loadedPrefs);
        assertEquals(loadedPrefs, prefsStorage.readUserPrefs().orElseThrow());
    }

    @Test
    public void initPrefs_saveFailure_stillReturnsDefaults() {
        Path prefsFile = testFolder.resolve("preferences.json");
        JsonUserPrefsStorage prefsStorage = failingPrefsStorage(prefsFile);

        assertEquals(new UserPrefs(), new MainApp().initPrefs(prefsStorage));
        assertFalse(Files.exists(prefsFile));
    }

    @Test
    public void stop_savesCurrentPreferences() throws Exception {
        JsonUserPrefsStorage prefsStorage = new JsonUserPrefsStorage(testFolder.resolve("preferences.json"));
        MainApp app = new MainApp();
        app.storage = storage();
        app.model = new ModelManager();
        GuiSettings settings = new GuiSettings(1100, 750, 30, 60);
        app.model.setGuiSettings(settings);

        app.stop();

        assertEquals(settings, prefsStorage.readUserPrefs().orElseThrow().getGuiSettings());
    }

    @Test
    public void stop_saveFailure_doesNotCrash() {
        Path prefsFile = testFolder.resolve("preferences.json");
        MainApp app = new MainApp();
        app.storage = new StorageManager(new JsonTuitionBookStorage(testFolder.resolve("contacts.json")),
                failingPrefsStorage(prefsFile));
        app.model = new ModelManager();

        assertDoesNotThrow(app::stop);
        assertFalse(Files.exists(prefsFile));
    }

    private JsonUserPrefsStorage failingPrefsStorage(Path prefsFile) {
        return new JsonUserPrefsStorage(prefsFile) {
            @Override
            public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
                throw new IOException("Simulated preference write failure");
            }
        };
    }

    private StorageManager storage() {
        return new StorageManager(new JsonTuitionBookStorage(testFolder.resolve("contacts.json")),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
    }
}
