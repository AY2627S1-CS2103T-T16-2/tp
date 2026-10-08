package seedu.tuitionbook.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.commons.exceptions.IllegalValueException;
import seedu.tuitionbook.commons.util.JsonUtil;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.testutil.TypicalPersons;

public class JsonSerializableTuitionBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableTuitionBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsTuitionBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonTuitionBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonTuitionBook.json");

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableTuitionBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableTuitionBook.class).get();
        TuitionBook tuitionBookFromFile = dataFromFile.toModelType();
        TuitionBook typicalPersonsTuitionBook = TypicalPersons.getTypicalTuitionBook();
        assertEquals(tuitionBookFromFile, typicalPersonsTuitionBook);
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableTuitionBook dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableTuitionBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableTuitionBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableTuitionBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableTuitionBook.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicateIds_throwsIllegalValueException() {
        String sharedId = "123e4567-e89b-12d3-a456-426614174000";
        JsonAdaptedPerson first = new JsonAdaptedPerson(sharedId, "student", null, "Alice", "91234567", null,
                null, List.of());
        JsonAdaptedPerson second = new JsonAdaptedPerson(sharedId, "guardian", null, "Bob", "92345678", null,
                null, List.of());
        JsonSerializableTuitionBook tuitionBook = new JsonSerializableTuitionBook(List.of(first, second));

        assertThrows(IllegalValueException.class, JsonSerializableTuitionBook.MESSAGE_DUPLICATE_ID,
                tuitionBook::toModelType);
    }

    @Test
    public void toModelType_invalidGuardianTarget_dropsLinkAndRetainsStudent() throws Exception {
        UUID studentId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID missingGuardianId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        JsonAdaptedPerson student = new JsonAdaptedPerson(studentId.toString(), "student", missingGuardianId.toString(),
                "Alice", "91234567", null, null, List.of());
        JsonSerializableTuitionBook tuitionBook = new JsonSerializableTuitionBook(List.of(student));

        Person restoredStudent = tuitionBook.toModelType().getPersonList().get(0);
        assertEquals(Role.STUDENT, restoredStudent.getRole());
        assertTrue(restoredStudent.getGuardianId().isEmpty());
    }

    @Test
    public void toModelType_studentGuardianIdTargetingStudent_dropsLinkAndRetainsBothContacts() throws Exception {
        UUID firstStudentId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UUID secondStudentId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
        JsonAdaptedPerson firstStudent = new JsonAdaptedPerson(firstStudentId.toString(), "student",
                secondStudentId.toString(), "Alice", "91234567", null, null, List.of());
        JsonAdaptedPerson secondStudent = new JsonAdaptedPerson(secondStudentId.toString(), "student", null,
                "Bob", "92345678", null, null, List.of());
        JsonSerializableTuitionBook tuitionBook = new JsonSerializableTuitionBook(List.of(firstStudent, secondStudent));

        TuitionBook restoredTuitionBook = tuitionBook.toModelType();
        assertEquals(2, restoredTuitionBook.getPersonList().size());
        assertTrue(restoredTuitionBook.getPersonList().get(0).getGuardianId().isEmpty());
    }

}
