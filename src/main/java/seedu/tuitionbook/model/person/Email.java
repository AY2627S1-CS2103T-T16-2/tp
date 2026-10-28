package seedu.tuitionbook.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

/**
 * Represents a Person's email in TuitionBook.
 * Guarantees: immutable; is valid as declared in {@link #isValidEmail(String)}
 */
public class Email {

    public static final String MESSAGE_TOO_LONG = "Emails must be at most 254 characters long.";
    public static final String MESSAGE_NAME_TOO_LONG =
            "The part of an email before the @ must be at most 64 characters long.";
    public static final String MESSAGE_DOMAIN_PART_TOO_LONG =
            "Each part of an email's domain between periods must be at most 63 characters long.";
    public static final String MESSAGE_FORMAT = "Emails must be in the format name@example.com.";
    public static final String MESSAGE_INVALID_NAME_CHARACTERS = "The part of an email before the @ may only "
            + "contain English letters, digits (0-9), periods (.), plus signs (+), underscores (_), hyphens (-), "
            + "and apostrophes (').";
    public static final String MESSAGE_INVALID_NAME_EDGE =
            "The part of an email before the @ must start and end with a letter or digit.";
    public static final String MESSAGE_CONSECUTIVE_PERIODS =
            "The part of an email before the @ cannot have two periods in a row.";
    public static final String MESSAGE_INVALID_DOMAIN_CHARACTERS = "The part of an email after the @ may only "
            + "contain English letters, digits (0-9), hyphens (-) and periods (.).";
    public static final String MESSAGE_EMPTY_DOMAIN_PART = "The part of an email after the @ cannot start or end "
            + "with a period, or have two periods in a row.";
    public static final String MESSAGE_DOMAIN_PART_EDGE = "The part of an email after the @ cannot have a hyphen (-) "
            + "at the start or end, or next to a period.";
    public static final String MESSAGE_SHORT_DOMAIN_ENDING =
            "The last part of an email's domain must be at least 2 characters long, as in .com or .sg.";
    private static final String LOCAL_PART_SPECIAL_CHARACTERS = ".+_-'";
    // Accepted characters are ASCII, so character counts equal byte counts for these SMTP/DNS limits.
    private static final int MAX_LENGTH = 254;
    private static final int MAX_LOCAL_PART_LENGTH = 64;
    private static final int MAX_DOMAIN_LABEL_LENGTH = 63;

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
        if (test.length() > MAX_LENGTH || !hasSingleAtSignBetweenParts(test)) {
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
        if (invalidEmail.length() > MAX_LENGTH) {
            return MESSAGE_TOO_LONG;
        }
        if (!hasSingleAtSignBetweenParts(invalidEmail)) {
            return MESSAGE_FORMAT;
        }
        String localPart = invalidEmail.substring(0, invalidEmail.indexOf('@'));
        if (localPart.length() > MAX_LOCAL_PART_LENGTH) {
            return MESSAGE_NAME_TOO_LONG;
        }
        if (!hasOnlyLocalPartCharacters(localPart)) {
            return MESSAGE_INVALID_NAME_CHARACTERS;
        }
        if (!hasAlphanumericEdges(localPart)) {
            return MESSAGE_INVALID_NAME_EDGE;
        }
        if (localPart.contains("..")) {
            return MESSAGE_CONSECUTIVE_PERIODS;
        }
        String domain = invalidEmail.substring(invalidEmail.indexOf('@') + 1);
        if (!hasOnlyDomainCharacters(domain)) {
            return MESSAGE_INVALID_DOMAIN_CHARACTERS;
        }
        String[] labels = splitDomainLabels(domain);
        if (!hasNoEmptyLabels(labels)) {
            return MESSAGE_EMPTY_DOMAIN_PART;
        }
        if (!hasAlphanumericLabelEdges(labels)) {
            return MESSAGE_DOMAIN_PART_EDGE;
        }
        if (!hasValidLabelLengths(labels)) {
            return MESSAGE_DOMAIN_PART_TOO_LONG;
        }
        return MESSAGE_SHORT_DOMAIN_ENDING;
    }

    private static boolean hasSingleAtSignBetweenParts(String test) {
        int atSignIndex = test.indexOf('@');
        return atSignIndex > 0 && atSignIndex == test.lastIndexOf('@') && atSignIndex < test.length() - 1;
    }

    private static boolean isValidLocalPart(String localPart) {
        return localPart.length() <= MAX_LOCAL_PART_LENGTH && hasOnlyLocalPartCharacters(localPart)
                && hasAlphanumericEdges(localPart) && !localPart.contains("..");
    }

    private static boolean hasOnlyLocalPartCharacters(String localPart) {
        return localPart.chars().allMatch(character -> isAsciiAlphanumeric((char) character)
                || LOCAL_PART_SPECIAL_CHARACTERS.indexOf(character) >= 0);
    }

    private static boolean hasAlphanumericEdges(String text) {
        return isAsciiAlphanumeric(text.charAt(0)) && isAsciiAlphanumeric(text.charAt(text.length() - 1));
    }

    private static boolean isValidDomain(String domain) {
        if (!hasOnlyDomainCharacters(domain)) {
            return false;
        }
        String[] labels = splitDomainLabels(domain);
        return hasNoEmptyLabels(labels) && hasAlphanumericLabelEdges(labels) && hasValidLabelLengths(labels)
                && labels[labels.length - 1].length() >= 2;
    }

    private static boolean hasOnlyDomainCharacters(String domain) {
        return domain.chars().allMatch(character -> isAsciiAlphanumeric((char) character)
                || character == '-' || character == '.');
    }

    private static String[] splitDomainLabels(String domain) {
        return domain.split("\\.", -1);
    }

    private static boolean hasNoEmptyLabels(String[] labels) {
        return Arrays.stream(labels).noneMatch(String::isEmpty);
    }

    private static boolean hasValidLabelLengths(String[] labels) {
        return Arrays.stream(labels).allMatch(label -> label.length() <= MAX_DOMAIN_LABEL_LENGTH);
    }

    private static boolean hasAlphanumericLabelEdges(String[] labels) {
        return Arrays.stream(labels).allMatch(Email::hasAlphanumericEdges);
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
