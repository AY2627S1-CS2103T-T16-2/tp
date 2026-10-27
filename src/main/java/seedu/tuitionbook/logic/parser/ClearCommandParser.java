package seedu.tuitionbook.logic.parser;

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
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(ClearCommand.MESSAGE_CONFIRMATION_REQUIRED);
        }
        if (!trimmedArgs.equals(ClearCommand.CONFIRMATION_KEYWORD)) {
            throw new ParseException(ClearCommand.MESSAGE_INVALID_CONFIRMATION);
        }

        return new ClearCommand();
    }
}
