package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.person.Role;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalPersonsAddressBook = TypicalPersons.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalPersonsAddressBook);
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicateIds_throwsIllegalValueException() {
        String sharedId = "123e4567-e89b-12d3-a456-426614174000";
        JsonAdaptedPerson first = new JsonAdaptedPerson(sharedId, "student", null, "Alice", "91234567", null,
                null, List.of());
        JsonAdaptedPerson second = new JsonAdaptedPerson(sharedId, "guardian", null, "Bob", "92345678", null,
                null, List.of());
        JsonSerializableAddressBook addressBook = new JsonSerializableAddressBook(List.of(first, second));

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_ID,
                addressBook::toModelType);
    }

    @Test
    public void toModelType_invalidGuardianTarget_dropsLinkAndRetainsStudent() throws Exception {
        UUID studentId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID missingGuardianId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        JsonAdaptedPerson student = new JsonAdaptedPerson(studentId.toString(), "student", missingGuardianId.toString(),
                "Alice", "91234567", null, null, List.of());
        JsonSerializableAddressBook addressBook = new JsonSerializableAddressBook(List.of(student));

        Person restoredStudent = addressBook.toModelType().getPersonList().get(0);
        assertEquals(Role.STUDENT, restoredStudent.getRole());
        assertTrue(restoredStudent.getGuardianId().isEmpty());
    }

}
