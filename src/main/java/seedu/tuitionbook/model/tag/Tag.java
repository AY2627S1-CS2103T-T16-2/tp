package seedu.tuitionbook.model.tag;

import static java.util.Objects.requireNonNull;
import static seedu.tuitionbook.commons.util.AppUtil.checkArgument;

/**
 * Represents a Tag in TuitionBook.
 * Guarantees: immutable; name is valid as declared in {@link #isValidTagName(String)}
 */
public class Tag {

    public static final String MESSAGE_CONSTRAINTS = "Tag names should contain only ASCII letters and digits in "
            + "segments separated by single hyphens; hyphens cannot be leading, trailing, or consecutive";

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
     * Returns true if a given string is a valid tag name.
     */
    public static boolean isValidTagName(String test) {
        requireNonNull(test);

        boolean previousCharacterWasHyphen = true;
        for (int i = 0; i < test.length(); i++) {
            char character = test.charAt(i);
            if (isAsciiLetterOrDigit(character)) {
                previousCharacterWasHyphen = false;
            } else if (character == '-' && !previousCharacterWasHyphen) {
                previousCharacterWasHyphen = true;
            } else {
                return false;
            }
        }
        return !previousCharacterWasHyphen;
    }

    private static boolean isAsciiLetterOrDigit(char character) {
        return character >= 'A' && character <= 'Z'
                || character >= 'a' && character <= 'z'
                || character >= '0' && character <= '9';
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
