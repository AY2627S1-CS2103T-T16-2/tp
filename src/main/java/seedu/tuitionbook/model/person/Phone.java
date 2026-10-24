package seedu.tuitionbook.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.tuitionbook.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a Person's phone number in TuitionBook.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {

    public static final String MESSAGE_CONSTRAINTS =
            "Phone numbers must contain at least 3 main-number digits and may start with '+'. Write digit groups "
                    + "plain or in balanced parentheses, separated by one space or hyphen. An optional numeric "
                    + "extension may immediately follow the main number or follow one space, using x123, ext 123, "
                    + "or ext. 123 (case-insensitive).";
    private static final int MINIMUM_DIGIT_COUNT = 3;
    public final String value;

    /**
     * Constructs a {@code Phone}.
     *
     * @param phone A valid phone number.
     */
    public Phone(String phone) {
        requireNonNull(phone);
        checkArgument(isValidPhone(phone), MESSAGE_CONSTRAINTS);
        value = phone;
    }

    /**
     * Returns true if a given string is a valid phone number.
     */
    public static boolean isValidPhone(String test) {
        requireNonNull(test);

        int extensionMarkerIndex = findExtensionMarker(test);
        String mainNumber = test;
        if (extensionMarkerIndex >= 0) {
            String extension = test.substring(extensionMarkerIndex);
            if (!isValidExtension(extension)) {
                return false;
            }
            int mainNumberEnd = extensionMarkerIndex;
            if (mainNumberEnd > 0 && test.charAt(mainNumberEnd - 1) == ' ') {
                mainNumberEnd--;
            }
            mainNumber = test.substring(0, mainNumberEnd);
        }

        return isValidMainNumber(mainNumber);
    }

    private static boolean isValidMainNumber(String mainNumber) {
        int currentIndex = mainNumber.startsWith("+") ? 1 : 0;
        int digitCount = 0;

        while (currentIndex < mainNumber.length()) {
            if (mainNumber.charAt(currentIndex) == '(') {
                currentIndex++;
                int groupStart = currentIndex;
                while (currentIndex < mainNumber.length() && isAsciiDigit(mainNumber.charAt(currentIndex))) {
                    currentIndex++;
                    digitCount++;
                }
                if (currentIndex == groupStart || currentIndex >= mainNumber.length()
                        || mainNumber.charAt(currentIndex) != ')') {
                    return false;
                }
                currentIndex++;
            } else {
                int groupStart = currentIndex;
                while (currentIndex < mainNumber.length() && isAsciiDigit(mainNumber.charAt(currentIndex))) {
                    currentIndex++;
                    digitCount++;
                }
                if (currentIndex == groupStart) {
                    return false;
                }
            }

            if (currentIndex == mainNumber.length()) {
                break;
            }
            char separator = mainNumber.charAt(currentIndex);
            if (separator != ' ' && separator != '-') {
                return false;
            }
            currentIndex++;
            if (currentIndex == mainNumber.length()) {
                return false;
            }
        }

        return digitCount >= MINIMUM_DIGIT_COUNT;
    }

    private static int findExtensionMarker(String phone) {
        for (int i = 0; i < phone.length(); i++) {
            char currentCharacter = phone.charAt(i);
            if ((currentCharacter >= 'A' && currentCharacter <= 'Z')
                    || (currentCharacter >= 'a' && currentCharacter <= 'z')) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isValidExtension(String extension) {
        String lowercaseExtension = extension.toLowerCase(Locale.ROOT);
        String extensionDigits;
        if (lowercaseExtension.startsWith("x")) {
            extensionDigits = extension.substring(1);
        } else if (lowercaseExtension.startsWith("ext. ")) {
            extensionDigits = extension.substring(5);
        } else if (lowercaseExtension.startsWith("ext ")) {
            extensionDigits = extension.substring(4);
        } else {
            return false;
        }

        return !extensionDigits.isEmpty()
                && extensionDigits.chars().allMatch(character -> isAsciiDigit((char) character));
    }

    private static boolean isAsciiDigit(char character) {
        return character >= '0' && character <= '9';
    }

    /**
     * Returns an identity representation that ignores supported main-number formatting and uses {@code x} as the
     * extension marker while retaining the extension digits.
     */
    static String normalizeForIdentity(String phone) {
        int extensionMarkerIndex = findExtensionMarker(phone);
        int mainNumberEnd = extensionMarkerIndex >= 0 ? extensionMarkerIndex : phone.length();
        StringBuilder normalizedPhone = new StringBuilder(phone.length());
        for (int i = 0; i < mainNumberEnd; i++) {
            char currentCharacter = phone.charAt(i);
            if (currentCharacter == '+' || isAsciiDigit(currentCharacter)) {
                normalizedPhone.append(currentCharacter);
            }
        }
        if (extensionMarkerIndex >= 0) {
            String extension = phone.substring(extensionMarkerIndex);
            int extensionDigitsStart = extension.toLowerCase(Locale.ROOT).startsWith("x")
                    ? 1 : extension.lastIndexOf(' ') + 1;
            normalizedPhone.append('x').append(extension.substring(extensionDigitsStart));
        }
        return normalizedPhone.toString();
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
        if (!(other instanceof Phone otherPhone)) {
            return false;
        }

        return value.equals(otherPhone.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
