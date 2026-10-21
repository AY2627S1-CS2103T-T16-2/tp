package seedu.tuitionbook.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.testutil.Assert.assertThrows;
import static seedu.tuitionbook.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.tuitionbook.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.logic.commands.exceptions.CommandException;
import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.person.Name;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.testutil.PersonBuilder;

/** Verifies save decisions describe the latest execution, including when command objects are reused. */
public class CommandSaveDecisionTest {
    @Test
    public void beforeExecution_mutatingCommands_allowSnapshotsWithoutRequestingSave() {
        for (Command command : List.of(new AddCommand(new PersonBuilder().build()),
                new DeleteCommand(INDEX_FIRST_PERSON), new EditCommand(INDEX_FIRST_PERSON,
                        new EditCommand.EditPersonDescriptor()), new ClearCommand(),
                new LinkCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON), new UnlinkCommand(INDEX_FIRST_PERSON))) {
            assertTrue(command.canMutate());
            assertFalse(command.requiresSave());
        }
    }

    @Test
    public void execute_addThenDuplicate_resetsSaveDecisionOnFailure() throws Exception {
        Model model = new ModelManager();
        AddCommand command = new AddCommand(new PersonBuilder().build());
        command.execute(model);
        assertTrue(command.requiresSave());
        assertThrows(CommandException.class, () -> command.execute(model));
        assertFalse(command.requiresSave());
        assertTrue(command.canMutate());
    }

    @Test
    public void execute_deleteThenInvalidIndex_resetsSaveDecisionOnFailure() throws Exception {
        Model model = modelWith(new PersonBuilder().build());
        DeleteCommand command = new DeleteCommand(INDEX_FIRST_PERSON);
        command.execute(model);
        assertTrue(command.requiresSave());
        assertThrows(CommandException.class, () -> command.execute(model));
        assertFalse(command.requiresSave());
    }

    @Test
    public void execute_editThenRepeatAndInvalidIndex_requestsSaveOnlyForChangedContact() throws Exception {
        Model model = modelWith(new PersonBuilder().withName("Original Student").build());
        EditCommand.EditPersonDescriptor descriptor = new EditCommand.EditPersonDescriptor();
        descriptor.setName(new Name("Renamed Student"));
        EditCommand command = new EditCommand(INDEX_FIRST_PERSON, descriptor);
        command.execute(model);
        assertTrue(command.requiresSave());
        model.updateFilteredPersonList(person -> false);
        assertThrows(CommandException.class, () -> command.execute(model));
        assertFalse(command.requiresSave());
        model.updateFilteredPersonList(person -> true);
        command.execute(model);
        assertFalse(command.requiresSave());
    }

    @Test
    public void execute_clearThenRepeatAndClearHiddenContact_requestsSaveOnlyForNonemptyBook() {
        Model model = modelWith(new PersonBuilder().build());
        ClearCommand command = new ClearCommand();
        command.execute(model);
        assertTrue(command.requiresSave());
        command.execute(model);
        assertFalse(command.requiresSave());
        model.addPerson(new PersonBuilder().build());
        model.updateFilteredPersonList(person -> false);
        command.execute(model);
        assertTrue(command.requiresSave());
        assertTrue(model.getTuitionBook().getPersonList().isEmpty());
    }

    @Test
    public void execute_linkThenRepeatAndInvalidIndex_requestsSaveOnlyForChangedRelationship() throws Exception {
        Model model = modelWith(new PersonBuilder().build(), guardian());
        LinkCommand command = new LinkCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON);
        command.execute(model);
        assertTrue(command.requiresSave());
        model.updateFilteredPersonList(person -> false);
        assertThrows(CommandException.class, () -> command.execute(model));
        assertFalse(command.requiresSave());
        model.updateFilteredPersonList(person -> true);
        command.execute(model);
        assertFalse(command.requiresSave());
    }

    @Test
    public void execute_unlinkThenRepeatAndInvalidIndex_requestsSaveOnlyForChangedRelationship() throws Exception {
        Person guardian = guardian();
        Model model = modelWith(new PersonBuilder().withGuardian(guardian).build(), guardian);
        UnlinkCommand command = new UnlinkCommand(INDEX_FIRST_PERSON);
        command.execute(model);
        assertTrue(command.requiresSave());
        model.updateFilteredPersonList(person -> false);
        assertThrows(CommandException.class, () -> command.execute(model));
        assertFalse(command.requiresSave());
        model.updateFilteredPersonList(person -> true);
        command.execute(model);
        assertFalse(command.requiresSave());
    }

    private static Person guardian() {
        return new PersonBuilder().withName("Grace Guardian").withPhone("90000001").withRole(Role.GUARDIAN).build();
    }

    private static Model modelWith(Person... persons) {
        Model model = new ModelManager();
        for (Person person : persons) {
            model.addPerson(person);
        }
        return model;
    }
}
