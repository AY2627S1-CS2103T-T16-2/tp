package seedu.tuitionbook.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.tuitionbook.logic.commands.LinkCommand.MESSAGE_USAGE;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.commons.core.index.Index;
import seedu.tuitionbook.logic.commands.LinkCommand;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;

public class LinkCommandParserTest {
    private final LinkCommandParser parser = new LinkCommandParser();

    @Test
    public void parse_validInput_success() throws Exception {
        assertEquals(new LinkCommand(Index.fromOneBased(1), Index.fromOneBased(2)), parser.parse("1 2"));
    }

    @Test
    public void parse_invalidInput_failure() {
        assertThrows(ParseException.class, () -> parser.parse("1"));
        assertThrows(ParseException.class, () -> parser.parse("1 2 3"));
        assertThrows(ParseException.class, () -> parser.parse("0 2"));
        assertThrows(ParseException.class, () -> parser.parse("one 2"));
        assertEquals(String.format(seedu.tuitionbook.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT, MESSAGE_USAGE),
                assertThrows(ParseException.class, () -> parser.parse("1")).getMessage());
    }
}
