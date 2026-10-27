package seedu.tuitionbook.logic.parser;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
        assertParseFailure(parser, "", ClearCommand.MESSAGE_CONFIRMATION_REQUIRED);
        assertParseFailure(parser, "   ", ClearCommand.MESSAGE_CONFIRMATION_REQUIRED);
        assertParseFailure(parser, "3", ClearCommand.MESSAGE_INVALID_CONFIRMATION);
        assertParseFailure(parser, "confirm extra", ClearCommand.MESSAGE_INVALID_CONFIRMATION);
        assertParseFailure(parser, "CONFIRM", ClearCommand.MESSAGE_INVALID_CONFIRMATION);
    }
}
