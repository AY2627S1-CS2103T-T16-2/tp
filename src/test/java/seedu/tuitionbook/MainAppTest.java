package seedu.tuitionbook;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.tuitionbook.commons.core.GuiSettings;
import seedu.tuitionbook.logic.Logic;
import seedu.tuitionbook.logic.LogicManager;
import seedu.tuitionbook.logic.commands.CommandResult;
import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.ReadOnlyUserPrefs;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.UserPrefs;
import seedu.tuitionbook.storage.GuardianLinkRepair;
import seedu.tuitionbook.model.person.Phone;
import seedu.tuitionbook.storage.JsonTuitionBookStorage;
import seedu.tuitionbook.storage.JsonUserPrefsStorage;
import seedu.tuitionbook.storage.StorageManager;
import seedu.tuitionbook.testutil.PersonBuilder;
import seedu.tuitionbook.ui.StartupNotice;

/**
 * Tests the application startup outcome using disposable contact-data files.
 */
public class MainAppTest {

    private static final String INVALID_JSON_GUIDANCE = "The data file is not valid UTF-8 JSON. Correct the encoding "
            + "or JSON syntax in the preserved file before trying again.";

    @TempDir
    public Path testFolder;

    @Test
    public void loadStartupState_missingFile_startsEmptyAndShowsNotice() {
        MainApp.StartupState state = MainApp.loadStartupState(storage(), new UserPrefs());

        assertFalse(Files.exists(testFolder.resolve("contacts.json")));
        assertTrue(state.model().getTuitionBook().getPersonList().isEmpty());
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
    public void loadStartupState_nullRoot_startsEmptyAndPreservesBackup() throws Exception {
        assertMalformedDataStartsEmptyAndPreservesBackup("null");
    }

    @Test
    public void loadStartupState_nullPerson_startsEmptyAndPreservesBackup() throws Exception {
        assertMalformedDataStartsEmptyAndPreservesBackup("{\"persons\":[null]}");
    }

    @Test
    public void loadStartupState_nullTag_startsEmptyAndPreservesBackup() throws Exception {
        assertMalformedDataStartsEmptyAndPreservesBackup("""
                {"persons":[{"name":"Test Student","phone":"81234567","tags":[null]}]}
                """);
    }

    @Test
    public void init_migrationUsesLegacyFallback_initializesStorageAndModelFromLegacyFile() throws Exception {
        Path legacyFile = testFolder.resolve("addressbook.json");
        Files.writeString(legacyFile, """
                {"persons":[{"name":"Legacy Alex","phone":"87438807","tags":[]}]}
                """);
        MainApp app = new MainApp() {
            @Override
            protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
                return new UserPrefs();
            }

            @Override
            Path migrateLegacyTuitionBookFile(Path currentFilePath, Path legacyFilePath) {
                return legacyFile;
            }
        };

        assertDoesNotThrow(app::init);

        assertEquals(legacyFile, app.storage.getTuitionBookFilePath());
        assertEquals("Legacy Alex", app.model.getTuitionBook().getPersonList().getFirst().getName().fullName);
    }

    @Test
    public void migrateLegacyTuitionBookFile_moveFails_usesUnchangedLegacyFile() throws Exception {
        Path currentFile = testFolder.resolve("tuitionbook.json");
        Path legacyFile = testFolder.resolve("addressbook.json");
        String originalContents = "{\"persons\":[]}";
        Files.writeString(legacyFile, originalContents);
        MainApp app = new MainApp() {
            @Override
            protected void moveLegacyTuitionBookFile(Path source, Path target) throws IOException {
                throw new IOException("simulated permission failure");
            }
        };

        Path selectedFile = assertDoesNotThrow(() -> app.migrateLegacyTuitionBookFile(currentFile, legacyFile));

        assertEquals(legacyFile, selectedFile);
        assertEquals(originalContents, Files.readString(legacyFile));
        assertFalse(Files.exists(currentFile));
    }

    @Test
    public void migrateLegacyTuitionBookFile_moveSucceeds_usesCurrentFile() throws Exception {
        Path currentFile = testFolder.resolve("tuitionbook.json");
        Path legacyFile = testFolder.resolve("addressbook.json");
        String originalContents = "{\"persons\":[]}";
        Files.writeString(legacyFile, originalContents);

        Path selectedFile = new MainApp().migrateLegacyTuitionBookFile(currentFile, legacyFile);

        assertEquals(currentFile, selectedFile);
        assertEquals(originalContents, Files.readString(currentFile));
        assertFalse(Files.exists(legacyFile));
    }

