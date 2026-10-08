package seedu.tuitionbook.logic.parser;

import static seedu.tuitionbook.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.tuitionbook.commons.core.index.Index;
import seedu.tuitionbook.logic.commands.ViewCommand;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;

/**
 * Parses the displayed index of a contact to view.
 */
public class ViewCommandParser implements Parser<ViewCommand> {

    /**
     * Returns a view command for the given index.
     *
     * @throws ParseException if the index is missing or invalid
     */
    @Override
    public ViewCommand parse(String args) throws ParseException {
        try {
            Index index = ParserUtil.parseIndex(args);
            return new ViewCommand(index);
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewCommand.MESSAGE_USAGE), pe);
        }
    }
}
