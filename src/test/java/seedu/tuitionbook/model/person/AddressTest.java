package seedu.tuitionbook.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class AddressTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Address(null));
    }

    @Test
    public void constructor_blankAddress_throwsIllegalArgumentException() {
        // A missing address is represented by the absence of an Address object, not a blank Address value.
        String invalidAddress = "";
        assertThrows(IllegalArgumentException.class, () -> new Address(invalidAddress));
    }

    @Test
    public void isValidAddress() {
        // null address
        assertThrows(NullPointerException.class, () -> Address.isValidAddress(null));

        // Blank values do not represent Address objects; absence is represented without an Address object.
        assertFalse(Address.isValidAddress("")); // empty string
        assertFalse(Address.isValidAddress(" ")); // spaces only

        // valid addresses
        assertTrue(Address.isValidAddress("Blk 456, Den Road, #01-355"));
        assertTrue(Address.isValidAddress("-")); // one character
        assertTrue(Address.isValidAddress("Leng Inc; 1234 Market St; San Francisco CA 2349879; USA")); // long address
    }

    @Test
    public void isValidAddress_lengthBoundariesCountUnicodeCodePoints() {
        String supplementaryCharacter = "\uD83D\uDE00";
        String addressWith199CodePoints = supplementaryCharacter.repeat(199);
        String addressWith200CodePoints = supplementaryCharacter.repeat(200);
        String addressWith201CodePoints = supplementaryCharacter.repeat(201);

        assertEquals(398, addressWith199CodePoints.length());
        assertEquals(199, addressWith199CodePoints.codePointCount(0, addressWith199CodePoints.length()));
        assertTrue(Address.isValidAddress(addressWith199CodePoints));
        assertTrue(Address.isValidAddress(addressWith200CodePoints));
        assertFalse(Address.isValidAddress(addressWith201CodePoints));
    }

    @Test
    public void equals() {
        Address address = new Address("Valid Address");

        // same values -> returns true
        assertTrue(address.equals(new Address("Valid Address")));

        // same object -> returns true
        assertTrue(address.equals(address));

        // null -> returns false
        assertFalse(address.equals(null));

        // different types -> returns false
        assertFalse(address.equals(5.0f));

        // different values -> returns false
        assertFalse(address.equals(new Address("Other Valid Address")));
    }
}
