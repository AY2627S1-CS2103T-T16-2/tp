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
        assertFalse(Address.isValidAddress("\u2003")); // Unicode EM SPACE only
        assertFalse(Address.isValidAddress("\u00a0\u2003\t")); // Unicode whitespace only
        assertFalse(Address.isValidAddress("123 Main Street\nUnit 2")); // embedded line break
        assertFalse(Address.isValidAddress("123 Main Street\u000BUnit 2")); // vertical tab
        assertFalse(Address.isValidAddress("123 Main Street\fUnit 2")); // form feed
        assertFalse(Address.isValidAddress("123 Main Street\r")); // trailing line break
        assertFalse(Address.isValidAddress("123 Main Street\u0085Unit 2")); // Unicode next-line character
        assertFalse(Address.isValidAddress("123 Main Street\u2028Unit 2")); // Unicode line separator
        assertFalse(Address.isValidAddress("123 Main Street\u2029Unit 2")); // Unicode paragraph separator

        // valid addresses
        assertTrue(Address.isValidAddress("Blk 456, Den Road, #01-355"));
        assertTrue(Address.isValidAddress("-")); // one character
        assertTrue(Address.isValidAddress("Leng Inc; 1234 Market St; San Francisco CA 2349879; USA")); // long address
    }

    @Test
    public void isValidAddress_formatOrControlCharactersOnly_returnsFalse() {
        String[] formatOnlyAddresses = {
            "\u200B", // zero-width space
            "\uFEFF", // byte-order mark
            "\u2060", // word joiner
            "\u200E\u200F", // left-to-right and right-to-left marks
            "\u202A\u202E\u2066", // bidi embedding, override, and isolate marks
            "\u200B\uFEFF\u2060\u200E"
        };
        for (String address : formatOnlyAddresses) {
            assertFalse(Address.isValidAddress(address));
        }

        String[] controlOnlyAddresses = {"\u0000", "\u0001\u001F", "\u007F", "\u009F"};
        for (String address : controlOnlyAddresses) {
            assertFalse(Address.isValidAddress(address));
        }
    }

    @Test
    public void isValidAddress_visibleUnicodeWithCombiningOrFormatCharacters_returnsTrue() {
        assertTrue(Address.isValidAddress("李小龙街 12号"));
        assertTrue(Address.isValidAddress("Cafe\u0301 Road"));
        assertTrue(Address.isValidAddress("123\u200B Main Street"));
        assertTrue(Address.isValidAddress("\u202AJosé Street\u202C"));
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
