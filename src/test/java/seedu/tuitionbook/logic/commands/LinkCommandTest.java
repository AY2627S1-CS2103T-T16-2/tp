package seedu.tuitionbook.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.assertCommandFailure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.commons.core.index.Index;
import seedu.tuitionbook.logic.Messages;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.UserPrefs;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.PersonHasRolePredicate;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.testutil.PersonBuilder;

public class LinkCommandTest {
    private static final UUID STUDENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OLD_GUARDIAN_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID NEW_GUARDIAN_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID HIDDEN_STUDENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000004");

    @Test
    public void execute_filteredRelink_success() throws Exception {
        Person oldGuardian = new PersonBuilder().withName("Old Guardian").withPhone("90000002")
                .withId(OLD_GUARDIAN_ID).withRole(Role.GUARDIAN).build();
        Person newGuardian = new PersonBuilder().withName("New Guardian").withPhone("90000003")
                .withId(NEW_GUARDIAN_ID).withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Student One").withPhone("90000001")
                .withId(STUDENT_ID).withEmail("student@example.com").withAddress("1 Main Street")
                .withTags("primary", "math").withGuardian(oldGuardian).build();
        ModelManager model = modelWith(student, oldGuardian, newGuardian);
        model.updateFilteredPersonList(person -> !person.getId().equals(oldGuardian.getId()));
        assertEquals(List.of(student, newGuardian), model.getFilteredPersonList());

        CommandResult result = new LinkCommand(Index.fromOneBased(1), Index.fromOneBased(2)).execute(model);

        String expectedMessage = String.format(LinkCommand.MESSAGE_RELINK_SUCCESS, student.getName(),
                newGuardian.getName(), oldGuardian.getName());
        assertEquals(expectedMessage, result.getFeedbackToUser());
        Person expectedStudent = student.withGuardianId(newGuardian.getId());
        Person updatedStudent = model.getTuitionBook().getPersonList().get(0);
        assertEquals(student.getId(), updatedStudent.getId());
        assertEquals(expectedStudent, updatedStudent);
        assertEquals(List.of(expectedStudent, oldGuardian, newGuardian), model.getTuitionBook().getPersonList());
        assertEquals(List.of(STUDENT_ID, OLD_GUARDIAN_ID, NEW_GUARDIAN_ID), contactIds(model));
        assertEquals(List.of(expectedStudent, oldGuardian, newGuardian), model.getFilteredPersonList());
    }

    @Test
    public void execute_firstTimeLink_success() throws Exception {
        Person student = student();
        Person guardian = guardian();
        ModelManager model = modelWith(student, guardian);

        CommandResult result = new LinkCommand(Index.fromOneBased(1), Index.fromOneBased(2)).execute(model);

        assertEquals(String.format(LinkCommand.MESSAGE_LINK_SUCCESS, student.getName(), guardian.getName()),
                result.getFeedbackToUser());
        assertEquals(guardian.getId(), model.getTuitionBook().getPersonList().get(0)
                .getGuardianId().orElseThrow());
    }

    @Test
    public void execute_invalidStudentIndex_failure() {
        ModelManager model = modelWithStudentAndGuardian();

        assertCommandFailure(new LinkCommand(Index.fromOneBased(3), Index.fromOneBased(2)), model,
                invalidStudentIndexMessage(2));
    }

    @Test
    public void execute_invalidGuardianIndex_failure() {
        ModelManager model = modelWithStudentAndGuardian();

        assertCommandFailure(new LinkCommand(Index.fromOneBased(1), Index.fromOneBased(3)), model,
                invalidGuardianIndexMessage(2));
    }

    @Test
    public void execute_bothIndexesInvalid_reportsStudentIndexFailure() {
        ModelManager model = modelWithStudentAndGuardian();

        assertCommandFailure(new LinkCommand(Index.fromOneBased(3), Index.fromOneBased(4)), model,
                invalidStudentIndexMessage(2));
    }

    @Test
    public void execute_emptyContactBook_reportsStudentIndexFailure() {
        ModelManager model = new ModelManager();

        assertCommandFailure(new LinkCommand(Index.fromOneBased(1), Index.fromOneBased(1)), model,
                invalidStudentIndexMessage(0));
    }

    @Test
    public void execute_noMatchesFilter_reportsStudentIndexFailureAndPreservesFilter() {
        ModelManager model = modelWithStudentAndGuardian();
        model.updateFilteredPersonList(person -> false);

        assertCommandFailure(new LinkCommand(Index.fromOneBased(1), Index.fromOneBased(1)), model,
                invalidStudentIndexMessage(0));
    }

    @Test
    public void execute_roleFilteredList_reportsGuardianIndexUsingDisplayedCount() {
        ModelManager model = modelWithStudentAndGuardian();
        model.updateFilteredPersonList(new PersonHasRolePredicate(Role.GUARDIAN));

        assertCommandFailure(new LinkCommand(Index.fromOneBased(1), Index.fromOneBased(2)), model,
                invalidGuardianIndexMessage(1));
    }

    @Test
    public void execute_invalidGuardianIndex_preLinkedStudentPreservesGuardianRelationship() {
        Person guardian = new PersonBuilder().withName("Guardian").withPhone("90000002")
                .withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Student").withPhone("90000001")
                .withGuardian(guardian).build();
        ModelManager model = new ModelManager(new seedu.tuitionbook.model.TuitionBook(), new UserPrefs());
        model.addPerson(student);
        model.addPerson(guardian);

        assertCommandFailure(new LinkCommand(Index.fromOneBased(1), Index.fromOneBased(3)), model,
                invalidGuardianIndexMessage(2));
        assertEquals(guardian.getId(), model.getFilteredPersonList().get(0).getGuardianId().orElseThrow());
    }

