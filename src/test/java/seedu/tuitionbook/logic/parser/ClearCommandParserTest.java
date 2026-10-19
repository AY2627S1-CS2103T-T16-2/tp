package seedu.tuitionbook.logic.parser;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static seedu.tuitionbook.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.tuitionbook.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.logic.commands.ClearCommand;

public class ClearCommandParserTest {

    private final ClearCommandParser parser = new ClearCommandParser();

    @Test
    public void parse_confirmationKeyword_returnsClearCommand() throws Exception {
        assertInstanceOf(ClearCommand.class, parser.parse("confirm"));
        assertInstanceOf(ClearCommand.class, parser.parse("   confirm   "));
    }

    @Test
    public void parse_invalidArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, "   ", expectedMessage);
        assertParseFailure(parser, "3", expectedMessage);
        assertParseFailure(parser, "confirm extra", expectedMessage);
        assertParseFailure(parser, "CONFIRM", expectedMessage);
    }
}
