package seedu.tuitionbook.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.testutil.Assert.assertThrows;
import static seedu.tuitionbook.testutil.TypicalPersons.ALICE;
import static seedu.tuitionbook.testutil.TypicalPersons.HOON;
import static seedu.tuitionbook.testutil.TypicalPersons.IDA;
import static seedu.tuitionbook.testutil.TypicalPersons.getTypicalTuitionBook;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.tuitionbook.commons.core.index.Index;
import seedu.tuitionbook.commons.exceptions.DataLoadingException;
import seedu.tuitionbook.logic.commands.UnlinkCommand;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.ReadOnlyTuitionBook;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.UserPrefs;
import seedu.tuitionbook.model.person.Address;
import seedu.tuitionbook.model.person.Name;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.PersonMatchesListFilterPredicate;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.model.tag.Tag;
import seedu.tuitionbook.testutil.PersonBuilder;
import seedu.tuitionbook.testutil.SymlinkTestUtil;

public class JsonTuitionBookStorageTest {
    private static final String CONFLICTING_TUITION_BOOK_JSON = """
            {
              "persons" : [ {
                "name" : "Alice",
                "phone" : "9123 4567",
                "email" : "alice@example.com",
                "address" : "Alice Street",
                "tags" : [ ]
              }, {
                "name" : "alice",
                "phone" : "9123-4567",
                "email" : "other-alice@example.com",
                "address" : "Other Street",
                "tags" : [ ]
              }, {
                "name" : "Bob",
                "phone" : "98765432",
                "email" : "bob@example.com",
                "address" : "Bob Street",
                "tags" : [ ]
              } ]
            }
            """;
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonTuitionBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readTuitionBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readTuitionBook(null));
    }

    @Test
    public void saveAndRead_specialCharactersAndLegacyTags_roundTripAndRemainFilterable() throws Exception {
        Person person = new PersonBuilder().withRole(Role.STUDENT).withTags("friend", "Sec-4", "Math  Science",
                "\"quotes\"", "C:\\notes", "<b>&</b>", "Math--Science", "!@#$%^&*()_+={}[]:;'<>,.?/\\|~`").build();
        TuitionBook original = new TuitionBook();
        original.addPerson(person);
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(testFolder.resolve("tags.json"));
        storage.saveTuitionBook(original);
        ModelManager restored = new ModelManager(storage.readTuitionBook().orElseThrow(), new UserPrefs());
        assertEquals(original, restored.getTuitionBook());
        assertEquals(person.getTags(), restored.getTuitionBook().getPersonList().getFirst().getTags());
        restored.updateFilteredPersonList(new PersonMatchesListFilterPredicate(Role.STUDENT,
                List.of("math  science", "\"QUOTES\"", "c:\\NOTES")));
        assertEquals(List.of(person), restored.getFilteredPersonList());
    }

    private java.util.Optional<ReadOnlyTuitionBook> readTuitionBook(String filePath) throws Exception {
        return new JsonTuitionBookStorage(Paths.get(filePath)).readTuitionBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readTuitionBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_symlinkedParentOutsideApplicationHome_rejected() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("application-home"));
        Path outsideDirectory = Files.createDirectory(testFolder.resolve("outside"));
        Path outsideFile = outsideDirectory.resolve("tuitionbook.json");
        Files.writeString(outsideFile, "{\"persons\":[]}");
        SymlinkTestUtil.createSymbolicLink(applicationHome.resolve("data"), outsideDirectory);
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(
                applicationHome.resolve("data").resolve("tuitionbook.json"), applicationHome);

        org.junit.jupiter.api.Assertions.assertThrows(DataLoadingException.class, storage::readTuitionBook);
        assertEquals("{\"persons\":[]}", Files.readString(outsideFile));
    }

    @Test
    public void read_externalAdHocPath_rejectedWithoutBlockingConfiguredFile() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("ad-hoc-read-home"));
        Path configuredFile = applicationHome.resolve("data/tuitionbook.json");
        Path outsideFile = testFolder.resolve("ad-hoc-outside.json");
        Files.writeString(outsideFile, "{\"persons\":[]}");
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(configuredFile, applicationHome);

        assertThrows(DataLoadingException.class, () -> storage.readTuitionBook(outsideFile));
        storage.saveTuitionBook(new TuitionBook());

        assertEquals("{\"persons\":[]}", Files.readString(outsideFile));
        assertTrue(Files.isRegularFile(configuredFile));
    }

    @Test
    public void save_symlinkedParentOutsideApplicationHome_rejectedAndTargetUnchanged() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("application-home"));
        Path outsideDirectory = Files.createDirectory(testFolder.resolve("outside"));
        Path outsideFile = outsideDirectory.resolve("tuitionbook.json");
        Files.writeString(outsideFile, "outside contents");
        Path dataLink = applicationHome.resolve("data");
        SymlinkTestUtil.createSymbolicLink(dataLink, outsideDirectory);
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(
                dataLink.resolve("tuitionbook.json"), applicationHome);

        assertThrows(IOException.class, () -> storage.saveTuitionBook(new TuitionBook()));
        assertTrue(Files.isSymbolicLink(dataLink));
        assertEquals("outside contents", Files.readString(outsideFile));
    }

    @Test
    public void readAndSave_finalFileSymlinkOutside_rejectedAndLinkAndTargetUnchanged() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("application-home"));
        Path dataDirectory = Files.createDirectory(applicationHome.resolve("data"));
        Path outsideFile = testFolder.resolve("outside.json");
        Files.writeString(outsideFile, "{\"persons\":[]}");
        Path dataLink = dataDirectory.resolve("tuitionbook.json");
        SymlinkTestUtil.createSymbolicLink(dataLink, outsideFile);
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(dataLink, applicationHome);

        assertThrows(DataLoadingException.class, storage::readTuitionBook);
        assertThrows(IOException.class, () -> storage.saveTuitionBook(new TuitionBook()));
        assertTrue(Files.isSymbolicLink(dataLink));
        assertEquals("{\"persons\":[]}", Files.readString(outsideFile));
    }

    @Test
    public void read_malformedExternalFileSymlink_doesNotBackUpOrMoveExternalData() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("application-home"));
        Path dataDirectory = Files.createDirectory(applicationHome.resolve("data"));
        Path outsideFile = testFolder.resolve("outside.json");
        Files.writeString(outsideFile, "{broken");
        Path dataLink = dataDirectory.resolve("tuitionbook.json");
        SymlinkTestUtil.createSymbolicLink(dataLink, outsideFile);
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(dataLink, applicationHome);

        DataLoadingException exception = org.junit.jupiter.api.Assertions.assertThrows(
                DataLoadingException.class, storage::readTuitionBook);

        assertTrue(exception.getMalformedFileBackupPath().isEmpty());
        assertTrue(Files.isSymbolicLink(dataLink));
        assertFalse(Files.exists(dataDirectory.resolve("tuitionbook.json.backup")));
        assertEquals("{broken", Files.readString(outsideFile));
    }

    @Test
    public void readAndSave_internalParentSymlink_succeeds() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("application-home"));
        Path internalData = Files.createDirectory(applicationHome.resolve("internal-data"));
        Path dataLink = applicationHome.resolve("data");
        SymlinkTestUtil.createSymbolicLink(dataLink, internalData);
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(
                dataLink.resolve("tuitionbook.json"), applicationHome);

        storage.saveTuitionBook(getTypicalTuitionBook());

        assertEquals(getTypicalTuitionBook(), new TuitionBook(storage.readTuitionBook().orElseThrow()));
        assertTrue(Files.exists(internalData.resolve("tuitionbook.json")));
    }

    @Test
    public void save_pathChangedToOutsideSymlinkSinceRead_revalidatesAndRejects() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("application-home"));
        Path dataDirectory = Files.createDirectory(applicationHome.resolve("data"));
        Path dataFile = dataDirectory.resolve("tuitionbook.json");
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(dataFile, applicationHome);
        storage.saveTuitionBook(new TuitionBook());
        storage.readTuitionBook();
        Files.delete(dataFile);
        Path outsideFile = testFolder.resolve("outside.json");
        Files.writeString(outsideFile, "outside contents");
        SymlinkTestUtil.createSymbolicLink(dataFile, outsideFile);

        assertThrows(IOException.class, () -> storage.saveTuitionBook(getTypicalTuitionBook()));
        assertTrue(Files.isSymbolicLink(dataFile));
        assertEquals("outside contents", Files.readString(outsideFile));
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readTuitionBook("notJsonFormatTuitionBook.json"));
    }

    @Test
    public void readTuitionBook_nullRoot_movesDataToBackupAndAllowsSaving() throws Exception {
        assertMalformedDataIsBackedUpAndSavingIsAllowed("null");
    }

    @Test
    public void readTuitionBook_nullPerson_movesDataToBackupAndAllowsSaving() throws Exception {
        assertMalformedDataIsBackedUpAndSavingIsAllowed("{\"persons\":[null]}");
    }

    @Test
    public void readTuitionBook_nullTag_movesDataToBackupAndAllowsSaving() throws Exception {
        assertMalformedDataIsBackedUpAndSavingIsAllowed("""
                {"persons":[{"name":"Test Student","phone":"81234567","tags":[null]}]}
                """);
    }

    @Test
    public void readTuitionBook_invalidPersonTuitionBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readTuitionBook("invalidPersonTuitionBook.json"));
    }

    @Test
    public void readTuitionBook_invalidAndValidPersonTuitionBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readTuitionBook("invalidAndValidPersonTuitionBook.json"));
    }

    @Test
    public void readTuitionBook_overLengthFields_movesFileToBackupAndAllowsSaving() throws Exception {
        assertOverLengthConfiguredFileIsBackedUp("OverLengthNameTuitionBook.json",
                "A".repeat(Name.MAX_LENGTH + 1), "Valid Address");
        assertOverLengthConfiguredFileIsBackedUp("OverLengthAddressTuitionBook.json",
                "Valid Name", "A".repeat(Address.MAX_LENGTH + 1));
    }

    @Test
    public void readTuitionBook_overLengthBackupFailure_preservesOriginalAndBlocksSave() throws Exception {
        assertOverLengthConfiguredFileIsPreservedAfterBackupFailure("BlockedOverLengthNameTuitionBook.json",
                "A".repeat(Name.MAX_LENGTH + 1), "Valid Address");
        assertOverLengthConfiguredFileIsPreservedAfterBackupFailure("BlockedOverLengthAddressTuitionBook.json",
                "Valid Name", "A".repeat(Address.MAX_LENGTH + 1));
    }

    @Test
    public void readAndSaveTuitionBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempTuitionBook.json");
        TuitionBook original = getTypicalTuitionBook();
        JsonTuitionBookStorage jsonTuitionBookStorage = new JsonTuitionBookStorage(filePath);

        // Save in new file and read back
        jsonTuitionBookStorage.saveTuitionBook(original, filePath);
        ReadOnlyTuitionBook readBack = jsonTuitionBookStorage.readTuitionBook(filePath).get();
        assertEquals(original, new TuitionBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonTuitionBookStorage.saveTuitionBook(original, filePath);
        readBack = jsonTuitionBookStorage.readTuitionBook(filePath).get();
        assertEquals(original, new TuitionBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonTuitionBookStorage.saveTuitionBook(original); // file path not specified
        readBack = jsonTuitionBookStorage.readTuitionBook().get(); // file path not specified
        assertEquals(original, new TuitionBook(readBack));

    }

    @Test
    public void saveAndRead_missingDirectoriesInsideApplicationHome_createdSuccessfully() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("application-home"));
        Path filePath = applicationHome.resolve("missing/data/tuitionbook.json");
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(filePath, applicationHome);

        storage.saveTuitionBook(getTypicalTuitionBook());

        assertTrue(Files.isRegularFile(filePath));
        assertEquals(getTypicalTuitionBook(), new TuitionBook(storage.readTuitionBook().orElseThrow()));
    }

    @Test
    public void readAndSaveTuitionBook_personWithoutEmailAndAddress_preservesAbsence() throws Exception {
        Path filePath = testFolder.resolve("OptionalFieldsTuitionBook.json");
        Person person = new PersonBuilder().withoutEmail().withoutAddress().build();
        TuitionBook original = new TuitionBook();
        original.addPerson(person);
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(filePath);

        storage.saveTuitionBook(original);
        ReadOnlyTuitionBook readBack = storage.readTuitionBook().orElseThrow();

        assertEquals(original, new TuitionBook(readBack));
        Person restoredPerson = readBack.getPersonList().get(0);
        assertTrue(restoredPerson.getEmail().isEmpty());
        assertTrue(restoredPerson.getAddress().isEmpty());
    }

    @Test
    public void readAndSaveTuitionBook_longMultiSegmentTag_preservesExactValue() throws Exception {
        Path filePath = testFolder.resolve("TagsTuitionBook.json");
        String longMultiSegmentTag = "A-".repeat(5000) + "B";
        Person person = new PersonBuilder().withTags("Sec4", longMultiSegmentTag).build();
        TuitionBook original = new TuitionBook();
        original.addPerson(person);
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(filePath);

        storage.saveTuitionBook(original);
        ReadOnlyTuitionBook readBack = storage.readTuitionBook().orElseThrow();

        assertEquals(original, new TuitionBook(readBack));
        assertEquals(Set.of(new Tag("Sec4"), new Tag(longMultiSegmentTag)),
                readBack.getPersonList().get(0).getTags());
    }

    @Test
    public void readAndSaveTuitionBook_linkedStudentAndGuardian_preservesIdsRolesAndLink() throws Exception {
        Path filePath = testFolder.resolve("LinkedContactsTuitionBook.json");
        Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("90000001")
                .withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Sam Student").withPhone("90000002")
                .withRole(Role.STUDENT).withGuardian(guardian).withoutEmail().withoutAddress().build();
        TuitionBook original = new TuitionBook();
        original.addPerson(student);
        original.addPerson(guardian);
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(filePath);

        storage.saveTuitionBook(original);
        ReadOnlyTuitionBook readBack = storage.readTuitionBook().orElseThrow();
        Person restoredStudent = readBack.getPersonList().get(0);
        Person restoredGuardian = readBack.getPersonList().get(1);

        assertEquals(student.getId(), restoredStudent.getId());
        assertEquals(Role.STUDENT, restoredStudent.getRole());
        assertEquals(guardian.getId(), restoredGuardian.getId());
        assertEquals(Role.GUARDIAN, restoredGuardian.getRole());
        assertEquals(restoredGuardian.getId(), restoredStudent.getGuardianId().orElseThrow());
        assertTrue(restoredStudent.getEmail().isEmpty());
        assertTrue(restoredStudent.getAddress().isEmpty());
    }

    @Test
    public void readAndSaveTuitionBook_unlinkedStudent_hasNoGuardianAfterReload() throws Exception {
        Path filePath = testFolder.resolve("UnlinkedContactsTuitionBook.json");
        Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("90000001")
                .withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Sam Student").withPhone("90000002")
                .withRole(Role.STUDENT).withGuardian(guardian).build();
        TuitionBook tuitionBook = new TuitionBook();
        tuitionBook.addPerson(student);
        tuitionBook.addPerson(guardian);
        ModelManager model = new ModelManager(tuitionBook, new UserPrefs());
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(filePath);

        new UnlinkCommand(Index.fromOneBased(1)).execute(model);
        storage.saveTuitionBook(model.getTuitionBook());

        ReadOnlyTuitionBook readBack = storage.readTuitionBook().orElseThrow();
        Person restoredStudent = readBack.getPersonList().get(0);

        assertEquals(model.getTuitionBook(), new TuitionBook(readBack));
        assertTrue(restoredStudent.getGuardianId().isEmpty());
    }

    @Test
    public void readTuitionBook_conflictingConfiguredFile_movesFileToBackupAndAllowsSave() throws Exception {
        Path filePath = testFolder.resolve("ConflictingTuitionBook.json");
        Files.writeString(filePath, CONFLICTING_TUITION_BOOK_JSON);
        String originalContents = Files.readString(filePath);
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(filePath);

        assertThrows(DataLoadingException.class, storage::readTuitionBook);
        Path backupPath = filePath.resolveSibling(filePath.getFileName() + ".backup");
        assertFalse(Files.exists(filePath));
        assertEquals(originalContents, Files.readString(backupPath));

        storage.saveTuitionBook(new TuitionBook());
        assertEquals(new TuitionBook(), new TuitionBook(storage.readTuitionBook().orElseThrow()));
    }

    @Test
    public void readTuitionBook_existingBackup_usesNextBackup() throws Exception {
        Path filePath = testFolder.resolve("ConflictingTuitionBook.json");
        Path backupPath = filePath.resolveSibling(filePath.getFileName() + ".backup");
        Files.writeString(filePath, CONFLICTING_TUITION_BOOK_JSON);
        Files.writeString(backupPath, "existing backup");
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(filePath);

        assertThrows(DataLoadingException.class, storage::readTuitionBook);

        assertEquals("existing backup", Files.readString(backupPath));
        assertEquals(CONFLICTING_TUITION_BOOK_JSON,
                Files.readString(filePath.resolveSibling(filePath.getFileName() + ".backup.1")));
    }

    @Test
    public void readTuitionBook_externalBackupSymlink_preservesSourceLinkAndTargetAndBlocksSave() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("application-home"));
        Path dataDirectory = Files.createDirectory(applicationHome.resolve("data"));
        Path filePath = dataDirectory.resolve("tuitionbook.json");
        Files.writeString(filePath, CONFLICTING_TUITION_BOOK_JSON);
        Path outsideFile = testFolder.resolve("outside-backup.json");
        Files.writeString(outsideFile, "outside contents");
        Path backupLink = dataDirectory.resolve("tuitionbook.json.backup");
        SymlinkTestUtil.createSymbolicLink(backupLink, outsideFile);
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(filePath, applicationHome);

        DataLoadingException exception = org.junit.jupiter.api.Assertions.assertThrows(
                DataLoadingException.class, storage::readTuitionBook);

        assertTrue(exception.getMalformedFileBackupPath().isEmpty());
        assertEquals(CONFLICTING_TUITION_BOOK_JSON, Files.readString(filePath));
        assertTrue(Files.isSymbolicLink(backupLink));
        assertEquals("outside contents", Files.readString(outsideFile));
        assertThrows(IOException.class, () -> storage.saveTuitionBook(new TuitionBook()));
    }

    @Test
    public void readTuitionBook_backupMoveFails_leavesOriginalAndBlocksSave() throws Exception {
        Path filePath = testFolder.resolve("ConflictingTuitionBook.json");
        Files.writeString(filePath, CONFLICTING_TUITION_BOOK_JSON);
        byte[] originalContents = Files.readAllBytes(filePath);
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(filePath) {
            @Override
            protected void moveFile(Path source, Path target) throws IOException {
                throw new IOException("simulated backup failure");
            }
        };

        assertThrows(DataLoadingException.class, storage::readTuitionBook);

        assertArrayEquals(originalContents, Files.readAllBytes(filePath));
        String expectedMessage = String.format(JsonTuitionBookStorage.MESSAGE_WRITE_BLOCKED, filePath);
        assertThrows(IOException.class, expectedMessage, () -> storage.saveTuitionBook(new TuitionBook()));
    }

    @Test
    public void readTuitionBook_missingConfiguredFile_doesNotBlockSave() throws Exception {
        Path filePath = testFolder.resolve("MissingTuitionBook.json");
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(filePath);

        assertTrue(storage.readTuitionBook().isEmpty());
        storage.saveTuitionBook(new TuitionBook());

        assertTrue(Files.exists(filePath));
        assertEquals(new TuitionBook(), new TuitionBook(storage.readTuitionBook().orElseThrow()));
    }

    @Test
    public void saveTuitionBook_writeFailure_preservesExistingFile() throws Exception {
        Path filePath = testFolder.resolve("AtomicSaveTuitionBook.json");
        String originalContents = "{\"persons\":[]}";
        Files.writeString(filePath, originalContents);
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(filePath) {
            @Override
            protected void writeJsonFile(JsonSerializableTuitionBook tuitionBook, Path temporaryFile)
                    throws IOException {
                Files.writeString(temporaryFile, "partial JSON");
                throw new IOException("simulated write failure");
            }
        };

        assertThrows(IOException.class, () -> storage.saveTuitionBook(getTypicalTuitionBook()));

        assertEquals(originalContents, Files.readString(filePath));
    }

    @Test
    public void saveTuitionBook_replacementFailure_preservesExistingFileAndCleansUpTemporaryFile() throws Exception {
        Path filePath = testFolder.resolve("AtomicSaveTuitionBook.json");
        String originalContents = "{\"persons\":[]}";
        Files.writeString(filePath, originalContents);
        Path[] temporaryFile = new Path[1];
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(filePath) {
            @Override
            protected void replaceFile(Path source, Path target) throws IOException {
                temporaryFile[0] = source;
                throw new IOException("simulated replacement failure");
            }
        };

        assertThrows(IOException.class, () -> storage.saveTuitionBook(getTypicalTuitionBook()));

        assertEquals(originalContents, Files.readString(filePath));
        assertFalse(Files.exists(temporaryFile[0]));
    }

    @Test
    public void readTuitionBookWithResult_repairedLink_keepsContactsAndAllowsSave() throws Exception {
        Path filePath = testFolder.resolve("RepairedTuitionBook.json");
        Files.writeString(filePath, """
                {"persons":[
                  {"id":"123e4567-e89b-12d3-a456-426614174000","role":"STUDENT",
                   "guardianId":"123e4567-e89b-12d3-a456-426614174099","name":"Sam Student",
                   "phone":"91234567","tags":[]},
                  {"id":"123e4567-e89b-12d3-a456-426614174001","role":"GUARDIAN",
                   "name":"Grace Guardian","phone":"92345678","tags":[]}
                ]}
                """);
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(filePath);

        TuitionBookLoadResult result = storage.readTuitionBookWithResult().orElseThrow();

        assertEquals(1, result.repairedGuardianLinkCount());
        assertEquals(2, result.tuitionBook().getPersonList().size());
        assertTrue(result.tuitionBook().getPersonList().get(0).getGuardianId().isEmpty());
        storage.saveTuitionBook(result.tuitionBook());
        assertEquals(0, storage.readTuitionBookWithResult().orElseThrow().repairedGuardianLinkCount());
    }

    @Test
    public void readTuitionBook_invalidUnrelatedFile_doesNotBlockConfiguredFile() throws Exception {
        Path configuredFilePath = testFolder.resolve("ConfiguredTuitionBook.json");
        Path unrelatedFilePath = testFolder.resolve("UnrelatedTuitionBook.json");
        Files.writeString(unrelatedFilePath, "not valid JSON");
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(configuredFilePath);

        assertThrows(DataLoadingException.class, () -> storage.readTuitionBook(unrelatedFilePath));
        storage.saveTuitionBook(new TuitionBook());

        assertTrue(Files.exists(configuredFilePath));
        assertEquals(new TuitionBook(), new TuitionBook(storage.readTuitionBook().orElseThrow()));
    }

    @Test
    public void saveTuitionBook_nullTuitionBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveTuitionBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code tuitionBook} at the specified {@code filePath}.
     */
    private void saveTuitionBook(ReadOnlyTuitionBook tuitionBook, String filePath) {
        try {
            new JsonTuitionBookStorage(Paths.get(filePath))
                    .saveTuitionBook(tuitionBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveTuitionBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveTuitionBook(new TuitionBook(), null));
    }

    private void assertOverLengthConfiguredFileIsBackedUp(String fileName, String name, String address)
            throws Exception {
        Path filePath = testFolder.resolve(fileName);
        Path backupPath = filePath.resolveSibling(filePath.getFileName() + ".backup");
        byte[] originalContents = createOverLengthFileContents(name, address);
        Files.write(filePath, originalContents);
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(filePath);

        DataLoadingException exception = org.junit.jupiter.api.Assertions.assertThrows(
                DataLoadingException.class, storage::readTuitionBook);
        assertEquals(backupPath, exception.getMalformedFileBackupPath().orElseThrow());
        assertFalse(Files.exists(filePath));
        assertArrayEquals(originalContents, Files.readAllBytes(backupPath));

        storage.saveTuitionBook(new TuitionBook());
        assertTrue(Files.exists(filePath));
        assertArrayEquals(originalContents, Files.readAllBytes(backupPath));
    }

    private void assertOverLengthConfiguredFileIsPreservedAfterBackupFailure(
            String fileName, String name, String address) throws Exception {
        Path filePath = testFolder.resolve(fileName);
        byte[] originalContents = createOverLengthFileContents(name, address);
        Files.write(filePath, originalContents);
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(filePath) {
            @Override
            protected void moveFile(Path source, Path target) throws IOException {
                throw new IOException("simulated backup failure");
            }
        };

        DataLoadingException exception = org.junit.jupiter.api.Assertions.assertThrows(
                DataLoadingException.class, storage::readTuitionBook);
        assertTrue(exception.getMalformedFileBackupPath().isEmpty());
        assertArrayEquals(originalContents, Files.readAllBytes(filePath));
        String expectedMessage = String.format(JsonTuitionBookStorage.MESSAGE_WRITE_BLOCKED, filePath);
        assertThrows(IOException.class, expectedMessage, () -> storage.saveTuitionBook(new TuitionBook()));
        assertArrayEquals(originalContents, Files.readAllBytes(filePath));
    }

    private byte[] createOverLengthFileContents(String name, String address) {
        return """
                {
                  "persons" : [ {
                    "name" : "%s",
                    "phone" : "91234567",
                    "email" : null,
                    "address" : "%s",
                    "tags" : [ ]
                  } ]
                }
                """.formatted(name, address).getBytes(StandardCharsets.UTF_8);
    }

    private void assertMalformedDataIsBackedUpAndSavingIsAllowed(String contents) throws Exception {
        Path filePath = testFolder.resolve("MalformedTuitionBook.json");
        Path backupPath = filePath.resolveSibling(filePath.getFileName() + ".backup");
        Files.writeString(filePath, contents);
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(filePath);

        assertThrows(DataLoadingException.class, storage::readTuitionBook);
        assertFalse(Files.exists(filePath));
        assertEquals(contents, Files.readString(backupPath));
        storage.saveTuitionBook(new TuitionBook());
        assertTrue(Files.exists(filePath));
        assertEquals(contents, Files.readString(backupPath));
    }
}
