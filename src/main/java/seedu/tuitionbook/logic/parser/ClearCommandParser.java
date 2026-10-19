package seedu.tuitionbook.logic.parser;

import static seedu.tuitionbook.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.tuitionbook.logic.commands.ClearCommand;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new ClearCommand object.
 */
public class ClearCommandParser implements Parser<ClearCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the ClearCommand.
     *
     * @throws ParseException if the user input does not contain only the required confirmation keyword
     */
    public ClearCommand parse(String args) throws ParseException {
        if (!args.trim().equals(ClearCommand.CONFIRMATION_KEYWORD)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE));
        }

        return new ClearCommand();
    }
}
