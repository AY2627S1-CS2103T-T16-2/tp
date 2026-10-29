package seedu.tuitionbook.model.person;

import static java.util.Objects.requireNonNull;

/**
 * Represents a Person's address in TuitionBook.
 * Guarantees: immutable; is valid as declared in {@link #isValidAddress(String)}
 */
public class Address {

    public static final int MAX_LENGTH = 200;

    public static final String MESSAGE_BLANK = "Addresses must contain at least one visible character.";
    public static final String MESSAGE_MULTIPLE_LINES = "Addresses must be on a single line.";
    public static final String MESSAGE_INVALID_CHARACTERS = "Addresses cannot contain control characters "
            + "(other than tabs) or broken Unicode characters, which can come from copied text. "
            + "Retype the address.";
    public static final String MESSAGE_TOO_LONG = "Addresses must be at most " + MAX_LENGTH + " characters.";

    public final String value;

    /**
     * Constructs an {@code Address}.
     *
     * @param address A valid address.
     */
    public Address(String address) {
        requireNonNull(address);
        if (!isValidAddress(address)) {
            throw new IllegalArgumentException(getConstraintMessage(address));
        }
        value = address;
    }

    /**
     * Returns true if a given string is a valid address.
     */
    public static boolean isValidAddress(String test) {
        requireNonNull(test);
        return test.codePointCount(0, test.length()) <= MAX_LENGTH
                && !hasLineBreak(test) && !hasInvalidCharacter(test) && hasMeaningfulVisibleCharacter(test);
    }

    /**
     * Returns the message describing the first constraint that the given invalid address violates.
     */
    public static String getConstraintMessage(String invalidAddress) {
        requireNonNull(invalidAddress);
        assert !isValidAddress(invalidAddress);
        if (!hasMeaningfulVisibleCharacter(invalidAddress)) {
            return MESSAGE_BLANK;
        }
        if (hasLineBreak(invalidAddress)) {
            return MESSAGE_MULTIPLE_LINES;
        }
        if (hasInvalidCharacter(invalidAddress)) {
            return MESSAGE_INVALID_CHARACTERS;
        }
        return MESSAGE_TOO_LONG;
    }

    private static boolean hasLineBreak(String test) {
        return test.codePoints().anyMatch(Address::isLineBreak);
    }

    /** Detects non-tab control characters and unpaired surrogates, which cannot be stored faithfully as UTF-8. */
    private static boolean hasInvalidCharacter(String test) {
        return test.codePoints().anyMatch(codePoint -> codePoint != '\t'
                && (Character.getType(codePoint) == Character.CONTROL
                || Character.getType(codePoint) == Character.SURROGATE));
    }

    private static boolean hasMeaningfulVisibleCharacter(String test) {
        return test.codePoints().anyMatch(Address::isMeaningfulVisibleCodePoint);
    }

    private static boolean isMeaningfulVisibleCodePoint(int codePoint) {
        int unicodeCategory = Character.getType(codePoint);
        // Combining marks only render when attached to a base character, so they cannot be an address on their own.
        return !Character.isWhitespace(codePoint)
                && !Character.isSpaceChar(codePoint)
                && unicodeCategory != Character.CONTROL
                && unicodeCategory != Character.FORMAT
                && unicodeCategory != Character.SURROGATE
                && unicodeCategory != Character.NON_SPACING_MARK
                && unicodeCategory != Character.ENCLOSING_MARK
                && unicodeCategory != Character.COMBINING_SPACING_MARK;
    }

    private static boolean isLineBreak(int codePoint) {
        return codePoint == '\n' || codePoint == '\u000B' || codePoint == '\f' || codePoint == '\r'
                || codePoint == '\u0085' || codePoint == '\u2028' || codePoint == '\u2029';
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Address otherAddress)) {
            return false;
        }

        return value.equals(otherAddress.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
