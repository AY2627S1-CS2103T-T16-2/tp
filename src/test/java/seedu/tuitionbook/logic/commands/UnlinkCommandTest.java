package seedu.tuitionbook.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.assertCommandFailure;

import java.util.ArrayList;
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

public class UnlinkCommandTest {
    private static final UUID STUDENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID GUARDIAN_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    public void execute_filteredLinkedStudent_successClearsFilter() throws Exception {
        Person guardian = guardian();
        Person student = linkedStudent(guardian);
        ModelManager model = modelWith(guardian, student);
        model.updateFilteredPersonList(new PersonHasRolePredicate(Role.STUDENT));

        CommandResult result = new UnlinkCommand(Index.fromOneBased(1)).execute(model);

        Person unlinkedStudent = student.clearGuardian();
        assertEquals(String.format(UnlinkCommand.MESSAGE_UNLINK_SUCCESS, student.getName(), guardian.getName()),
                result.getFeedbackToUser());
        assertEquals(List.of(guardian, unlinkedStudent), model.getTuitionBook().getPersonList());
        assertEquals(List.of(guardian, unlinkedStudent), model.getFilteredPersonList());
    }

    @Test
    public void execute_unlinkedStudent_successWithoutChanges() throws Exception {
        Person student = unlinkedStudent();
        Person guardian = guardian();
        ModelManager model = modelWith(student, guardian);
        model.updateFilteredPersonList(new PersonHasRolePredicate(Role.STUDENT));
        List<Person> expectedContacts = new ArrayList<>(model.getTuitionBook().getPersonList());
        List<Person> expectedFilteredContacts = new ArrayList<>(model.getFilteredPersonList());

        CommandResult result = new UnlinkCommand(Index.fromOneBased(1)).execute(model);

        assertEquals(String.format(UnlinkCommand.MESSAGE_NO_LINKED_GUARDIAN, student.getName()),
                result.getFeedbackToUser());
        assertEquals(expectedContacts, model.getTuitionBook().getPersonList());
        assertEquals(expectedFilteredContacts, model.getFilteredPersonList());
    }

    @Test
    public void execute_invalidIndex_failure() {
        ModelManager model = modelWith(unlinkedStudent(), guardian());

        assertCommandFailure(new UnlinkCommand(Index.fromOneBased(3)), model,
                invalidStudentIndexMessage(2));
    }

    @Test
    public void execute_guardianIndex_failure() {
        ModelManager model = modelWith(unlinkedStudent(), guardian());

        assertCommandFailure(new UnlinkCommand(Index.fromOneBased(2)), model,
                UnlinkCommand.MESSAGE_STUDENT_REQUIRED);
    }

    @Test
    public void equals_and_toString() {
        UnlinkCommand command = new UnlinkCommand(Index.fromOneBased(1));

        assertTrue(command.equals(command));
        assertTrue(command.equals(new UnlinkCommand(Index.fromOneBased(1))));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertFalse(command.equals(new UnlinkCommand(Index.fromOneBased(2))));
        assertTrue(command.toString().contains("studentIndex"));
    }

    private static Person linkedStudent(Person guardian) {
        return new PersonBuilder().withName("Student One").withPhone("90000001").withId(STUDENT_ID)
                .withGuardian(guardian).build();
    }

    private static Person unlinkedStudent() {
        return new PersonBuilder().withName("Student One").withPhone("90000001").withId(STUDENT_ID).build();
    }

    private static Person guardian() {
        return new PersonBuilder().withName("Guardian One").withPhone("90000002").withId(GUARDIAN_ID)
                .withRole(Role.GUARDIAN).build();
    }

    private static ModelManager modelWith(Person... persons) {
        ModelManager model = new ModelManager(new TuitionBook(), new UserPrefs());
        for (Person person : persons) {
            model.addPerson(person);
        }
        return model;
    }

    private static String invalidStudentIndexMessage(int displayedCount) {
        return String.format(UnlinkCommand.MESSAGE_INVALID_STUDENT_INDEX,
                Messages.getInvalidPersonDisplayedIndexMessage(displayedCount));
    }
}
