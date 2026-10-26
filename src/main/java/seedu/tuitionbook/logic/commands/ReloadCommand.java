package seedu.tuitionbook.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.tuitionbook.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.tuitionbook.model.Model.SHOW_ALL_PERSONS_REFRESH_COMMAND;

import seedu.tuitionbook.commons.exceptions.DataLoadingException;
import seedu.tuitionbook.logic.commands.exceptions.CommandException;
import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.storage.Storage;
import seedu.tuitionbook.storage.TuitionBookLoadResult;

/** Reloads the configured data file so users can recover from a blocked load without restarting. */
public class ReloadCommand extends Command {
    public static final String COMMAND_WORD = "reload";
    public static final String MESSAGE_USAGE = "reload: Replaces current contacts with data from the configured file."
            + " Clears the current filter. Takes no arguments.";
    public static final String MESSAGE_SUCCESS = "Reloaded %d contact(s). Saving is enabled; write permissions"
            + " will be checked when saving. Re-enter any command previously rolled back after a save failure.";

    @Override
    public CommandResult execute(Model model) throws CommandException {
        throw new CommandException("Reload requires access to storage.");
    }

    @Override
    public CommandResult execute(Model model, Storage storage) throws CommandException {
        requireNonNull(model);
        requireNonNull(storage);
        TuitionBookLoadResult result;
        try {
            result = storage.readTuitionBookWithResult()
                    .orElseGet(() -> new TuitionBookLoadResult(new TuitionBook(), 0));
        } catch (DataLoadingException exception) {
            String guidance = " " + exception.getUserFacingGuidance();
            String recovery = exception.getMalformedFileBackupPath()
                    .map(path -> " Unreadable file preserved at " + path
                            + ". Saving is enabled. Restore the preserved file before reloading again.")
                    .orElse(" Saving remains blocked. Resolve the problem with the original file, then run reload "
                            + "again.");
            throw new CommandException("Could not reload contacts. Current contacts and filter are unchanged."
                    + guidance + recovery, exception);
        }
        model.setTuitionBook(result.tuitionBook());
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS, SHOW_ALL_PERSONS_REFRESH_COMMAND);
        String message = String.format(MESSAGE_SUCCESS, model.getFilteredPersonList().size());
        if (result.repairedGuardianLinkCount() > 0) {
            message += " Removed " + result.repairedGuardianLinkCount()
                    + " invalid guardian link(s) in memory. Review the data before saving.";
        }
        return new CommandResult(message);
    }

    @Override
    public boolean requiresSave() {
        return false;
    }

    @Override
    public boolean canMutate() {
        return true;
    }
}