    @Test
    public void execute_invalidRoles_failure() {
        Person student = student();
        Person guardian = guardian();
        Person hiddenStudent = hiddenStudent();
        ModelManager model = filteredModel(student, guardian, hiddenStudent);

        assertCommandFailureAndStateUnchanged(
                new LinkCommand(Index.fromOneBased(2), Index.fromOneBased(2)), model,
                LinkCommand.MESSAGE_STUDENT_REQUIRED);
        assertCommandFailureAndStateUnchanged(
                new LinkCommand(Index.fromOneBased(1), Index.fromOneBased(1)), model,
                LinkCommand.MESSAGE_GUARDIAN_REQUIRED);
    }

    @Test
    public void execute_alreadyLinkedPair_successWithoutChanges() throws Exception {
        Person guardian = guardian();
        Person linkedStudent = new PersonBuilder(student()).withGuardian(guardian).build();
        Person hiddenStudent = hiddenStudent();
        ModelManager model = filteredModel(linkedStudent, guardian, hiddenStudent);
        List<Person> expectedContacts = new ArrayList<>(model.getTuitionBook().getPersonList());
        List<UUID> expectedContactIds = contactIds(model);
        List<Person> expectedFilteredContacts = new ArrayList<>(model.getFilteredPersonList());
        List<UUID> expectedFilteredContactIds = filteredContactIds(model);

        CommandResult result = new LinkCommand(Index.fromOneBased(1), Index.fromOneBased(2)).execute(model);

        assertEquals(String.format(LinkCommand.MESSAGE_ALREADY_LINKED, linkedStudent.getName(), guardian.getName()),
                result.getFeedbackToUser());
        assertModelStateUnchanged(model, expectedContacts, expectedContactIds,
                expectedFilteredContacts, expectedFilteredContactIds);
    }

    @Test
    public void equals_and_toString() {
        LinkCommand command = new LinkCommand(Index.fromOneBased(1), Index.fromOneBased(2));
        assertTrue(command.equals(command));
        assertTrue(command.equals(new LinkCommand(Index.fromOneBased(1), Index.fromOneBased(2))));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertFalse(command.equals(new LinkCommand(Index.fromOneBased(2), Index.fromOneBased(1))));
        assertTrue(command.toString().contains("studentIndex"));
    }

    private static Person student() {
        return new PersonBuilder().withName("Student One").withPhone("90000001").withId(STUDENT_ID).build();
    }

    private static Person guardian() {
        return new PersonBuilder().withName("New Guardian").withPhone("90000003")
                .withId(NEW_GUARDIAN_ID).withRole(Role.GUARDIAN).build();
    }

    private static Person hiddenStudent() {
        return new PersonBuilder().withName("Hidden Student").withPhone("90000004")
                .withId(HIDDEN_STUDENT_ID).build();
    }

    private static ModelManager modelWith(Person... persons) {
        ModelManager model = new ModelManager(new TuitionBook(), new UserPrefs());
        Arrays.stream(persons).forEach(model::addPerson);
        return model;
    }

    private static ModelManager modelWithStudentAndGuardian() {
        return modelWith(student(), guardian());
    }

    private static ModelManager filteredModel(Person firstPerson, Person secondPerson, Person hiddenPerson) {
        ModelManager model = modelWith(firstPerson, secondPerson, hiddenPerson);
        model.updateFilteredPersonList(person -> !person.getId().equals(hiddenPerson.getId()));
        assertEquals(List.of(firstPerson, secondPerson), model.getFilteredPersonList());
        return model;
    }

    private static void assertCommandFailureAndStateUnchanged(LinkCommand command, ModelManager model,
            String expectedMessage) {
        List<Person> expectedContacts = new ArrayList<>(model.getTuitionBook().getPersonList());
        List<UUID> expectedContactIds = contactIds(model);
        List<Person> expectedFilteredContacts = new ArrayList<>(model.getFilteredPersonList());
        List<UUID> expectedFilteredContactIds = filteredContactIds(model);

        assertCommandFailure(command, model, expectedMessage);
        assertModelStateUnchanged(model, expectedContacts, expectedContactIds,
                expectedFilteredContacts, expectedFilteredContactIds);
    }

    private static void assertModelStateUnchanged(ModelManager model, List<Person> expectedContacts,
            List<UUID> expectedContactIds, List<Person> expectedFilteredContacts,
            List<UUID> expectedFilteredContactIds) {
        assertEquals(expectedContacts, model.getTuitionBook().getPersonList());
        assertEquals(expectedContactIds, contactIds(model));
        assertEquals(expectedFilteredContacts, model.getFilteredPersonList());
        assertEquals(expectedFilteredContactIds, filteredContactIds(model));
    }

    private static List<UUID> contactIds(ModelManager model) {
        return model.getTuitionBook().getPersonList().stream().map(Person::getId).toList();
    }

    private static List<UUID> filteredContactIds(ModelManager model) {
        return model.getFilteredPersonList().stream().map(Person::getId).toList();
    }

    private static String invalidStudentIndexMessage(int displayedPersonCount) {
        return "Student index: " + Messages.getInvalidPersonDisplayedIndexMessage(displayedPersonCount);
    }

    private static String invalidGuardianIndexMessage(int displayedPersonCount) {
        return "Guardian index: " + Messages.getInvalidPersonDisplayedIndexMessage(displayedPersonCount);
    }
}
