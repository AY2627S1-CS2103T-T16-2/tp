package seedu.tuitionbook.model.tag;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;

/**
 * Represents a Tag in TuitionBook.
 * Guarantees: immutable; name is valid as declared in {@link #isValidTagName(String)}
 */
public class Tag {

    public static final String MESSAGE_BLANK = "Tag names must not be blank.";
    public static final String MESSAGE_INVALID_CHARACTERS =
            "Tag names may contain only English letters, digits, spaces, and punctuation.";
    /** Orders tags by case-insensitive names, then by their original names. */
    public static final Comparator<Tag> DISPLAY_COMPARATOR = Comparator
            .comparing((Tag tag) -> tag.tagName, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(tag -> tag.tagName);

    public final String tagName;

    /**
     * Constructs a {@code Tag}.
     *
     * @param tagName A valid tag name.
     */
    public Tag(String tagName) {
        requireNonNull(tagName);
        String normalizedTagName = trimSurroundingSpaces(tagName);
        if (!isValidTagName(normalizedTagName)) {
            throw new IllegalArgumentException(getConstraintMessage(normalizedTagName));
        }
        this.tagName = normalizedTagName;
    }

    private static String trimSurroundingSpaces(String tagName) {
        int start = 0;
        int end = tagName.length();
        while (start < end && tagName.charAt(start) == ' ') {
            start++;
        }
        while (end > start && tagName.charAt(end - 1) == ' ') {
            end--;
        }
        return tagName.substring(start, end);
    }

    /**
     * Returns true if the name contains only printable ASCII characters and is not blank.
     * When a {@code Tag} is constructed, surrounding ASCII spaces are trimmed before this
     * validation and storage, so the stored value can differ from the constructor argument.
     * Internal spaces and punctuation are preserved.
     */
    public static boolean isValidTagName(String test) {
        requireNonNull(test);
        return hasOnlyPrintableAscii(test) && test.chars().anyMatch(character -> character != ' ');
    }

    /**
     * Returns the message describing the constraint that the given invalid tag name violates.
     */
    public static String getConstraintMessage(String invalidTagName) {
        requireNonNull(invalidTagName);
        assert !isValidTagName(invalidTagName);
        return hasOnlyPrintableAscii(invalidTagName) ? MESSAGE_BLANK : MESSAGE_INVALID_CHARACTERS;
    }

    private static boolean hasOnlyPrintableAscii(String test) {
        return test.chars().allMatch(character -> character >= ' ' && character <= '~');
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Tag otherTag)) {
            return false;
        }

        return tagName.equals(otherTag.tagName);
    }

    @Override
    public int hashCode() {
        return tagName.hashCode();
    }

    /**
     * Formats state as text for viewing.
     */
    public String toString() {
        return '[' + tagName + ']';
    }

}
