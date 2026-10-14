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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.tuitionbook.commons.exceptions.DataLoadingException;
import seedu.tuitionbook.model.ReadOnlyTuitionBook;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.testutil.PersonBuilder;

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
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readTuitionBook("notJsonFormatTuitionBook.json"));
    }

    @Test
    public void readTuitionBook_nullRoot_throwsDataLoadingException() throws Exception {
        assertMalformedDataIsPreservedAndSavingIsBlocked("null");
    }

    @Test
    public void readTuitionBook_nullPerson_throwsDataLoadingException() throws Exception {
        assertMalformedDataIsPreservedAndSavingIsBlocked("{\"persons\":[null]}");
    }

    @Test
    public void readTuitionBook_nullTag_throwsDataLoadingException() throws Exception {
        assertMalformedDataIsPreservedAndSavingIsBlocked("""
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

    private void assertMalformedDataIsPreservedAndSavingIsBlocked(String contents) throws Exception {
        Path filePath = testFolder.resolve("MalformedTuitionBook.json");
        Files.writeString(filePath, contents);
        JsonTuitionBookStorage storage = new JsonTuitionBookStorage(filePath);

        assertThrows(DataLoadingException.class, storage::readTuitionBook);
        assertEquals(contents, Files.readString(filePath));
        assertThrows(IOException.class, () -> storage.saveTuitionBook(new TuitionBook()));
        assertEquals(contents, Files.readString(filePath));
    }
}
