package seedu.tuitionbook.model.person;

import java.util.Locale;

/**
 * Represents a Person's role in TuitionBook.
 * A contact is either a student or a guardian.
 */
public enum Role {
    STUDENT, GUARDIAN;

    public static final String MESSAGE_CONSTRAINTS = "Role should be either 'student' or 'guardian'.";

    /**
     * Returns true if a given string is a valid role, ignoring case and surrounding whitespace.
     */
    public static boolean isValidRole(String test) {
        if (test == null) {
            return false;
        }
        String normalised = test.trim().toUpperCase(Locale.ROOT);
        return normalised.equals(STUDENT.name()) || normalised.equals(GUARDIAN.name());
    }

    /**
     * Parses the given string into a {@code Role}, ignoring case and surrounding whitespace.
     *
     * @throws IllegalArgumentException if the string is not a valid role.
     */
    public static Role fromString(String role) {
        if (!isValidRole(role)) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        return Role.valueOf(role.trim().toUpperCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
