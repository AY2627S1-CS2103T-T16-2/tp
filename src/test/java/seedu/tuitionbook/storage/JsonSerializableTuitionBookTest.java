package seedu.tuitionbook.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.commons.exceptions.IllegalValueException;
import seedu.tuitionbook.commons.util.JsonUtil;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.person.Name;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.testutil.TypicalPersons;

public class JsonSerializableTuitionBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableTuitionBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsTuitionBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonTuitionBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonTuitionBook.json");
    private static final Path CASE_EQUIVALENT_DUPLICATE_PERSON_FILE =
            TEST_DATA_FOLDER.resolve("caseEquivalentDuplicatePersonTuitionBook.json");
    private static final Path SAME_IDENTITY_DIFFERENT_ROLES_FILE =
            TEST_DATA_FOLDER.resolve("sameIdentityDifferentRolesTuitionBook.json");

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
        assertThrows(IllegalValueException.class,
                String.format(JsonSerializableTuitionBook.MESSAGE_DUPLICATE_PERSON, 2, 1), dataFromFile::toModelType);
    }

    @Test
    public void toModelType_caseEquivalentUnicodeDuplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableTuitionBook dataFromFile = JsonUtil.readJsonFile(CASE_EQUIVALENT_DUPLICATE_PERSON_FILE,
                JsonSerializableTuitionBook.class).get();

        assertThrows(IllegalValueException.class,
                String.format(JsonSerializableTuitionBook.MESSAGE_DUPLICATE_PERSON, 2, 1), dataFromFile::toModelType);
    }

    @Test
    public void toModelType_laterDuplicatePerson_namesEarlierContactWithSameRole() {
        // Contact 1 shares the name and phone but not the role, so it is not the reported duplicate.
        JsonSerializableTuitionBook tuitionBook = new JsonSerializableTuitionBook(List.of(
                person("123e4567-e89b-12d3-a456-426614174000", "guardian", null, "Bob", "92345678"),
                person("123e4567-e89b-12d3-a456-426614174001", "student", null, "Alice", "91234567"),
                person("123e4567-e89b-12d3-a456-426614174002", "student", null, "Bob", "92345678"),
                person("123e4567-e89b-12d3-a456-426614174003", "student", null, "BOB", "9234 5678")));

        assertThrows(IllegalValueException.class, "Contact 4 in the file has the same role, name and phone number "
                + "as contact 3.", tuitionBook::toModelType);
    }

    @Test
    public void toModelType_nullContact_namesPosition() {
        List<JsonAdaptedPerson> contacts = new ArrayList<>();
        contacts.add(person("123e4567-e89b-12d3-a456-426614174000", "student", null, "Alice", "91234567"));
        contacts.add(null);
        JsonSerializableTuitionBook tuitionBook = new JsonSerializableTuitionBook(contacts);

        assertThrows(IllegalValueException.class, "Contact 2 in the file is null instead of a contact. "
                + "Remove it from the \"persons\" list.", tuitionBook::toModelType);
    }

    @Test
    public void toModelType_invalidContactValue_prefixesPosition() {
        JsonSerializableTuitionBook tuitionBook = new JsonSerializableTuitionBook(List.of(
                person("123e4567-e89b-12d3-a456-426614174000", "student", null, "Alice", "91234567"),
                person("123e4567-e89b-12d3-a456-426614174001", "student", null, "-Bob", "92345678")));

        assertThrows(IllegalValueException.class, "Contact 2 in the file: " + Name.MESSAGE_INVALID_START,
                tuitionBook::toModelType);
    }

    @Test
    public void toModelType_missingContactValue_prefixesPosition() {
        JsonSerializableTuitionBook tuitionBook = new JsonSerializableTuitionBook(List.of(
                person("123e4567-e89b-12d3-a456-426614174000", "student", null, null, "91234567")));

        assertThrows(IllegalValueException.class, "Contact 1 in the file: Missing \"name\" value.",
                tuitionBook::toModelType);
    }

    @Test
    public void toModelType_sameNormalizedIdentityInDifferentRoles_success() throws Exception {
        JsonSerializableTuitionBook dataFromFile = JsonUtil.readJsonFile(SAME_IDENTITY_DIFFERENT_ROLES_FILE,
                JsonSerializableTuitionBook.class).get();

        TuitionBook tuitionBook = dataFromFile.toModelType();

        assertEquals(List.of(Role.STUDENT, Role.GUARDIAN), tuitionBook.getPersonList().stream()
                .map(Person::getRole).toList());
        assertEquals(2, tuitionBook.getPersonList().size());
    }

    @Test
    public void toModelType_duplicateIds_throwsIllegalValueException() {
        String sharedId = "123e4567-e89b-12d3-a456-426614174000";
        JsonAdaptedPerson first = new JsonAdaptedPerson(sharedId, "student", null, "Alice", "91234567", null,
                null, List.of());
        JsonAdaptedPerson second = new JsonAdaptedPerson(sharedId, "guardian", null, "Bob", "92345678", null,
                null, List.of());
        JsonSerializableTuitionBook tuitionBook = new JsonSerializableTuitionBook(List.of(first, second));

        assertThrows(IllegalValueException.class, "Contact 2 in the file has the same \"id\" as contact 1. "
                + "Each contact needs a different id.", tuitionBook::toModelType);
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

    @Test
    public void toModelTypeWithResult_cleanContacts_reportsNoRepairs() throws Exception {
        String guardianId = "123e4567-e89b-12d3-a456-426614174001";
        JsonAdaptedPerson student = person("123e4567-e89b-12d3-a456-426614174000", "student", guardianId,
                "Alice", "91234567");
        JsonAdaptedPerson guardian = person(guardianId, "guardian", null, "Bob", "92345678");

        TuitionBookLoadResult result = new JsonSerializableTuitionBook(List.of(student, guardian))
                .toModelTypeWithResult();

        assertEquals(0, result.repairedGuardianLinkCount());
        assertEquals(2, result.tuitionBook().getPersonList().size());
        assertEquals(UUID.fromString(guardianId), result.tuitionBook().getPersonList().get(0)
                .getGuardianId().orElseThrow());
    }

    @Test
    public void toModelTypeWithResult_invalidLinks_reportsEachRepairAndRetainsContacts() throws Exception {
        String missingId = "123e4567-e89b-12d3-a456-426614174099";
        String studentId = "123e4567-e89b-12d3-a456-426614174000";
        List<JsonAdaptedPerson> contacts = List.of(
                person(studentId, "student", studentId, "Self Link", "91234560"),
                person("123e4567-e89b-12d3-a456-426614174001", "student", "not-a-uuid",
                        "Malformed Link", "91234561"),
                person("123e4567-e89b-12d3-a456-426614174002", "guardian", missingId,
                        "Guardian Link", "91234562"),
                person("123e4567-e89b-12d3-a456-426614174003", "student", missingId,
                        "Missing Link", "91234563"),
                person("123e4567-e89b-12d3-a456-426614174004", "student", studentId,
                        "Student Target", "91234564"));

        TuitionBookLoadResult result = new JsonSerializableTuitionBook(contacts).toModelTypeWithResult();

        assertEquals(5, result.repairedGuardianLinkCount());
        assertEquals(5, result.tuitionBook().getPersonList().size());
        assertEquals(List.of(
                GuardianLinkRepair.Reason.SELF_LINK,
                GuardianLinkRepair.Reason.MALFORMED_GUARDIAN_ID,
                GuardianLinkRepair.Reason.NON_STUDENT_GUARDIAN_LINK,
                GuardianLinkRepair.Reason.MISSING_GUARDIAN,
                GuardianLinkRepair.Reason.STUDENT_GUARDIAN),
                result.guardianLinkRepairs().stream().map(GuardianLinkRepair::reason).toList());
        for (Person person : result.tuitionBook().getPersonList()) {
            assertTrue(person.getGuardianId().isEmpty());
        }
    }

    @Test
    public void toModelTypeWithResult_repairsFollowJsonOrder() throws Exception {
        String missingId = "123e4567-e89b-12d3-a456-426614174099";
        List<JsonAdaptedPerson> contacts = List.of(
                person("123e4567-e89b-12d3-a456-426614174000", "student", missingId,
                        "Missing First", "91234560"),
                person("123e4567-e89b-12d3-a456-426614174001", "student", "not-a-uuid",
                        "Malformed Second", "91234561"));

        TuitionBookLoadResult result = new JsonSerializableTuitionBook(contacts).toModelTypeWithResult();

        assertEquals(List.of("Missing First", "Malformed Second"),
                result.guardianLinkRepairs().stream().map(GuardianLinkRepair::personName).toList());
    }

    private static JsonAdaptedPerson person(String id, String role, String guardianId, String name, String phone) {
        return new JsonAdaptedPerson(id, role, guardianId, name, phone, null, null, List.of());
    }

}
