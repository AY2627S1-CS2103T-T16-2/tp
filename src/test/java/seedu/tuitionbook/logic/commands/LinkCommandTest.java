package seedu.tuitionbook.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.commons.core.index.Index;
import seedu.tuitionbook.logic.commands.exceptions.CommandException;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.UserPrefs;
import seedu.tuitionbook.model.person.Person;
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
    public void execute_invalidIndex_failure() {
        ModelManager model = modelWithStudentAndGuardian();
        assertThrows(CommandException.class, () ->
                new LinkCommand(Index.fromOneBased(3), Index.fromOneBased(2)).execute(model));
        assertThrows(CommandException.class, () ->
                new LinkCommand(Index.fromOneBased(1), Index.fromOneBased(3)).execute(model));
    }

    @Test
    public void execute_invalidRoles_failure() {
        ModelManager model = modelWithStudentAndGuardian();
        assertThrows(CommandException.class, () ->
                new LinkCommand(Index.fromOneBased(2), Index.fromOneBased(2)).execute(model));
        assertThrows(CommandException.class, () ->
                new LinkCommand(Index.fromOneBased(1), Index.fromOneBased(1)).execute(model));
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
}
