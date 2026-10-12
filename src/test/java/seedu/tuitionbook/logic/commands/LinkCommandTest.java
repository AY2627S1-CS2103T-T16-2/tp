package seedu.tuitionbook.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.assertCommandFailure;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.commons.core.index.Index;
import seedu.tuitionbook.logic.Messages;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.UserPrefs;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.PersonHasRolePredicate;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.testutil.PersonBuilder;

public class LinkCommandTest {
    private ModelManager modelWithStudentAndGuardian() {
        ModelManager model = new ModelManager(new seedu.tuitionbook.model.TuitionBook(), new UserPrefs());
        model.addPerson(new PersonBuilder().withName("Student").withPhone("90000001").build());
        model.addPerson(new PersonBuilder().withName("Guardian").withPhone("90000002")
                .withRole(Role.GUARDIAN).build());
        return model;
    }

    @Test
    public void execute_linkReplaceAndRelink_success() throws Exception {
        Person student = new PersonBuilder().withName("Student").withPhone("90000001").build();
        Person oldGuardian = new PersonBuilder().withName("Old Guardian").withPhone("90000002")
                .withRole(Role.GUARDIAN).build();
        Person newGuardian = new PersonBuilder().withName("New Guardian").withPhone("90000003")
                .withRole(Role.GUARDIAN).build();
        ModelManager model = new ModelManager(new seedu.tuitionbook.model.TuitionBook(), new UserPrefs());
        model.addPerson(student);
        model.addPerson(oldGuardian);
        model.addPerson(newGuardian);

        new LinkCommand(Index.fromOneBased(1), Index.fromOneBased(2)).execute(model);
        assertEquals(oldGuardian.getId(), model.getFilteredPersonList().get(0).getGuardianId().orElseThrow());
        CommandResult result = new LinkCommand(Index.fromOneBased(1), Index.fromOneBased(3)).execute(model);
        assertTrue(result.getFeedbackToUser().contains("Previous guardian unlinked"));
        assertEquals(newGuardian.getId(), model.getFilteredPersonList().get(0).getGuardianId().orElseThrow());

        result = new LinkCommand(Index.fromOneBased(1), Index.fromOneBased(3)).execute(model);
        assertTrue(result.getFeedbackToUser().contains("already linked"));
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
        ModelManager model = modelWithStudentAndGuardian();
        assertCommandFailure(new LinkCommand(Index.fromOneBased(2), Index.fromOneBased(2)), model,
                LinkCommand.MESSAGE_STUDENT_REQUIRED);
        assertCommandFailure(new LinkCommand(Index.fromOneBased(1), Index.fromOneBased(1)), model,
                LinkCommand.MESSAGE_GUARDIAN_REQUIRED);
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

    private static String invalidStudentIndexMessage(int displayedPersonCount) {
        return "Student index: " + Messages.getInvalidPersonDisplayedIndexMessage(displayedPersonCount);
    }

    private static String invalidGuardianIndexMessage(int displayedPersonCount) {
        return "Guardian index: " + Messages.getInvalidPersonDisplayedIndexMessage(displayedPersonCount);
    }
}
