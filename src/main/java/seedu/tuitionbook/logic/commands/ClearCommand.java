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
    public static final String MESSAGE_CONFIRMATION_REQUIRED =
            "Confirmation required: enter \"clear confirm\" to clear all contacts.";
    public static final String MESSAGE_INVALID_CONFIRMATION =
            "Invalid confirmation: enter \"clear confirm\" to clear all contacts.";

    private boolean requiresSave;

    @Override
    public boolean requiresSave() {
        return requiresSave;
    }

    @Override
    public boolean canMutate() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        requiresSave = false;
        boolean hasContacts = !model.getTuitionBook().getPersonList().isEmpty();
        model.setTuitionBook(new TuitionBook());
        requiresSave = hasContacts;
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
