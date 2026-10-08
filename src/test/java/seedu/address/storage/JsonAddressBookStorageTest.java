package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.person.Role;
import seedu.address.testutil.PersonBuilder;

public class JsonAddressBookStorageTest {
    private static final String CONFLICTING_ADDRESS_BOOK_JSON = """
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
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void readAndSaveAddressBook_personWithoutEmailAndAddress_preservesAbsence() throws Exception {
        Path filePath = testFolder.resolve("OptionalFieldsAddressBook.json");
        Person person = new PersonBuilder().withoutEmail().withoutAddress().build();
        AddressBook original = new AddressBook();
        original.addPerson(person);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        storage.saveAddressBook(original);
        ReadOnlyAddressBook readBack = storage.readAddressBook().orElseThrow();

        assertEquals(original, new AddressBook(readBack));
        Person restoredPerson = readBack.getPersonList().get(0);
        assertTrue(restoredPerson.getEmail().isEmpty());
        assertTrue(restoredPerson.getAddress().isEmpty());
    }

    @Test
    public void readAndSaveAddressBook_linkedStudentAndGuardian_preservesIdsRolesAndLink() throws Exception {
        Path filePath = testFolder.resolve("LinkedContactsAddressBook.json");
        Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("90000001")
                .withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Sam Student").withPhone("90000002")
                .withRole(Role.STUDENT).withGuardian(guardian).withoutEmail().withoutAddress().build();
        AddressBook original = new AddressBook();
        original.addPerson(student);
        original.addPerson(guardian);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        storage.saveAddressBook(original);
        ReadOnlyAddressBook readBack = storage.readAddressBook().orElseThrow();
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
    public void readAddressBook_conflictingConfiguredFile_blocksSaveUntilSuccessfulRead() throws Exception {
        Path filePath = testFolder.resolve("ConflictingAddressBook.json");
        Files.writeString(filePath, CONFLICTING_ADDRESS_BOOK_JSON);
        String originalContents = Files.readString(filePath);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        assertThrows(DataLoadingException.class, storage::readAddressBook);
        String expectedMessage = String.format(JsonAddressBookStorage.MESSAGE_WRITE_BLOCKED, filePath);
        assertThrows(IOException.class, expectedMessage, () -> storage.saveAddressBook(new AddressBook()));
        assertTrue(Files.exists(filePath));
        assertEquals(originalContents, Files.readString(filePath));

        String correctedContents = originalContents.replace("9123-4567", "9123-4568");
        Files.writeString(filePath, correctedContents);
        assertEquals(3, storage.readAddressBook().orElseThrow().getPersonList().size());

        storage.saveAddressBook(new AddressBook());
        assertEquals(new AddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void readAddressBook_missingConfiguredFile_doesNotBlockSave() throws Exception {
        Path filePath = testFolder.resolve("MissingAddressBook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        assertTrue(storage.readAddressBook().isEmpty());
        storage.saveAddressBook(new AddressBook());

        assertTrue(Files.exists(filePath));
        assertEquals(new AddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void readAddressBook_invalidUnrelatedFile_doesNotBlockConfiguredFile() throws Exception {
        Path configuredFilePath = testFolder.resolve("ConfiguredAddressBook.json");
        Path unrelatedFilePath = testFolder.resolve("UnrelatedAddressBook.json");
        Files.writeString(unrelatedFilePath, "not valid JSON");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(configuredFilePath);

        assertThrows(DataLoadingException.class, () -> storage.readAddressBook(unrelatedFilePath));
        storage.saveAddressBook(new AddressBook());

        assertTrue(Files.exists(configuredFilePath));
        assertEquals(new AddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
