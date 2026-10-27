package seedu.tuitionbook.model.tag;

import static java.util.Objects.requireNonNull;
import static seedu.tuitionbook.commons.util.AppUtil.checkArgument;

import java.util.Comparator;

/**
 * Represents a Tag in TuitionBook.
 * Guarantees: immutable; name is valid as declared in {@link #isValidTagName(String)}
 */
public class Tag {

    public static final String MESSAGE_CONSTRAINTS = "Tag names must not be blank and may contain only "
            + "ASCII letters, digits, spaces, and printable punctuation";
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
        checkArgument(isValidTagName(tagName), MESSAGE_CONSTRAINTS);
        this.tagName = tagName;
    }

    /**
     * Returns true if the name contains only printable ASCII characters and is not blank.
     * Spaces and punctuation are preserved; command parsers trim surrounding whitespace.
     */
    public static boolean isValidTagName(String test) {
        requireNonNull(test);

        boolean hasNonSpace = false;
        for (int i = 0; i < test.length(); i++) {
            char character = test.charAt(i);
            if (character < ' ' || character > '~') {
                return false;
            }
            hasNonSpace |= character != ' ';
        }
        return hasNonSpace;
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
