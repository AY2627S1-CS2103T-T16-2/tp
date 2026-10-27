package seedu.tuitionbook.model.person;

import static java.util.Objects.requireNonNull;

/**
 * Represents a Person's name in TuitionBook.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final int MAX_LENGTH = 100;

    public static final String MESSAGE_BLANK = "Names must not be blank.";
    public static final String MESSAGE_TOO_LONG = "Names must be at most " + MAX_LENGTH + " characters.";
    public static final String MESSAGE_INVALID_START = "Names must start with a letter or number.";
    public static final String MESSAGE_INVALID_CHARACTERS = "Names may only contain letters, numbers, accent marks, "
            + "spaces, periods (.), apostrophes ('), forward slashes (/), and hyphens (-).";

    /*
     * The first character must be a Unicode letter or number. Subsequent characters may also be
     * Unicode combining marks, ASCII spaces (U+0020), periods, straight or right curly apostrophes, forward slashes,
     * or hyphens.
     */
    public static final String VALIDATION_REGEX = "[\\p{L}\\p{N}][\\p{L}\\p{N}\\p{M} .'\u2019/-]*";
    private static final String VALID_START_REGEX = "(?s)[\\p{L}\\p{N}].*";

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        if (!isValidName(name)) {
            throw new IllegalArgumentException(getConstraintMessage(name));
        }
        fullName = name;
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        return test.matches(VALIDATION_REGEX) && test.codePointCount(0, test.length()) <= MAX_LENGTH;
    }

    /**
     * Returns the message describing the first constraint that the given invalid name violates.
     */
    public static String getConstraintMessage(String invalidName) {
        requireNonNull(invalidName);
        assert !isValidName(invalidName);
        if (invalidName.isBlank()) {
            return MESSAGE_BLANK;
        }
        if (invalidName.codePointCount(0, invalidName.length()) > MAX_LENGTH) {
            return MESSAGE_TOO_LONG;
        }
        if (!invalidName.matches(VALID_START_REGEX)) {
            return MESSAGE_INVALID_START;
        }
        return MESSAGE_INVALID_CHARACTERS;
    }


    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
