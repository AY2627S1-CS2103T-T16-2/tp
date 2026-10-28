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
    public void isValidAddress_combiningMarksOnly_returnsFalse() {
        assertFalse(Address.isValidAddress("\u0301")); // combining acute accent
        assertFalse(Address.isValidAddress("\u0301\u0302 \u20DD")); // non-spacing and enclosing marks
        assertFalse(Address.isValidAddress("\u0903")); // Devanagari spacing mark without a base letter
        assertEquals(Address.MESSAGE_BLANK, Address.getConstraintMessage("\u0301"));
    }

    @Test
    public void isValidAddress_embeddedControlCharacters_returnsFalse() {
        for (String address : new String[] {"Blk\u0000 1", "Blk 1\u0007", "Blk\u001B[31m 1", "Blk\u009B 1",
            "Blk\u007F 1"}) {
            assertFalse(Address.isValidAddress(address), address);
            assertEquals(Address.MESSAGE_INVALID_CHARACTERS, Address.getConstraintMessage(address));
        }
        assertTrue(Address.isValidAddress("Blk 12\tMain Street")); // tabs remain accepted
    }

    @Test
    public void isValidAddress_unpairedSurrogates_returnsFalse() {
        for (String address : new String[] {"Blk\uD800 1", "Blk\uDC00 1", "Blk 1\uD83D", "\uDE00Blk 1"}) {
            assertFalse(Address.isValidAddress(address));
            assertEquals(Address.MESSAGE_INVALID_CHARACTERS, Address.getConstraintMessage(address));
            assertThrows(IllegalArgumentException.class, Address.MESSAGE_INVALID_CHARACTERS, () ->
                    new Address(address));
        }
        assertFalse(Address.isValidAddress("\uD800"));
        assertTrue(Address.isValidAddress("Blk \uD83D\uDE00 1")); // a complete surrogate pair is accepted
    }

    @Test
    public void isValidAddress_unicodeSpacesBetweenVisibleText_returnsTrue() {
        for (String space : new String[] {"\u00a0", "\u2007", "\u202f", "\u2003", "\u3000", "\u200b", "\ufeff"}) {
            assertTrue(Address.isValidAddress("Blk" + space + "1"));
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
    public void getConstraintMessage_reportsFirstViolatedConstraint() {
        assertEquals(Address.MESSAGE_BLANK, Address.getConstraintMessage(""));
        assertEquals(Address.MESSAGE_BLANK, Address.getConstraintMessage("\u200B\u2003"));
        assertEquals(Address.MESSAGE_BLANK, Address.getConstraintMessage(" \n "));
        assertEquals(Address.MESSAGE_MULTIPLE_LINES, Address.getConstraintMessage("123 Main Street\nUnit 2"));
        assertEquals(Address.MESSAGE_MULTIPLE_LINES,
                Address.getConstraintMessage("A".repeat(Address.MAX_LENGTH) + "\nB"));
        assertEquals(Address.MESSAGE_TOO_LONG, Address.getConstraintMessage("A".repeat(Address.MAX_LENGTH + 1)));
    }

    @Test
    public void getConstraintMessage_validAddress_failsAssertion() {
        assertThrows(AssertionError.class, () -> Address.getConstraintMessage("123 Main Street"));
    }

    @Test
    public void constructor_invalidAddress_reportsSpecificMessage() {
        assertThrows(IllegalArgumentException.class, Address.MESSAGE_MULTIPLE_LINES, () -> new Address("A\nB"));
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
