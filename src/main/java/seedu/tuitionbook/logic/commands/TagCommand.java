package seedu.tuitionbook.logic.commands;

import java.util.Set;

import seedu.tuitionbook.commons.core.index.Index;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.tag.Tag;

/** Adds tags to a displayed contact without replacing its existing tags. */
public class TagCommand extends TagModificationCommand {
    public static final String COMMAND_WORD = "tag";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds tags to the contact identified by its displayed index, keeping existing tags.\n"
            + "Parameters: INDEX t/TAG [t/TAG]...\n"
            + "Example: tag 1 t/Math t/Sec 4";
    public static final String MESSAGE_SUCCESS = "Added tags to %1$s: %2$s.";
    public static final String MESSAGE_NO_CHANGE = "All specified tags are already present on %1$s.";

    /**
     * Creates a command adding tags to the selected contact.
     *
     * @param index Index of the contact in the currently displayed list.
     * @param tags Non-empty set of tags to add; existing tags are retained.
     */
    public TagCommand(Index index, Set<Tag> tags) {
        super(index, tags, MESSAGE_SUCCESS, MESSAGE_NO_CHANGE);
    }

    @Override
    protected boolean updateTags(Set<Tag> updatedTags) {
        return updatedTags.addAll(tags);
    }

    @Override
    protected String formatFeedback(Person original, boolean changed) {
        return changed ? String.format(MESSAGE_SUCCESS, original.getName(), formatTagsAbsentFrom(original))
                : super.formatFeedback(original, false);
    }
}
