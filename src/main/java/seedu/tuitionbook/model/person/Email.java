package seedu.tuitionbook.model.person;

import static java.util.Objects.requireNonNull;

/**
 * Represents a Person's email in TuitionBook.
 * Guarantees: immutable; is valid as declared in {@link #isValidEmail(String)}
 */
public class Email {

    public static final String MESSAGE_FORMAT = "Emails must be in the format name@example.com.";
    public static final String MESSAGE_INVALID_NAME_CHARACTERS = "The part of an email before the @ may only "
            + "contain English letters, digits (0-9), periods (.), plus signs (+), underscores (_), hyphens (-), "
            + "and apostrophes (').";
    public static final String MESSAGE_INVALID_NAME_EDGE =
            "The part of an email before the @ must start and end with a letter or digit.";
    public static final String MESSAGE_CONSECUTIVE_PERIODS =
            "The part of an email before the @ cannot have two periods in a row.";
    public static final String MESSAGE_INVALID_DOMAIN = "The part of an email after the @ must look like example.com, "
            + "using only English letters, digits (0-9), hyphens (-) and periods (.).";
    private static final String LOCAL_PART_SPECIAL_CHARACTERS = ".+_-'";

    public final String value;

    /**
     * Constructs an {@code Email}.
     *
     * @param email A valid email address.
     */
    public Email(String email) {
        requireNonNull(email);
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException(getConstraintMessage(email));
        }
        value = email;
    }

    /**
     * Returns true if a given string is a valid email.
     */
    public static boolean isValidEmail(String test) {
        requireNonNull(test);
        if (!hasSingleAtSignBetweenParts(test)) {
            return false;
        }

        int atSignIndex = test.indexOf('@');
        String localPart = test.substring(0, atSignIndex);
        String domain = test.substring(atSignIndex + 1);
        return isValidLocalPart(localPart) && isValidDomain(domain);
    }

    /**
     * Returns the message describing the first constraint that the given invalid email violates.
     */
    public static String getConstraintMessage(String invalidEmail) {
        requireNonNull(invalidEmail);
        assert !isValidEmail(invalidEmail);
        if (!hasSingleAtSignBetweenParts(invalidEmail)) {
            return MESSAGE_FORMAT;
        }
        String localPart = invalidEmail.substring(0, invalidEmail.indexOf('@'));
        if (!hasOnlyLocalPartCharacters(localPart)) {
            return MESSAGE_INVALID_NAME_CHARACTERS;
        }
        if (!hasAlphanumericEdges(localPart)) {
            return MESSAGE_INVALID_NAME_EDGE;
        }
        if (localPart.contains("..")) {
            return MESSAGE_CONSECUTIVE_PERIODS;
        }
        return MESSAGE_INVALID_DOMAIN;
    }

    private static boolean hasSingleAtSignBetweenParts(String test) {
        int atSignIndex = test.indexOf('@');
        return atSignIndex > 0 && atSignIndex == test.lastIndexOf('@') && atSignIndex < test.length() - 1;
    }

    private static boolean isValidLocalPart(String localPart) {
        return hasOnlyLocalPartCharacters(localPart) && hasAlphanumericEdges(localPart) && !localPart.contains("..");
    }

    private static boolean hasOnlyLocalPartCharacters(String localPart) {
        return localPart.chars().allMatch(character -> isAsciiAlphanumeric((char) character)
                || LOCAL_PART_SPECIAL_CHARACTERS.indexOf(character) >= 0);
    }

    private static boolean hasAlphanumericEdges(String text) {
        return isAsciiAlphanumeric(text.charAt(0)) && isAsciiAlphanumeric(text.charAt(text.length() - 1));
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
