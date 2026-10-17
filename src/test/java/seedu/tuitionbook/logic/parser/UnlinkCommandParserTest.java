package seedu.tuitionbook.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.tuitionbook.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.tuitionbook.logic.commands.UnlinkCommand.MESSAGE_USAGE;
import static seedu.tuitionbook.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.commons.core.index.Index;
import seedu.tuitionbook.logic.commands.UnlinkCommand;

public class UnlinkCommandParserTest {
    private final UnlinkCommandParser parser = new UnlinkCommandParser();

    @Test
    public void parse_validInput_success() throws Exception {
        assertEquals(new UnlinkCommand(Index.fromOneBased(1)), parser.parse("1"));
    }

    @Test
    public void parse_invalidInput_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, MESSAGE_USAGE);
        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, "1 2", expectedMessage);
        assertParseFailure(parser, "0", expectedMessage);
        assertParseFailure(parser, "-1", expectedMessage);
        assertParseFailure(parser, "one", expectedMessage);
        assertParseFailure(parser, Long.toString(Integer.MAX_VALUE + 1L), expectedMessage);
    }
}
