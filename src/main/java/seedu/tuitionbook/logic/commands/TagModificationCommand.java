package seedu.tuitionbook.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.tuitionbook.commons.util.AppUtil.checkArgument;
import static seedu.tuitionbook.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import seedu.tuitionbook.commons.core.index.Index;
import seedu.tuitionbook.commons.util.ToStringBuilder;
import seedu.tuitionbook.logic.Messages;
import seedu.tuitionbook.logic.commands.exceptions.CommandException;
import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.tag.Tag;

/** Shared execution for incremental tag changes, preserving all other contact data. */
abstract class TagModificationCommand extends Command {
    protected final Set<Tag> tags;
    private final Index index;
    private final String successMessage;
    private final String noChangeMessage;
    private boolean requiresSave;

    /**
     * Creates an incremental tag command with a defensive copy of the requested tags.
     *
     * @param index Index of the contact in the currently displayed list.
     * @param tags Non-empty set of tags to add or remove.
     * @param successMessage Feedback format used when tags change, with the contact name as its argument.
     * @param noChangeMessage Feedback format used when no tags change, with the contact name as its argument.
     */
    TagModificationCommand(Index index, Set<Tag> tags, String successMessage, String noChangeMessage) {
        this.index = requireNonNull(index);
        this.tags = Set.copyOf(tags);
        checkArgument(!tags.isEmpty(), "At least one tag must be supplied.");
        this.successMessage = successMessage;
        this.noChangeMessage = noChangeMessage;
    }

    @Override
    public boolean canMutate() {
        return true;
    }

    @Override
    public boolean requiresSave() {
        return requiresSave;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        requiresSave = false;
        List<Person> displayedPersons = model.getFilteredPersonList();
        if (index.getZeroBased() >= displayedPersons.size()) {
            throw new CommandException(Messages.getInvalidPersonDisplayedIndexMessage(displayedPersons.size()));
        }
        Person original = displayedPersons.get(index.getZeroBased());
        Set<Tag> updatedTags = new HashSet<>(original.getTags());
        if (!updateTags(updatedTags)) {
            return new CommandResult(formatFeedback(original, false));
        }
        Person updated = new Person(original.getId(), original.getRole(), original.getGuardianId().orElse(null),
                original.getName(), original.getPhone(), original.getEmail().orElse(null),
                original.getAddress().orElse(null), updatedTags);
        model.setPerson(original, updated);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        requiresSave = true;
        return new CommandResult(formatFeedback(original, true));
    }

    /**
     * Formats feedback using the contact's original tags, before any modification.
     *
     * @param original Contact before the command's tag changes.
     * @param changed Whether the command changed the tag set.
     * @return Feedback describing the command's result.
     */
    protected String formatFeedback(Person original, boolean changed) {
        return String.format(changed ? successMessage : noChangeMessage, original.getName());
    }

    /**
     * Updates a disposable copy of the contact's tags.
     *
     * @param updatedTags Mutable copy of the contact's current tag set to modify.
     * @return Whether the set changed.
     */
    protected abstract boolean updateTags(Set<Tag> updatedTags);

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        TagModificationCommand otherCommand = (TagModificationCommand) other;
        return index.equals(otherCommand.index) && tags.equals(otherCommand.tags);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("tags", tags).toString();
    }
}
