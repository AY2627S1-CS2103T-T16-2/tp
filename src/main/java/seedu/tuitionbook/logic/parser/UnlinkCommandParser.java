package seedu.tuitionbook.logic.parser;

import static seedu.tuitionbook.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.tuitionbook.logic.commands.UnlinkCommand;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;

/** Parses the displayed student index required by {@code unlink}. */
public class UnlinkCommandParser implements Parser<UnlinkCommand> {
    @Override
    public UnlinkCommand parse(String args) throws ParseException {
        ParserUtil.checkForUnsupportedSpaces(args);
        try {
            return new UnlinkCommand(ParserUtil.parseIndex(args));
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, UnlinkCommand.MESSAGE_USAGE), pe);
        }
    }
}
