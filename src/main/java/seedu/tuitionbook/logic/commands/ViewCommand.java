package seedu.tuitionbook.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import seedu.tuitionbook.commons.core.index.Index;
import seedu.tuitionbook.commons.util.ToStringBuilder;
import seedu.tuitionbook.logic.Messages;
import seedu.tuitionbook.logic.commands.exceptions.CommandException;
import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;

/**
 * Displays a contact and their guardian or linked students without changing TuitionBook.
 */
public class ViewCommand extends Command {

    public static final String COMMAND_WORD = "view";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Displays a contact and their guardian or linked students.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";
    public static final String MESSAGE_SUCCESS = "Viewing contact: %1$s";
    public static final String MESSAGE_NO_GUARDIAN = "No guardian linked.";
    public static final String MESSAGE_NO_STUDENTS = "No students linked.";

    private final Index targetIndex;

    /**
     * Creates a command to view the contact at the given displayed index.
     */
    public ViewCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedPersons = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= displayedPersons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person selectedPerson = displayedPersons.get(targetIndex.getZeroBased());
        List<Person> allPersons = model.getTuitionBook().getPersonList();
        StringBuilder result = new StringBuilder();
        appendContactDetails(result, selectedPerson);

        if (selectedPerson.getRole() == Role.STUDENT) {
            appendGuardianDetails(result, selectedPerson, allPersons);
        } else {
            appendLinkedStudents(result, selectedPerson, allPersons);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, selectedPerson.getName()), result.toString());
    }

    private static void appendContactDetails(StringBuilder result, Person person) {
        String tags = person.getTags().stream()
                .map(tag -> tag.tagName)
                .sorted()
                .collect(Collectors.joining(", "));
        result.append("Name: ").append(person.getName()).append('\n')
                .append("Role: ").append(person.getRole().name()).append('\n')
                .append("Phone: ").append(person.getPhone()).append('\n')
                .append("Email: ").append(person.getEmail().map(Object::toString).orElse("-")).append('\n')
                .append("Address: ").append(person.getAddress().map(Object::toString).orElse("-")).append('\n')
                .append("Tags: ").append(tags.isEmpty() ? "-" : tags);
    }

    private static void appendGuardianDetails(StringBuilder result, Person student, List<Person> allPersons) {
        if (student.getGuardianId().isEmpty()) {
            result.append("\n\n").append(MESSAGE_NO_GUARDIAN);
            return;
        }

        UUID guardianId = student.getGuardianId().orElseThrow();
        for (Person person : allPersons) {
            if (person.getId().equals(guardianId) && person.getRole() == Role.GUARDIAN) {
                result.append("\n\nGuardian details:\n");
                appendContactDetails(result, person);
                return;
            }
        }
        result.append("\n\n").append(MESSAGE_NO_GUARDIAN);
    }

    private static void appendLinkedStudents(StringBuilder result, Person guardian, List<Person> allPersons) {
        boolean hasLinkedStudents = false;
        for (Person person : allPersons) {
            boolean isLinkedStudent = person.getRole() == Role.STUDENT
                    && person.getGuardianId().filter(guardian.getId()::equals).isPresent();
            if (isLinkedStudent) {
                if (!hasLinkedStudents) {
                    result.append("\n\nLinked students:");
                    hasLinkedStudents = true;
                }
                result.append("\n- ").append(person.getName());
            }
        }
        if (!hasLinkedStudents) {
            result.append("\n\n").append(MESSAGE_NO_STUDENTS);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ViewCommand otherViewCommand)) {
            return false;
        }
        return targetIndex.equals(otherViewCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
