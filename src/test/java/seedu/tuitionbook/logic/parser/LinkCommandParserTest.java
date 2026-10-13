package seedu.tuitionbook.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.tuitionbook.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.tuitionbook.logic.commands.LinkCommand.MESSAGE_USAGE;
import static seedu.tuitionbook.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.commons.core.index.Index;
import seedu.tuitionbook.logic.commands.LinkCommand;

public class LinkCommandParserTest {
    private final LinkCommandParser parser = new LinkCommandParser();

    @Test
    public void parse_validInput_success() throws Exception {
        assertEquals(new LinkCommand(Index.fromOneBased(1), Index.fromOneBased(2)), parser.parse("1 2"));
    }

    @Test
    public void parse_invalidInput_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, MESSAGE_USAGE);
        assertParseFailure(parser, "1", expectedMessage);
        assertParseFailure(parser, "1 2 3", expectedMessage);
        assertParseFailure(parser, "0 2", expectedMessage);
        assertParseFailure(parser, "-1 2", expectedMessage);
        assertParseFailure(parser, "one 2", expectedMessage);
        assertParseFailure(parser, "1 one", expectedMessage);
        assertParseFailure(parser, "1 " + Long.toString(Integer.MAX_VALUE + 1L), expectedMessage);
    }
}
