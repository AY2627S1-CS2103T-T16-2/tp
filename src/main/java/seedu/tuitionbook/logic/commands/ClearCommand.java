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

    private boolean hasExecuted;

    private boolean requiresSave;

    @Override
    public boolean requiresSave() {
        assert hasExecuted : "execute() must be called before requiresSave()";
        return requiresSave;
    }

    @Override
    public boolean canMutate() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) {
        hasExecuted = true;
        requireNonNull(model);
        requiresSave = false;
        boolean hasContacts = !model.getTuitionBook().getPersonList().isEmpty();
        model.setTuitionBook(new TuitionBook());
        requiresSave = hasContacts;
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
