package seedu.tuitionbook.logic.parser;

import seedu.tuitionbook.logic.commands.TagCommand;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;

/** Parses the displayed contact index and tags to add. */
public class TagCommandParser implements Parser<TagCommand> {
    @Override
    public TagCommand parse(String args) throws ParseException {
        return TagModificationCommandParser.parse(args, TagCommand.MESSAGE_USAGE, TagCommand::new);
    }
}
