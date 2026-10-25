package seedu.tuitionbook.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.tuitionbook.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's email in TuitionBook.
 * Guarantees: immutable; is valid as declared in {@link #isValidEmail(String)}
 */
public class Email {

    public static final String MESSAGE_CONSTRAINTS = "Emails must use local-part@domain. The local part may contain "
            + "ASCII letters, digits, periods, plus signs, underscores, hyphens, and apostrophes; it must start and "
            + "end with a letter or digit, and periods cannot be consecutive. Domain labels use ASCII letters, "
            + "digits, or internal hyphens, and the final label must contain at least 2 characters.";
    private static final String LOCAL_PART_SPECIAL_CHARACTERS = ".+_-'";

    public final String value;

    /**
     * Constructs an {@code Email}.
     *
     * @param email A valid email address.
     */
    public Email(String email) {
        requireNonNull(email);
        checkArgument(isValidEmail(email), MESSAGE_CONSTRAINTS);
        value = email;
    }

    /**
     * Returns true if a given string is a valid email.
     */
    public static boolean isValidEmail(String test) {
        requireNonNull(test);
        int atSignIndex = test.indexOf('@');
        if (atSignIndex <= 0 || atSignIndex != test.lastIndexOf('@') || atSignIndex == test.length() - 1) {
            return false;
        }

        String localPart = test.substring(0, atSignIndex);
        String domain = test.substring(atSignIndex + 1);
        return isValidLocalPart(localPart) && isValidDomain(domain);
    }

    private static boolean isValidLocalPart(String localPart) {
        if (!isAsciiAlphanumeric(localPart.charAt(0))
                || !isAsciiAlphanumeric(localPart.charAt(localPart.length() - 1))) {
            return false;
        }
        boolean previousCharacterWasPeriod = false;
        for (char currentCharacter : localPart.toCharArray()) {
            if (!isAsciiAlphanumeric(currentCharacter)
                    && LOCAL_PART_SPECIAL_CHARACTERS.indexOf(currentCharacter) < 0) {
                return false;
            }
            if (currentCharacter == '.' && previousCharacterWasPeriod) {
                return false;
            }
            previousCharacterWasPeriod = currentCharacter == '.';
        }
        return true;
    }

    private static boolean isValidDomain(String domain) {
        String[] labels = domain.split("\\.", -1);
        if (labels[labels.length - 1].length() < 2) {
            return false;
        }
        for (String label : labels) {
            if (label.isEmpty() || !isAsciiAlphanumeric(label.charAt(0))
                    || !isAsciiAlphanumeric(label.charAt(label.length() - 1))) {
                return false;
            }
            for (int i = 0; i < label.length(); i++) {
                char currentCharacter = label.charAt(i);
                if (!isAsciiAlphanumeric(currentCharacter) && currentCharacter != '-') {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isAsciiAlphanumeric(char character) {
        return (character >= 'A' && character <= 'Z')
                || (character >= 'a' && character <= 'z')
                || (character >= '0' && character <= '9');
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
        if (!(other instanceof Email otherEmail)) {
            return false;
        }

        return value.equals(otherEmail.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
