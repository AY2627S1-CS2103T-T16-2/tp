package seedu.tuitionbook.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.tuitionbook.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;

import seedu.tuitionbook.commons.core.index.Index;
import seedu.tuitionbook.commons.util.ToStringBuilder;
import seedu.tuitionbook.logic.Messages;
import seedu.tuitionbook.logic.commands.exceptions.CommandException;
import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;

/** Links a student to a guardian using their displayed indexes. */
public class LinkCommand extends Command {
    public static final String COMMAND_WORD = "link";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Links a student to a guardian using their displayed indexes.\n"
            + "Parameters: STUDENT_INDEX GUARDIAN_INDEX\n"
            + "Example: " + COMMAND_WORD + " 1 2";
    public static final String MESSAGE_LINK_SUCCESS = "Linked student %1$s to guardian %2$s.";
    public static final String MESSAGE_RELINK_SUCCESS = "Linked student %1$s to guardian %2$s."
            + " Previous guardian unlinked: %3$s.";
    public static final String MESSAGE_ALREADY_LINKED = "Student %1$s is already linked to guardian %2$s.";
    public static final String MESSAGE_STUDENT_REQUIRED = "The first index must refer to a student.";
    public static final String MESSAGE_GUARDIAN_REQUIRED = "The second index must refer to a guardian.";

    private final Index studentIndex;
    private final Index guardianIndex;

    /**
     * @param studentIndex The index of the student to link
     * @param guardianIndex The index of the guardian to link
     */
    public LinkCommand(Index studentIndex, Index guardianIndex) {
        this.studentIndex = requireNonNull(studentIndex);
        this.guardianIndex = requireNonNull(guardianIndex);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> persons = model.getFilteredPersonList();
        if (studentIndex.getZeroBased() >= persons.size() || guardianIndex.getZeroBased() >= persons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person student = persons.get(studentIndex.getZeroBased());
        Person guardian = persons.get(guardianIndex.getZeroBased());
        if (student.getRole() != Role.STUDENT) {
            throw new CommandException(MESSAGE_STUDENT_REQUIRED);
        }
        if (guardian.getRole() != Role.GUARDIAN) {
            throw new CommandException(MESSAGE_GUARDIAN_REQUIRED);
        }

        if (student.getGuardianId().filter(guardian.getId()::equals).isPresent()) {
            return new CommandResult(String.format(MESSAGE_ALREADY_LINKED, student.getName(), guardian.getName()));
        }

        String previousGuardianName = student.getGuardianId()
                .flatMap(id -> persons.stream().filter(person -> person.getId().equals(id)).findFirst())
                .map(person -> person.getName().toString())
                .orElse(null);
        model.setPerson(student, student.withGuardianId(guardian.getId()));
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        if (previousGuardianName != null) {
            return new CommandResult(String.format(MESSAGE_RELINK_SUCCESS, student.getName(), guardian.getName(),
                    previousGuardianName));
        }
        return new CommandResult(String.format(MESSAGE_LINK_SUCCESS, student.getName(), guardian.getName()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof LinkCommand otherLinkCommand)) {
            return false;
        }
        return studentIndex.equals(otherLinkCommand.studentIndex)
                && guardianIndex.equals(otherLinkCommand.guardianIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("studentIndex", studentIndex).add("guardianIndex", guardianIndex)
                .toString();
    }
}
