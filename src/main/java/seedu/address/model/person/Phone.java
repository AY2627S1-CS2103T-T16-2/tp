package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's phone number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {

    public static final String MESSAGE_CONSTRAINTS =
            "Phone numbers must contain at least 3 digits, may start with '+', and may use a single space or hyphen "
                    + "between digits.";
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

        int currentIndex = test.startsWith("+") ? 1 : 0;
        int digitCount = 0;
        boolean isDigitExpected = true;

        while (currentIndex < test.length()) {
            char currentCharacter = test.charAt(currentIndex);
            if (currentCharacter >= '0' && currentCharacter <= '9') {
                digitCount++;
                isDigitExpected = false;
            } else if ((currentCharacter == ' ' || currentCharacter == '-') && !isDigitExpected) {
                isDigitExpected = true;
            } else {
                return false;
            }
            currentIndex++;
        }

        return digitCount >= MINIMUM_DIGIT_COUNT && !isDigitExpected;
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
