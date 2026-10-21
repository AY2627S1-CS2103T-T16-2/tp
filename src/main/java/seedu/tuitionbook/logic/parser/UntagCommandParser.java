package seedu.tuitionbook.logic.parser;

import seedu.tuitionbook.logic.commands.UntagCommand;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;

/** Parses the displayed contact index and tags to remove. */
public class UntagCommandParser implements Parser<UntagCommand> {
    @Override
    public UntagCommand parse(String args) throws ParseException {
        return TagModificationCommandParser.parse(args, UntagCommand.MESSAGE_USAGE, UntagCommand::new);
    }
}
