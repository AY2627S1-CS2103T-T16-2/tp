package seedu.tuitionbook.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_TAG;

import seedu.tuitionbook.commons.util.ToStringBuilder;
import seedu.tuitionbook.logic.Messages;
import seedu.tuitionbook.logic.commands.exceptions.CommandException;
import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.person.Person;

/**
 * Adds a person to TuitionBook.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a contact to TuitionBook. "
            + "Parameters: "
            + PREFIX_ROLE + "ROLE "
            + PREFIX_NAME + "NAME "
            + PREFIX_PHONE + "PHONE "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_ADDRESS + "ADDRESS] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_ROLE + "student "
            + PREFIX_NAME + "John Doe "
            + PREFIX_PHONE + "98765432 "
            + PREFIX_EMAIL + "johnd@example.com "
            + PREFIX_ADDRESS + "311, Clementi Ave 2, #02-25 "
            + PREFIX_TAG + "friends "
            + PREFIX_TAG + "owesMoney\n"
            + "Use double quotes around a name, address or tag containing prefix-like text, for example "
            + "n/\"Rina a/p Kaur\", a/\"20 Road c/o Rina\" or t/\"call p/next week\".\n"
            + "Inside quoted values, use \\\" for a literal quote and \\\\ for a literal backslash.";

    public static final String MESSAGE_SUCCESS = "New person added: %1$s";
    public static final String MESSAGE_DUPLICATE_PERSON = "This person already exists in TuitionBook.";
    public static final String MESSAGE_MISSING_REQUIRED_FIELDS = "Missing required field(s): %1$s\n%2$s";
    public static final String MESSAGE_GUARDIAN_PREFIX_NOT_ALLOWED = "`g/` cannot be used when adding a contact. "
            + "Add the contact first, then link the student to a guardian using the guardian-linking command.";

    private boolean hasExecuted;

    private final Person toAdd;
    private boolean requiresSave;

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public AddCommand(Person person) {
        requireNonNull(person);
        toAdd = person;
    }

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
    public CommandResult execute(Model model) throws CommandException {
        hasExecuted = true;
        requireNonNull(model);
        requiresSave = false;

        if (model.hasPerson(toAdd)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        model.addPerson(toAdd);
        requiresSave = true;
        CommandResult result = new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(toAdd)));
        boolean isDisplayed = model.getFilteredPersonList().stream()
                .anyMatch(person -> person.getId().equals(toAdd.getId()));
        if (!isDisplayed) {
            result = result.withAdditionalFeedback("The new contact is saved but does not match this working set. "
                    + "Use list to locate it. To refresh the current working set, rerun: "
                    + model.getPersonListViewState().refreshCommand());
        }
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
