package seedu.tuitionbook.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.TuitionBook;

/**
 * Clears TuitionBook.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String CONFIRMATION_KEYWORD = "confirm";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Clears all contacts, including contacts hidden by the current filter.\n"
            + "Parameters: " + CONFIRMATION_KEYWORD + "\n"
            + "Example: " + COMMAND_WORD + " " + CONFIRMATION_KEYWORD;
    public static final String MESSAGE_SUCCESS =
            "All contacts have been cleared, including contacts hidden by the current filter.";


    @Override
    public boolean requiresSave() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setTuitionBook(new TuitionBook());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
