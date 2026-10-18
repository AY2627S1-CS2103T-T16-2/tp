package seedu.tuitionbook.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.tuitionbook.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Objects;

import seedu.tuitionbook.commons.util.ToStringBuilder;
import seedu.tuitionbook.logic.Messages;
import seedu.tuitionbook.model.Model;
import java.util.function.Predicate;
import seedu.tuitionbook.model.person.Person;

/**
 * Lists persons in TuitionBook to the user, optionally filtered by role and tags.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String MESSAGE_SUCCESS = "Listed all persons.";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists all persons, or only those matching the given role and tags.\n"
            + "Parameters: [" + PREFIX_ROLE + "ROLE] [" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_ROLE + "student " + PREFIX_TAG + "Sec4";

    private final Predicate<Person> predicate;

    /**
     * Creates a ListCommand that lists every person.
     */
    public ListCommand() {
        this.predicate = null;
    }

    /**
     * Creates a ListCommand that lists only the persons matching the given predicate.
     */
    public ListCommand(Predicate<Person> predicate) {
        requireNonNull(predicate);
        this.predicate = predicate;
    }

    @Override
    public boolean requiresSave() {
        return false;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        if (predicate == null) {
            model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
            return new CommandResult(MESSAGE_SUCCESS);
        }
        model.updateFilteredPersonList(predicate);
        return new CommandResult(
                String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ListCommand otherListCommand)) {
            return false;
        }

        return Objects.equals(predicate, otherListCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
