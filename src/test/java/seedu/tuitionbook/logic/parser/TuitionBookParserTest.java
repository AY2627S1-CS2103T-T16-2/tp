package seedu.tuitionbook.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.tuitionbook.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.tuitionbook.testutil.Assert.assertThrows;
import static seedu.tuitionbook.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.logic.commands.AddCommand;
import seedu.tuitionbook.logic.commands.ClearCommand;
import seedu.tuitionbook.logic.commands.DeleteCommand;
import seedu.tuitionbook.logic.commands.EditCommand;
import seedu.tuitionbook.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.tuitionbook.logic.commands.ExitCommand;
import seedu.tuitionbook.logic.commands.FindCommand;
import seedu.tuitionbook.logic.commands.HelpCommand;
import seedu.tuitionbook.logic.commands.LinkCommand;
import seedu.tuitionbook.logic.commands.ListCommand;
import seedu.tuitionbook.logic.commands.ViewCommand;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;
import seedu.tuitionbook.model.person.NameContainsKeywordsPredicate;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.PersonHasRolePredicate;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.testutil.EditPersonDescriptorBuilder;
import seedu.tuitionbook.testutil.PersonBuilder;
import seedu.tuitionbook.testutil.PersonUtil;

public class TuitionBookParserTest {

    private final TuitionBookParser parser = new TuitionBookParser();

    @Test
    public void parseCommand_add() throws Exception {
        Person person = new PersonBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD) instanceof ClearCommand);
        assertTrue(parser.parseCommand(ClearCommand.COMMAND_WORD + " 3") instanceof ClearCommand);
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Person person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_find() throws Exception {
        List<String> keywords = List.of("foo", "bar", "baz");
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " " + keywords.stream().collect(Collectors.joining(" ")));
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(keywords)), command);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD + " 3") instanceof HelpCommand);
    }

    @Test
    public void parseCommand_list() throws Exception {
        assertTrue(parser.parseCommand(ListCommand.COMMAND_WORD) instanceof ListCommand);
        assertEquals(new ListCommand(new PersonHasRolePredicate(Role.STUDENT)),
                parser.parseCommand(ListCommand.COMMAND_WORD + " r/student"));
        assertThrows(ParseException.class, () -> parser.parseCommand(ListCommand.COMMAND_WORD + " 3"));
    }

    @Test
    public void parseCommand_link() throws Exception {
        assertEquals(new LinkCommand(INDEX_FIRST_PERSON, seedu.tuitionbook.testutil.TypicalIndexes.INDEX_SECOND_PERSON),
                parser.parseCommand(LinkCommand.COMMAND_WORD + " 1 2"));
    }

    @Test
    public void parseCommand_view() throws Exception {
        assertEquals(new ViewCommand(INDEX_FIRST_PERSON),
                parser.parseCommand(ViewCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased()));
        assertThrows(ParseException.class, () -> parser.parseCommand(ViewCommand.COMMAND_WORD));
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
    }
}
