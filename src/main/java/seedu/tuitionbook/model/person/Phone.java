package seedu.tuitionbook.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.tuitionbook.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's phone number in TuitionBook.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {

    public static final String MESSAGE_CONSTRAINTS =
            "Phone numbers must contain at least 3 ASCII digits in the main number and may start with '+'. Main "
                    + "groups must be plain digits or digits in balanced parentheses, separated by one or more "
                    + "spaces, one hyphen, or one slash. After at least one space, you may add one parenthesized "
                    + "annotation containing ASCII letters, digits, and single internal spaces, with at least one "
                    + "letter. An optional numeric extension may follow the main number or annotation, directly "
                    + "or after spaces, using x123, ext 123, or ext. 123 (case-insensitive).";
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
        return parsePhone(test) != null;
    }

    private static ParsedPhone parsePhone(String phone) {
        return new PhoneParser(phone).parse();
    }

    private record ParsedPhone(boolean hasLeadingPlus, String mainDigits, String annotation,
                               String extensionDigits) {
        private String toIdentityString() {
            StringBuilder identity = new StringBuilder();
            if (hasLeadingPlus) {
                identity.append('+');
            }
            identity.append(mainDigits);
            if (extensionDigits != null) {
                identity.append('x').append(extensionDigits);
            }
            return identity.toString();
        }
    }

    /** Parses a phone number once into the components shared by validation and identity comparison. */
    private static final class PhoneParser {
        private final String phone;
        private final StringBuilder mainDigits = new StringBuilder();
        private int currentIndex;
        private boolean hasLeadingPlus;
        private String annotation;
        private String extensionDigits;

        private PhoneParser(String phone) {
            this.phone = phone;
        }

        private ParsedPhone parse() {
            hasLeadingPlus = consume('+');
            if (!parseNumberGroup()) {
                return null;
            }

            while (currentIndex < phone.length()) {
                if (parseExtension()) {
                    return buildParsedPhone();
                }

                char separator = phone.charAt(currentIndex);
                if (separator == ' ') {
                    consumeSpaces();
                    if (currentIndex == phone.length()) {
                        return null;
                    }
                    if (parseExtension()) {
                        return buildParsedPhone();
                    }
                    if (phone.charAt(currentIndex) == '(' && !isParenthesizedDigitGroup()) {
                        if (!parseAnnotation()) {
                            return null;
                        }
                        return parseAfterAnnotation();
                    }
                    if (!parseNumberGroup()) {
                        return null;
                    }
                } else if (separator == '-' || separator == '/') {
                    currentIndex++;
                    if (!parseNumberGroup()) {
                        return null;
                    }
                } else {
                    return null;
                }
            }

            return buildParsedPhone();
        }

        private ParsedPhone parseAfterAnnotation() {
            if (currentIndex == phone.length()) {
                return buildParsedPhone();
            }
            if (phone.charAt(currentIndex) == ' ') {
                consumeSpaces();
                if (currentIndex == phone.length()) {
                    return null;
                }
            }
            if (!parseExtension()) {
                return null;
            }
            return buildParsedPhone();
        }

        private boolean parseNumberGroup() {
            if (currentIndex == phone.length()) {
                return false;
            }
            if (phone.charAt(currentIndex) == '(') {
                if (!isParenthesizedDigitGroup()) {
                    return false;
                }
                currentIndex++;
                while (isCurrentCharacterAsciiDigit()) {
                    mainDigits.append(phone.charAt(currentIndex));
                    currentIndex++;
                }
                currentIndex++;
                return true;
            }
            if (!isCurrentCharacterAsciiDigit()) {
                return false;
            }
            while (isCurrentCharacterAsciiDigit()) {
                mainDigits.append(phone.charAt(currentIndex));
                currentIndex++;
            }
            return true;
        }

        private boolean isParenthesizedDigitGroup() {
            int digitIndex = currentIndex + 1;
            int firstDigitIndex = digitIndex;
            while (digitIndex < phone.length() && isAsciiDigit(phone.charAt(digitIndex))) {
                digitIndex++;
            }
            return digitIndex > firstDigitIndex && digitIndex < phone.length() && phone.charAt(digitIndex) == ')';
        }

        private boolean parseAnnotation() {
            int annotationStart = currentIndex + 1;
            int annotationIndex = annotationStart;
            boolean hasLetter = false;
            boolean previousCharacterWasSpace = false;
            while (annotationIndex < phone.length() && phone.charAt(annotationIndex) != ')') {
                char character = phone.charAt(annotationIndex);
                if (isAsciiLetter(character)) {
                    hasLetter = true;
                    previousCharacterWasSpace = false;
                } else if (isAsciiDigit(character)) {
                    previousCharacterWasSpace = false;
                } else if (character == ' ' && annotationIndex > annotationStart
                        && !previousCharacterWasSpace) {
                    previousCharacterWasSpace = true;
                } else {
                    return false;
                }
                annotationIndex++;
            }
            if (annotationIndex == annotationStart || annotationIndex == phone.length()
                    || previousCharacterWasSpace || !hasLetter) {
                return false;
            }
            annotation = phone.substring(annotationStart, annotationIndex);
            currentIndex = annotationIndex + 1;
            return true;
        }

        private boolean parseExtension() {
            int extensionStart;
            if (startsWithIgnoreCase("x")) {
                extensionStart = currentIndex + 1;
            } else if (startsWithIgnoreCase("ext. ")) {
                extensionStart = currentIndex + 5;
            } else if (startsWithIgnoreCase("ext ")) {
                extensionStart = currentIndex + 4;
            } else {
                return false;
            }

            int extensionEnd = extensionStart;
            while (extensionEnd < phone.length() && isAsciiDigit(phone.charAt(extensionEnd))) {
                extensionEnd++;
            }
            if (extensionEnd == extensionStart || extensionEnd != phone.length()) {
                return false;
            }
            extensionDigits = phone.substring(extensionStart);
            currentIndex = phone.length();
            return true;
        }

        private ParsedPhone buildParsedPhone() {
            if (mainDigits.length() < MINIMUM_DIGIT_COUNT) {
                return null;
            }
            return new ParsedPhone(hasLeadingPlus, mainDigits.toString(), annotation, extensionDigits);
        }

        private boolean consume(char expectedCharacter) {
            if (currentIndex < phone.length() && phone.charAt(currentIndex) == expectedCharacter) {
                currentIndex++;
                return true;
            }
            return false;
        }

        private void consumeSpaces() {
            while (currentIndex < phone.length() && phone.charAt(currentIndex) == ' ') {
                currentIndex++;
            }
        }

        private boolean startsWithIgnoreCase(String expected) {
            return phone.regionMatches(true, currentIndex, expected, 0, expected.length());
        }

        private boolean isCurrentCharacterAsciiDigit() {
            return currentIndex < phone.length() && isAsciiDigit(phone.charAt(currentIndex));
        }
    }

    private static boolean isAsciiLetter(char character) {
        return (character >= 'A' && character <= 'Z')
                || (character >= 'a' && character <= 'z');
    }

    private static boolean isAsciiDigit(char character) {
        return character >= '0' && character <= '9';
    }

    /**
     * Returns an identity representation that removes spaces, hyphens, slashes, numeric-group parentheses, and the
     * annotation; preserves a leading {@code +}; normalizes the extension marker to {@code x}; and retains the
     * extension digits.
     */
    static String normalizeForIdentity(String phone) {
        requireNonNull(phone);
        ParsedPhone parsedPhone = requireNonNull(parsePhone(phone));
        return parsedPhone.toIdentityString();
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
