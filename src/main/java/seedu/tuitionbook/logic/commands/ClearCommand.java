package seedu.tuitionbook.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.TuitionBook;

/**
 * Clears TuitionBook.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_SUCCESS = "Address book has been cleared!";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setTuitionBook(new TuitionBook());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
