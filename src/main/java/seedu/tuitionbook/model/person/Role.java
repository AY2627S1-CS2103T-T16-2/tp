package seedu.tuitionbook.model.person;

import java.util.Locale;

import seedu.tuitionbook.commons.util.StringUtil;

/**
 * Represents a Person's role in TuitionBook.
 * A contact is either a student or a guardian.
 */
public enum Role {
    STUDENT, GUARDIAN;

    public static final String MESSAGE_CONSTRAINTS = "Role should be either 'student' or 'guardian'.";

    /**
     * Returns true if a given string is a valid role, ignoring ASCII case and surrounding whitespace.
     * Non-ASCII lookalikes such as {@code ſtudent} or {@code guardıan} are rejected, even though Unicode
     * uppercasing would map them to a role name.
     */
    public static boolean isValidRole(String test) {
        if (test == null || !StringUtil.isAscii(StringUtil.trimAsciiWhitespace(test))) {
            return false;
        }
        String normalised = StringUtil.trimAsciiWhitespace(test).toUpperCase(Locale.ROOT);
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
        return Role.valueOf(StringUtil.trimAsciiWhitespace(role).toUpperCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
