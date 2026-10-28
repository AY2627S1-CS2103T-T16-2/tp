package seedu.tuitionbook.logic.commands;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.tuitionbook.commons.core.index.Index;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.tag.Tag;

/** Removes only the specified tags from a displayed contact. */
public class UntagCommand extends TagModificationCommand {
    public static final String COMMAND_WORD = "untag";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Removes specified tags from the contact identified by its displayed index, keeping other tags.\n"
            + "Parameters: INDEX t/TAG [t/TAG]...\n"
            + "Absent tags are skipped and reported.\n"
            + "Example: untag 1 t/Math t/Sec 4";
    public static final String MESSAGE_SUCCESS = "Removed tags from %1$s.";
    public static final String MESSAGE_NO_CHANGE = "None of the specified tags are present on %1$s.";
    public static final String MESSAGE_TAGS_NOT_PRESENT = " Tags not present: %1$s.";
    public static final String MESSAGE_CASE_SUGGESTION =
            " Tag %1$s not found, did you mean %2$s?";

    /**
     * Creates a command removing tags from the selected contact.
     *
     * @param index Index of the contact in the currently displayed list.
     * @param tags Non-empty set of tags to remove; absent tags are skipped and reported.
     */
    public UntagCommand(Index index, Set<Tag> tags) {
        super(index, tags, MESSAGE_SUCCESS, MESSAGE_NO_CHANGE);
    }

    @Override
    protected boolean updateTags(Set<Tag> updatedTags) {
        return updatedTags.removeAll(tags);
    }

    @Override
    protected String formatFeedback(Person original, boolean changed) {
        String missingTags = formatTagsAbsentFrom(original);
        String feedback = super.formatFeedback(original, changed);
        if (missingTags.isEmpty()) {
            return feedback;
        }
        String suggestions = tags.stream()
                .filter(tag -> !original.getTags().contains(tag))
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .map(tag -> formatCaseSuggestion(tag, original))
                .collect(Collectors.joining());
        return feedback + String.format(MESSAGE_TAGS_NOT_PRESENT, missingTags) + suggestions;
    }

    /** Suggests only case variants that remain on this contact after the requested removals. */
    private String formatCaseSuggestion(Tag requested, Person original) {
        String storedSpellings = original.getTags().stream()
                .filter(tag -> !tags.contains(tag) && tag.tagName.equalsIgnoreCase(requested.tagName))
                .map(tag -> tag.tagName)
                .sorted()
                .map(TagModificationCommand::quoteTagName)
                .collect(Collectors.joining(", "));
        return storedSpellings.isEmpty() ? ""
                : String.format(MESSAGE_CASE_SUGGESTION, quoteTagName(requested.tagName), storedSpellings);
    }
}
