package seedu.tuitionbook.logic.commands;

import seedu.tuitionbook.logic.commands.exceptions.CommandException;
import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.storage.Storage;

/**
 * Represents a command with hidden internal logic and the ability to be executed.
 */
public abstract class Command {

    /**
     * Executes the command with access to the model and storage.
     * The default implementation delegates to {@link #execute(Model)} without using storage.
     * Commands that require storage access override this method.
     *
     * @param model Model on which the command operates.
     * @param storage Storage available to commands that need to read or write persisted data.
     * @return Result containing user feedback and any requested UI actions.
     * @throws CommandException If the command cannot be completed.
     */
    public CommandResult execute(Model model, Storage storage) throws CommandException {
        return execute(model);
    }

    /**
     * Executes the command and returns the result message.
     *
     * @param model {@code Model} which the command should operate on.
     * @return feedback message of the operation result for display
     * @throws CommandException If an error occurs during command execution.
     */
    public abstract CommandResult execute(Model model) throws CommandException;

    /**
     * Returns whether this command requires the contact data to be saved after successful execution.
     */
    public abstract boolean requiresSave();

    /**
     * Returns whether executing this command can change contact data and may require persistence.
     *
     * <p>This is distinct from {@link #requiresSave()}, which is evaluated after execution and can be false when a
     * command completes without making a change.</p>
     */
    public boolean canMutate() {
        return requiresSave();
    }

}
