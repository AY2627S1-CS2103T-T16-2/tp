package seedu.tuitionbook.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.tuitionbook.commons.util.ToStringBuilder;
import seedu.tuitionbook.logic.Messages;
import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.person.NameContainsKeywordsPredicate;

/**
 * Finds and lists persons whose names contain at least one argument as a literal substring anywhere,
 * using case-insensitive matching. Only names are searched, and multiple keywords use OR.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds contacts whose names contain any of the "
            + "given keywords, ignoring case. A keyword can match part of a name, e.g. rac matches Grace. "
            + "Only names are searched.\n"
            + "Parameters: KEYWORD [MORE_KEYWORDS]...\n"
            + "Example: " + COMMAND_WORD + " rac bob";

    private final NameContainsKeywordsPredicate predicate;

    public FindCommand(NameContainsKeywordsPredicate predicate) {
        this.predicate = predicate;
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
        model.updateFilteredPersonList(predicate, COMMAND_WORD + " " + String.join(" ", predicate.getKeywords()));
        return new CommandResult(
                String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public int hashCode() {
        return predicate.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