    @Test
    public void migrateLegacyTuitionBookFile_currentFileExists_usesCurrentAndLeavesLegacyFile() throws Exception {
        Path currentFile = testFolder.resolve("tuitionbook.json");
        Path legacyFile = testFolder.resolve("addressbook.json");
        String currentContents = "{\"persons\":[]}";
        String legacyContents = "{\"persons\":[{\"name\":\"Legacy Alex\",\"phone\":\"87438807\",\"tags\":[]}]}";
        Files.writeString(currentFile, currentContents);
        Files.writeString(legacyFile, legacyContents);

        Path selectedFile = new MainApp().migrateLegacyTuitionBookFile(currentFile, legacyFile);

        assertEquals(currentFile, selectedFile);
        assertEquals(currentContents, Files.readString(currentFile));
        assertEquals(legacyContents, Files.readString(legacyFile));
    }

    @Test
    public void migrateLegacyTuitionBookFile_filesMissing_usesCurrentFilePath() {
        Path currentFile = testFolder.resolve("tuitionbook.json");
        Path legacyFile = testFolder.resolve("addressbook.json");

        Path selectedFile = new MainApp().migrateLegacyTuitionBookFile(currentFile, legacyFile);

        assertEquals(currentFile, selectedFile);
        assertFalse(Files.exists(currentFile));
        assertFalse(Files.exists(legacyFile));
    }

    @Test
    public void clearThenRestart_existingEmptyDataFileDoesNotRestoreContacts() throws Exception {
        StorageManager storage = storage();
        TuitionBook tuitionBook = new TuitionBook();
        tuitionBook.addPerson(new PersonBuilder().build());
        storage.saveTuitionBook(tuitionBook);

        MainApp.StartupState startupState = MainApp.loadStartupState(storage, new UserPrefs());
        Model model = startupState.model();
        assertTrue(model.getTuitionBook().getPersonList().contains(tuitionBook.getPersonList().get(0)));
        Logic logic = new LogicManager(model, storage);
        logic.execute("clear confirm");

        MainApp.StartupState restartedState = MainApp.loadStartupState(storage, new UserPrefs());

        assertTrue(Files.exists(testFolder.resolve("contacts.json")));
        assertTrue(restartedState.model().getTuitionBook().getPersonList().isEmpty());
        assertTrue(restartedState.notice().isEmpty());
    }

    @Test
    public void documentedViewExamples_fromEmptyStartup_succeed() throws Exception {
        StorageManager storage = storage();
        Model model = MainApp.loadStartupState(storage, new UserPrefs()).model();
        Logic logic = new LogicManager(model, storage);
        logic.execute("add r/student n/Alex Yeoh p/87438807");
        logic.execute("add r/guardian n/Bernice Yu p/99272758");
        logic.execute("link 1 2");
        logic.execute("add r/student n/Charlotte Oliveiro p/93210283");
        logic.execute("add r/guardian n/David Li p/91031282");

        assertViewContains(logic.execute("view 1"), "Alex Yeoh", "Guardian: Bernice Yu (99272758)");
        assertViewContains(logic.execute("view 1 v/"), "Alex Yeoh", "Guardian details:", "Phone: 99272758");
        assertViewContains(logic.execute("view 2"), "Bernice Yu", "Linked students:", "Alex Yeoh (87438807)");
        assertViewContains(logic.execute("view 2 v/"), "Bernice Yu", "Name: Alex Yeoh", "Phone: 87438807");
        assertViewContains(logic.execute("view 3"), "Charlotte Oliveiro", "No guardian linked.");
        assertViewContains(logic.execute("view 4"), "David Li", "No students linked.");
        logic.execute("list r/student");
        assertViewContains(logic.execute("view 1"), "Alex Yeoh", "Guardian: Bernice Yu (99272758)");
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
        StartupNotice notice = state.notice().orElseThrow();
        assertEquals(1, notice.repairedGuardianLinkCount());
        assertEquals("Sam Student", notice.guardianLinkRepairs().get(0).personName());
        assertEquals(GuardianLinkRepair.Reason.MISSING_GUARDIAN,
                notice.guardianLinkRepairs().get(0).reason());
    }

    @Test
    public void loadStartupState_malformedFile_createsBackupAndAllowsSaving() throws Exception {
        Path dataFile = testFolder.resolve("contacts.json");
        Files.writeString(dataFile, "{broken");
        StorageManager storage = storage();

        MainApp.StartupState state = MainApp.loadStartupState(storage, new UserPrefs());

        assertTrue(state.model().getTuitionBook().getPersonList().isEmpty());
        Path backupPath = dataFile.resolveSibling(dataFile.getFileName() + ".backup");
        assertEquals(StartupNotice.loadFailedBackedUp(backupPath, INVALID_JSON_GUIDANCE),
                state.notice().orElseThrow());
        assertFalse(Files.exists(dataFile));
        assertEquals("{broken", Files.readString(backupPath));
        storage.saveTuitionBook(state.model().getTuitionBook());
        assertTrue(Files.exists(dataFile));
    }

