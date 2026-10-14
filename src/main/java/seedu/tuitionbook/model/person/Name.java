package seedu.tuitionbook.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.tuitionbook.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's name in TuitionBook.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final int MAX_LENGTH = 100;

    public static final String MESSAGE_CONSTRAINTS =
            "Names must not be blank and must be at most " + MAX_LENGTH
                    + " characters, counted as Unicode code points. They must start with a Unicode letter or number, "
                    + "and may only contain Unicode letters and numbers, Unicode combining marks, spaces, periods, "
                    + "straight apostrophes, forward slashes, and hyphens.";

    /*
     * The first character must be a Unicode letter or number. Subsequent characters may also be
     * Unicode combining marks, spaces, periods, straight apostrophes, forward slashes, or hyphens.
     */
    public static final String VALIDATION_REGEX = "[\\p{L}\\p{N}][\\p{L}\\p{N}\\p{M} .'/-]*";

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = name;
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        return test.matches(VALIDATION_REGEX) && test.codePointCount(0, test.length()) <= MAX_LENGTH;
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
