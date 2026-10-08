package seedu.tuitionbook.logic.parser;

import static seedu.tuitionbook.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.tuitionbook.logic.commands.LinkCommand;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;

/** Parses the two displayed indexes required by {@code link}. */
public class LinkCommandParser implements Parser<LinkCommand> {
    @Override
    public LinkCommand parse(String args) throws ParseException {
        String[] parts = args.trim().split("\\s+");
        if (parts.length != 2) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, LinkCommand.MESSAGE_USAGE));
        }
        try {
            return new LinkCommand(ParserUtil.parseIndex(parts[0]), ParserUtil.parseIndex(parts[1]));
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, LinkCommand.MESSAGE_USAGE), pe);
        }
    }
}
