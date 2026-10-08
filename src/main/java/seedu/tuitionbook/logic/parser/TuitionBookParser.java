package seedu.tuitionbook.logic.parser;

import static seedu.tuitionbook.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.tuitionbook.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.tuitionbook.commons.core.LogsCenter;
import seedu.tuitionbook.logic.commands.AddCommand;
import seedu.tuitionbook.logic.commands.ClearCommand;
import seedu.tuitionbook.logic.commands.Command;
import seedu.tuitionbook.logic.commands.DeleteCommand;
import seedu.tuitionbook.logic.commands.EditCommand;
import seedu.tuitionbook.logic.commands.ExitCommand;
import seedu.tuitionbook.logic.commands.FindCommand;
import seedu.tuitionbook.logic.commands.HelpCommand;
import seedu.tuitionbook.logic.commands.LinkCommand;
import seedu.tuitionbook.logic.commands.ListCommand;
import seedu.tuitionbook.logic.commands.ViewCommand;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;

/**
 * Parses user input.
 */
public class TuitionBookParser {

    /**
     * Used for initial separation of command word and args.
     */
    private static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)");
    private static final Logger logger = LogsCenter.getLogger(TuitionBookParser.class);

    /**
     * Parses user input into command for execution.
     *
     * @param userInput full user input string
     * @return the command based on the user input
     * @throws ParseException if the user input does not conform to the expected format
     */
    public Command parseCommand(String userInput) throws ParseException {
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = matcher.group("commandWord");
        final String arguments = matcher.group("arguments");

        // Note to developers: Change LOG_LEVEL in LogsCenter to enable lower level (i.e., FINE, FINER and lower)
        // log messages such as the one below.
        // Lower level log messages are used sparingly to minimize noise in the code.
        logger.fine("Command word: " + commandWord + "; Arguments: " + arguments);

        return switch (commandWord) {
            case AddCommand.COMMAND_WORD -> new AddCommandParser().parse(arguments);
            case EditCommand.COMMAND_WORD -> new EditCommandParser().parse(arguments);
            case DeleteCommand.COMMAND_WORD -> new DeleteCommandParser().parse(arguments);
            case ClearCommand.COMMAND_WORD -> new ClearCommand();
            case FindCommand.COMMAND_WORD -> new FindCommandParser().parse(arguments);
            case ListCommand.COMMAND_WORD -> new ListCommandParser().parse(arguments);
            case LinkCommand.COMMAND_WORD -> new LinkCommandParser().parse(arguments);
            case ViewCommand.COMMAND_WORD -> new ViewCommandParser().parse(arguments);
            case ExitCommand.COMMAND_WORD -> new ExitCommand();
            case HelpCommand.COMMAND_WORD -> new HelpCommand();
            default -> {
                logger.finer("This user input caused a ParseException: " + userInput);
                throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
            }
        };
    }

}
