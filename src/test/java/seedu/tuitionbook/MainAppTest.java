package seedu.tuitionbook;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.testutil.Assert.assertThrows;

import java.io.IOException;
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
import seedu.tuitionbook.storage.JsonTuitionBookStorage;
import seedu.tuitionbook.storage.JsonUserPrefsStorage;
import seedu.tuitionbook.storage.StorageManager;
import seedu.tuitionbook.testutil.PersonBuilder;
import seedu.tuitionbook.testutil.SymlinkTestUtil;
import seedu.tuitionbook.ui.StartupNotice;

/**
 * Tests the application startup outcome using disposable contact-data files.
 */
public class MainAppTest {

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
    public void initializeApplication_safeDisposablePaths_initializesWithoutStartingJavaFx() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("initialization-home"));
        Path dataDirectory = Files.createDirectory(applicationHome.resolve("data"));
        Path prefsFile = applicationHome.resolve("preferences.json");
        Path legacyFile = dataDirectory.resolve("addressbook.json");
        Path currentFile = dataDirectory.resolve("tuitionbook.json");
        byte[] legacyBytes = "{\"persons\":[]}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Files.write(legacyFile, legacyBytes);
        MainApp app = new MainApp();

        app.initializeApplication(applicationHome, prefsFile, legacyFile, currentFile);

        assertTrue(Files.exists(prefsFile));
        assertFalse(Files.exists(legacyFile));
        org.junit.jupiter.api.Assertions.assertArrayEquals(legacyBytes, Files.readAllBytes(currentFile));
        assertEquals(currentFile, app.storage.getTuitionBookFilePath());
        assertTrue(app.model.getTuitionBook().getPersonList().isEmpty());
        org.junit.jupiter.api.Assertions.assertNotNull(app.logic);
        org.junit.jupiter.api.Assertions.assertNotNull(app.ui);
    }

    @Test
    public void migrateLegacyTuitionBookFile_externalDataSymlink_rejectedWithoutMovingFile() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("application-home"));
        Path outsideData = Files.createDirectory(testFolder.resolve("outside-data"));
        Path legacyFile = outsideData.resolve("addressbook.json");
        Files.writeString(legacyFile, "legacy contents");
        Path dataLink = applicationHome.resolve("data");
        SymlinkTestUtil.createSymbolicLink(dataLink, outsideData);

        org.junit.jupiter.api.Assertions.assertThrows(IOException.class, () -> MainApp.migrateLegacyTuitionBookFile(
                applicationHome, dataLink.resolve("addressbook.json"), dataLink.resolve("tuitionbook.json")));

        assertTrue(Files.isSymbolicLink(dataLink));
        assertEquals("legacy contents", Files.readString(legacyFile));
        assertFalse(Files.exists(outsideData.resolve("tuitionbook.json")));
    }

    @Test
    public void migrateLegacyTuitionBookFile_existingLegacyFile_movesBytesToCurrentPath() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("migration-home"));
        Path dataDirectory = Files.createDirectory(applicationHome.resolve("data"));
        Path legacyFile = dataDirectory.resolve("addressbook.json");
        Path currentFile = dataDirectory.resolve("tuitionbook.json");
        byte[] legacyBytes = {0, 1, 2, 3, -1};
        Files.write(legacyFile, legacyBytes);

        MainApp.migrateLegacyTuitionBookFile(applicationHome, legacyFile, currentFile);

        assertFalse(Files.exists(legacyFile));
        org.junit.jupiter.api.Assertions.assertArrayEquals(legacyBytes, Files.readAllBytes(currentFile));
    }

    @Test
    public void migrateLegacyTuitionBookFile_currentFileExists_leavesBothFilesUnchanged() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("existing-current-home"));
        Path dataDirectory = Files.createDirectory(applicationHome.resolve("data"));
        Path legacyFile = dataDirectory.resolve("addressbook.json");
        Path currentFile = dataDirectory.resolve("tuitionbook.json");
        Files.writeString(legacyFile, "legacy contents");
        Files.writeString(currentFile, "current contents");

        MainApp.migrateLegacyTuitionBookFile(applicationHome, legacyFile, currentFile);

        assertEquals("legacy contents", Files.readString(legacyFile));
        assertEquals("current contents", Files.readString(currentFile));
    }

    @Test
    public void migrateLegacyTuitionBookFile_bothFilesMissing_leavesPathsMissing() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("missing-migration-home"));
        Path dataDirectory = Files.createDirectory(applicationHome.resolve("data"));
        Path legacyFile = dataDirectory.resolve("addressbook.json");
        Path currentFile = dataDirectory.resolve("tuitionbook.json");

        MainApp.migrateLegacyTuitionBookFile(applicationHome, legacyFile, currentFile);

        assertFalse(Files.exists(legacyFile));
        assertFalse(Files.exists(currentFile));
    }

    @Test
    public void verifyStartupPaths_externalDataSymlink_rejectedBeforeJavaFxLaunch() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("preflight-home"));
        Path outsideData = Files.createDirectory(testFolder.resolve("preflight-outside"));
        Path dataLink = applicationHome.resolve("data");
        SymlinkTestUtil.createSymbolicLink(dataLink, outsideData);

        IOException exception = org.junit.jupiter.api.Assertions.assertThrows(
                IOException.class, () -> MainApp.verifyStartupPaths(applicationHome,
                        applicationHome.resolve("preferences.json"), dataLink.resolve("addressbook.json"),
                        dataLink.resolve("tuitionbook.json")));

        assertTrue(exception.getMessage().contains("resolves outside the application folder"));
        assertTrue(Files.isSymbolicLink(dataLink));
    }

    @Test
    public void verifyStartupPaths_missingContainedPaths_acceptedWithoutCreatingFiles() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("missing-paths-home"));
        Path prefsFile = applicationHome.resolve("preferences.json");
        Path legacyFile = applicationHome.resolve("data/addressbook.json");
        Path currentFile = applicationHome.resolve("data/tuitionbook.json");

        assertDoesNotThrow(() -> MainApp.verifyStartupPaths(
                applicationHome, prefsFile, legacyFile, currentFile));

        assertFalse(Files.exists(prefsFile));
        assertFalse(Files.exists(legacyFile));
        assertFalse(Files.exists(currentFile));
    }

    @Test
    public void verifyStartupPaths_defaultApplicationPaths_accepted() {
        assertDoesNotThrow(() -> MainApp.verifyStartupPaths());
    }

    @Test
    public void verifyStartupPaths_eachManagedPathUnsafe_rejectsAllThreePositions() throws Exception {
        for (int unsafePathIndex = 0; unsafePathIndex < 3; unsafePathIndex++) {
            Path applicationHome = Files.createDirectory(testFolder.resolve("preflight-home-" + unsafePathIndex));
            Path dataDirectory = Files.createDirectory(applicationHome.resolve("data"));
            Path outsideFile = testFolder.resolve("preflight-outside-" + unsafePathIndex);
            Files.writeString(outsideFile, "outside contents");
            Path[] managedPaths = {
                applicationHome.resolve("preferences.json"),
                dataDirectory.resolve("addressbook.json"),
                dataDirectory.resolve("tuitionbook.json")
            };
            SymlinkTestUtil.createSymbolicLink(managedPaths[unsafePathIndex], outsideFile);

            IOException exception = org.junit.jupiter.api.Assertions.assertThrows(
                    IOException.class, () -> MainApp.verifyStartupPaths(applicationHome,
                            managedPaths[0], managedPaths[1], managedPaths[2]));

            assertTrue(exception.getMessage().contains("resolves outside the application folder"));
            assertEquals("outside contents", Files.readString(outsideFile));
            assertTrue(Files.isSymbolicLink(managedPaths[unsafePathIndex]));
        }
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
    public void loadStartupState_externalDataSymlink_refusesStartupWithActionableMessage() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("startup-home"));
        Path dataDirectory = Files.createDirectory(applicationHome.resolve("data"));
        Path outsideFile = testFolder.resolve("startup-outside.json");
        Files.writeString(outsideFile, "{\"persons\":[]}");
        Path dataFile = dataDirectory.resolve("tuitionbook.json");
        SymlinkTestUtil.createSymbolicLink(dataFile, outsideFile);
        StorageManager storage = new StorageManager(new JsonTuitionBookStorage(dataFile, applicationHome),
                new JsonUserPrefsStorage(applicationHome.resolve("preferences.json"), applicationHome));

        IllegalStateException exception = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class, () -> MainApp.loadStartupState(storage, new UserPrefs()));

        assertTrue(exception.getMessage().contains("resolves outside the application folder"));
        assertTrue(exception.getMessage().contains("Correct or remove the symbolic link"));
        assertEquals("{\"persons\":[]}", Files.readString(outsideFile));
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
        assertEquals(StartupNotice.guardianLinksRepaired(1), state.notice().orElseThrow());
    }

    @Test
    public void loadStartupState_malformedFile_createsBackupAndAllowsSaving() throws Exception {
        Path dataFile = testFolder.resolve("contacts.json");
        Files.writeString(dataFile, "{broken");
        StorageManager storage = storage();

        MainApp.StartupState state = MainApp.loadStartupState(storage, new UserPrefs());

        assertTrue(state.model().getTuitionBook().getPersonList().isEmpty());
        Path backupPath = dataFile.resolveSibling(dataFile.getFileName() + ".backup");
        assertEquals(StartupNotice.loadFailedBackedUp(backupPath), state.notice().orElseThrow());
        assertFalse(Files.exists(dataFile));
        assertEquals("{broken", Files.readString(backupPath));
        storage.saveTuitionBook(state.model().getTuitionBook());
        assertTrue(Files.exists(dataFile));
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

        assertEquals(StartupNotice.loadFailedBackupFailed(), state.notice().orElseThrow());
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
    public void initPrefs_externalSymlink_refusesStartupAndPreservesTarget() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("prefs-home"));
        Path outsideFile = testFolder.resolve("outside-prefs.json");
        Files.writeString(outsideFile, "{}");
        Path preferencesFile = applicationHome.resolve("preferences.json");
        SymlinkTestUtil.createSymbolicLink(preferencesFile, outsideFile);
        JsonUserPrefsStorage prefsStorage = new JsonUserPrefsStorage(preferencesFile, applicationHome);

        IllegalStateException exception = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class, () -> new MainApp().initPrefs(prefsStorage));

        assertTrue(exception.getMessage().contains("resolves outside the application folder"));
        assertEquals("{}", Files.readString(outsideFile));
        assertTrue(Files.isSymbolicLink(preferencesFile));
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
        assertEquals(StartupNotice.loadFailedBackedUp(backupFile), state.notice().orElseThrow());
        assertFalse(Files.exists(dataFile));
        assertEquals(contents, Files.readString(backupFile));
        storage.saveTuitionBook(state.model().getTuitionBook());
        assertTrue(Files.exists(dataFile));
    }
}