    @Test
    public void loadStartupState_invalidUtf8_reportsEncodingGuidanceAndPreservesBytes() throws Exception {
        // Scenario adapted from https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/196
        Path dataFile = testFolder.resolve("contacts.json");
        String json = "{\"persons\":[{\"name\":\"Valid Name\",\"phone\":\"81234567\","
                + "\"address\":\"X\",\"tags\":[]}]}";
        byte[] originalContents = json.getBytes(StandardCharsets.UTF_8);
        originalContents[json.indexOf('X')] = (byte) 0x80;
        Files.write(dataFile, originalContents);

        MainApp.StartupState state = MainApp.loadStartupState(storage(), new UserPrefs());

        Path backupPath = dataFile.resolveSibling(dataFile.getFileName() + ".backup");
        assertEquals(StartupNotice.loadFailedBackedUp(backupPath, INVALID_JSON_GUIDANCE),
                state.notice().orElseThrow());
        assertFalse(Files.exists(dataFile));
        assertArrayEquals(originalContents, Files.readAllBytes(backupPath));
    }

    @Test
    public void loadStartupState_invalidPhone_reportsValidatorGuidanceAndPreservesBackup() throws Exception {
        Path dataFile = testFolder.resolve("contacts.json");
        Files.writeString(dataFile, """
                {"persons":[{"name":"Invalid Phone","phone":"not-a-phone","tags":[]}]}
                """);

        MainApp.StartupState state = MainApp.loadStartupState(storage(), new UserPrefs());

        StartupNotice notice = state.notice().orElseThrow();
        assertEquals(StartupNotice.Type.LOAD_FAILED_BACKED_UP, notice.type());
        assertTrue(notice.loadFailureGuidance().contains("invalid contact data"));
        assertTrue(notice.loadFailureGuidance().contains(Phone.MESSAGE_CONSTRAINTS));
        assertFalse(Files.exists(dataFile));
        assertTrue(Files.exists(dataFile.resolveSibling(dataFile.getFileName() + ".backup")));
    }

    @Test
    public void loadStartupState_malformedFileBackupFails_blocksSavingAndShowsNotice() throws Exception {
        Path dataFile = testFolder.resolve("contacts.json");
        Files.writeString(dataFile, "{broken");
        JsonTuitionBookStorage tuitionBookStorage = new JsonTuitionBookStorage(dataFile) {
            @Override
            protected void moveFile(Path source, Path target) throws IOException {
                throw new IOException("simulated backup failure");
            }
        };
        StorageManager storage = new StorageManager(tuitionBookStorage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));

        MainApp.StartupState state = MainApp.loadStartupState(storage, new UserPrefs());

        assertEquals(StartupNotice.loadFailedBackupFailed(INVALID_JSON_GUIDANCE), state.notice().orElseThrow());
        assertEquals("{broken", Files.readString(dataFile));
        assertThrows(IOException.class, () -> storage.saveTuitionBook(state.model().getTuitionBook()));
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

    private void assertViewContains(CommandResult result, String... expectedFragments) {
        String details = result.getDetailsToShow().orElseThrow();
        for (String expectedFragment : expectedFragments) {
            assertTrue(details.contains(expectedFragment), () ->
                    "Missing <" + expectedFragment + "> in:\n" + details);
        }
    }
    private void assertMalformedDataStartsEmptyAndPreservesBackup(String contents) throws Exception {
        Path dataFile = testFolder.resolve("contacts.json");
        Path backupFile = dataFile.resolveSibling(dataFile.getFileName() + ".backup");
        Files.writeString(dataFile, contents);
        StorageManager storage = storage();

        MainApp.StartupState state = MainApp.loadStartupState(storage, new UserPrefs());

        assertTrue(state.model().getTuitionBook().getPersonList().isEmpty());
        StartupNotice notice = state.notice().orElseThrow();
        assertEquals(StartupNotice.Type.LOAD_FAILED_BACKED_UP, notice.type());
        assertEquals(backupFile, notice.malformedFileBackupPath());
        assertFalse(notice.loadFailureGuidance().isBlank());
        assertFalse(Files.exists(dataFile));
        assertEquals(contents, Files.readString(backupFile));
        storage.saveTuitionBook(state.model().getTuitionBook());
        assertTrue(Files.exists(dataFile));
    }
}
