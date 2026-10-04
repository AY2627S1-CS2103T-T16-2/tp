package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PhoneTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Phone(null));
    }

    @Test
    public void constructor_invalidPhone_throwsIllegalArgumentException() {
        String invalidPhone = "";
        assertThrows(IllegalArgumentException.class, () -> new Phone(invalidPhone));
    }

    @Test
    public void constructor_validPhone_preservesFormatting() {
        String validPhone = "+65 9123-4567";
        assertEquals(validPhone, new Phone(validPhone).value);
    }

    @Test
    public void isValidPhone() {
        // null phone number
        assertThrows(NullPointerException.class, () -> Phone.isValidPhone(null));

        // invalid phone numbers
        assertFalse(Phone.isValidPhone("")); // empty string
        assertFalse(Phone.isValidPhone(" ")); // spaces only
        assertFalse(Phone.isValidPhone("12")); // less than 3 digits
        assertFalse(Phone.isValidPhone("1-2")); // formatting does not count toward the minimum
        assertFalse(Phone.isValidPhone("phone")); // non-numeric
        assertFalse(Phone.isValidPhone("9011p041")); // alphabets within digits
        assertFalse(Phone.isValidPhone("++6591234567")); // repeated leading plus signs
        assertFalse(Phone.isValidPhone("+ 65 91234567")); // separator immediately after plus sign
        assertFalse(Phone.isValidPhone("-91234567")); // leading hyphen
        assertFalse(Phone.isValidPhone("91234567-")); // trailing hyphen
        assertFalse(Phone.isValidPhone("9123--4567")); // repeated separators
        assertFalse(Phone.isValidPhone("9123  4567")); // repeated spaces

        // valid phone numbers
        assertTrue(Phone.isValidPhone("911")); // exactly 3 numbers
        assertTrue(Phone.isValidPhone("93121534"));
        assertTrue(Phone.isValidPhone("+651234"));
        assertTrue(Phone.isValidPhone("9123 4567"));
        assertTrue(Phone.isValidPhone("9123-4567"));
        assertTrue(Phone.isValidPhone("+65 9123-4567"));
        assertTrue(Phone.isValidPhone("124293842033123")); // long phone numbers
    }

    @Test
    public void equals() {
        Phone phone = new Phone("999");

        // same values -> returns true
        assertTrue(phone.equals(new Phone("999")));

        // same object -> returns true
        assertTrue(phone.equals(phone));

        // null -> returns false
        assertFalse(phone.equals(null));

        // different types -> returns false
        assertFalse(phone.equals(5.0f));

        // different values -> returns false
        assertFalse(phone.equals(new Phone("995")));
    }
}
