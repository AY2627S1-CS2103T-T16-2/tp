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

/** Removes a student's guardian link using the student's displayed index. */
public class UnlinkCommand extends Command {

    public static final String COMMAND_WORD = "unlink";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Removes the guardian link of the student identified by the displayed index.\n"
            + "Parameters: STUDENT_INDEX\n"
            + "Example: " + COMMAND_WORD + " 1";
    public static final String MESSAGE_UNLINK_SUCCESS = "Unlinked student %1$s from guardian %2$s.";
    public static final String MESSAGE_NO_LINKED_GUARDIAN = "Student %1$s has no linked guardian.";
    public static final String MESSAGE_INVALID_STUDENT_INDEX = "Student index: %1$s";
    public static final String MESSAGE_STUDENT_REQUIRED = "The index must refer to a student.";

    private final Index studentIndex;
    private boolean requiresSave;

    /** Creates an unlink command for the student at the given displayed index. */
    public UnlinkCommand(Index studentIndex) {
        this.studentIndex = requireNonNull(studentIndex);
    }

    @Override
    public boolean requiresSave() {
        return requiresSave;
    }

    @Override
    public boolean canMutate() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        requiresSave = false;
        List<Person> persons = model.getFilteredPersonList();
        int displayedPersonCount = persons.size();
        if (studentIndex.getZeroBased() >= displayedPersonCount) {
            throw new CommandException(String.format(MESSAGE_INVALID_STUDENT_INDEX,
                    Messages.getInvalidPersonDisplayedIndexMessage(displayedPersonCount)));
        }

        Person student = persons.get(studentIndex.getZeroBased());
        if (student.getRole() != Role.STUDENT) {
            throw new CommandException(MESSAGE_STUDENT_REQUIRED);
        }

        Person guardian = student.getGuardianId()
                .flatMap(id -> model.getTuitionBook().getPersonList().stream()
                        .filter(person -> person.getId().equals(id)).findFirst())
                .orElse(null);
        if (guardian == null) {
            return new CommandResult(String.format(MESSAGE_NO_LINKED_GUARDIAN, student.getName()));
        }

        model.setPerson(student, student.clearGuardian());
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        requiresSave = true;
        return new CommandResult(String.format(MESSAGE_UNLINK_SUCCESS, student.getName(), guardian.getName()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof UnlinkCommand otherUnlinkCommand)) {
            return false;
        }
        return studentIndex.equals(otherUnlinkCommand.studentIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("studentIndex", studentIndex).toString();
    }
}
