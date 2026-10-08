package seedu.tuitionbook.logic.parser;

import static seedu.tuitionbook.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.tuitionbook.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.tuitionbook.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.logic.Messages;
import seedu.tuitionbook.logic.commands.ListCommand;
import seedu.tuitionbook.model.person.PersonHasRolePredicate;
import seedu.tuitionbook.model.person.Role;

public class ListCommandParserTest {

    private ListCommandParser parser = new ListCommandParser();

    @Test
    public void parse_emptyArgs_returnsListCommandForAllPersons() {
        assertParseSuccess(parser, "", new ListCommand());
        assertParseSuccess(parser, "   ", new ListCommand());
    }

    @Test
    public void parse_validRole_returnsFilteredListCommand() {
        ListCommand expectedStudentCommand = new ListCommand(new PersonHasRolePredicate(Role.STUDENT));
        assertParseSuccess(parser, " r/student", expectedStudentCommand);
        assertParseSuccess(parser, " r/ STUDENT ", expectedStudentCommand);

        ListCommand expectedGuardianCommand = new ListCommand(new PersonHasRolePredicate(Role.GUARDIAN));
        assertParseSuccess(parser, " r/guardian", expectedGuardianCommand);
    }

    @Test
    public void parse_invalidRole_throwsParseException() {
        assertParseFailure(parser, " r/teacher", Role.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " r/", Role.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_nonEmptyPreamble_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " 3", expectedMessage);
        assertParseFailure(parser, " students", expectedMessage);
        assertParseFailure(parser, " 3 r/student", expectedMessage);
    }

    @Test
    public void parse_duplicateRolePrefix_throwsParseException() {
        assertParseFailure(parser, " r/student r/guardian",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_ROLE));
    }
}
