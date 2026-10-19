package seedu.tuitionbook.logic.parser;

import static seedu.tuitionbook.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.tuitionbook.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.tuitionbook.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.tuitionbook.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.logic.Messages;
import seedu.tuitionbook.logic.commands.ViewCommand;

/**
 * Tests parsing of the view command's displayed index.
 */
public class ViewCommandParserTest {

    private final ViewCommandParser parser = new ViewCommandParser();

    @Test
    public void parse_validIndex_returnsViewCommand() {
        assertParseSuccess(parser, "1", new ViewCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, "  1  ", new ViewCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_validIndexWithVerbosePrefix_returnsVerboseViewCommand() {
        ViewCommand expectedCommand = new ViewCommand(INDEX_FIRST_PERSON, true);

        assertParseSuccess(parser, "1 v/", expectedCommand);
        assertParseSuccess(parser, "  1  v/  ", expectedCommand);
    }

    @Test
    public void parse_missingOrInvalidIndex_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, "0", expectedMessage);
        assertParseFailure(parser, "-1", expectedMessage);
        assertParseFailure(parser, "abc", expectedMessage);
        assertParseFailure(parser, Long.toString(Integer.MAX_VALUE + 1L), expectedMessage);
        assertParseFailure(parser, "1 extra", expectedMessage);
        assertParseFailure(parser, "1 v/details", expectedMessage);
    }

    @Test
    public void parse_duplicateVerbosePrefix_throwsParseException() {
        assertParseFailure(parser, "1 v/ v/",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_VERBOSE));
    }
}
