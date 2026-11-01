package seedu.tuitionbook.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.tuitionbook.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.tuitionbook.model.Model.SHOW_ALL_PERSONS_REFRESH_COMMAND;

import java.util.Objects;
import java.util.function.Predicate;

import seedu.tuitionbook.commons.util.ToStringBuilder;
import seedu.tuitionbook.logic.Messages;
import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.PersonHasRolePredicate;
import seedu.tuitionbook.model.person.PersonMatchesListFilterPredicate;

/**
 * Lists persons in TuitionBook to the user, optionally filtered by role and tags.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = SHOW_ALL_PERSONS_REFRESH_COMMAND;

    public static final String MESSAGE_SUCCESS = "Listed all persons.";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists all persons, or only those matching the given role and tags.\n"
            + "Parameters: [" + PREFIX_ROLE + "ROLE] [" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_ROLE + "student " + PREFIX_TAG + "Sec4";

    private final Predicate<Person> predicate;
    private final String refreshCommand;

    /**
     * Creates a ListCommand that lists every person.
     */
    public ListCommand() {
        this.predicate = null;
        this.refreshCommand = COMMAND_WORD;
    }

    /** Creates a role-filtered command and its equivalent refresh text from the parsed role. */
    public ListCommand(PersonHasRolePredicate predicate) {
        this(predicate, COMMAND_WORD + " " + PREFIX_ROLE + requireNonNull(predicate).getRole());
    }

    /** Creates a tag/role-filtered command and its equivalent refresh text from the parsed criteria. */
    public ListCommand(PersonMatchesListFilterPredicate predicate) {
        this(predicate, formatRefreshCommand(requireNonNull(predicate)));
    }

    private ListCommand(Predicate<Person> predicate, String refreshCommand) {
        requireNonNull(predicate);
        this.predicate = predicate;
        this.refreshCommand = refreshCommand;
    }

    private static String formatRefreshCommand(PersonMatchesListFilterPredicate predicate) {
        StringBuilder command = new StringBuilder(COMMAND_WORD);
        if (predicate.getRole() != null) {
            command.append(" ").append(PREFIX_ROLE).append(predicate.getRole());
        }
        for (String tag : predicate.getTags()) {
            command.append(" ").append(PREFIX_TAG);
            if (tag.matches("[A-Za-z0-9_-]+")) {
                command.append(tag);
            } else {
                command.append('"').append(tag.replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
            }
        }
        return command.toString();
    }

    @Override
    public boolean canMutate() {
        return false;
    }

    @Override
    public boolean requiresSave() {
        return false;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        if (predicate == null) {
            model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS, refreshCommand);
            return new CommandResult(MESSAGE_SUCCESS);
        }
        model.updateFilteredPersonList(predicate, refreshCommand);
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
    public int hashCode() {
        return Objects.hashCode(predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
