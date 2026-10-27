package seedu.tuitionbook.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void constructor_validName_preservesFormatting() {
        String validName = "Dr. José O'Connor-Lee";
        assertEquals(validName, new Name(validName).fullName);

        String decomposedName = "Jose\u0301";
        assertEquals(decomposedName, new Name(decomposedName).fullName);
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("^")); // only non-alphanumeric characters
        assertFalse(Name.isValidName("peter*")); // contains non-alphanumeric characters
        assertFalse(Name.isValidName("James&")); // contains an unsupported symbol
        assertFalse(Name.isValidName("John@Doe")); // contains an unsupported at sign
        assertFalse(Name.isValidName("John#Doe")); // contains an unsupported hash sign
        assertFalse(Name.isValidName(" Amy Bee")); // starts with whitespace
        assertFalse(Name.isValidName("\u0301Jose")); // starts with a combining mark
        assertFalse(Name.isValidName("/John")); // starts with a forward slash
        assertFalse(Name.isValidName("Amy\u00A0Bee")); // non-breaking space is not an ASCII space
        assertFalse(Name.isValidName("Amy\u2003Bee")); // EM SPACE is not an ASCII space
        assertFalse(Name.isValidName("Amy\u2009Bee")); // thin space is not an ASCII space
        assertFalse(Name.isValidName("Amy\u3000Bee")); // ideographic space is not an ASCII space

        // valid name
        assertTrue(Name.isValidName("Amy Bee"));
        assertTrue(Name.isValidName("Muhammad s/o Ali")); // contains a forward-slash name particle
        assertTrue(Name.isValidName("Mary-Jane Tan"));
        assertTrue(Name.isValidName("Anne-Marie Tan"));
        assertTrue(Name.isValidName("O'Connor"));
        assertTrue(Name.isValidName("O\u2019Connor"));
        assertTrue(Name.isValidName("Dr. Lee"));
        assertTrue(Name.isValidName("José Tan"));
        assertTrue(Name.isValidName("Jose\u0301"));
        assertTrue(Name.isValidName("李小龙"));
        assertTrue(Name.isValidName("அருண்"));
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("12345")); // numbers only
        assertTrue(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr 2nd")); // long names
    }

    @Test
    public void getConstraintMessage_reportsFirstViolatedConstraint() {
        assertEquals(Name.MESSAGE_BLANK, Name.getConstraintMessage(""));
        assertEquals(Name.MESSAGE_BLANK, Name.getConstraintMessage("   "));
        assertEquals(Name.MESSAGE_TOO_LONG, Name.getConstraintMessage("A".repeat(Name.MAX_LENGTH + 1)));
        assertEquals(Name.MESSAGE_TOO_LONG, Name.getConstraintMessage("@" + "A".repeat(Name.MAX_LENGTH)));
        assertEquals(Name.MESSAGE_INVALID_START, Name.getConstraintMessage("-Amy"));
        assertEquals(Name.MESSAGE_INVALID_START, Name.getConstraintMessage(" Amy"));
        assertEquals(Name.MESSAGE_INVALID_START, Name.getConstraintMessage("\u0301Amy"));
        assertEquals(Name.MESSAGE_INVALID_CHARACTERS, Name.getConstraintMessage("R@chel"));
        assertEquals(Name.MESSAGE_INVALID_CHARACTERS, Name.getConstraintMessage("Amy\u2003Bee"));
    }

    @Test
    public void getConstraintMessage_validName_failsAssertion() {
        assertThrows(AssertionError.class, () -> Name.getConstraintMessage("Amy Bee"));
    }

    @Test
    public void constructor_invalidName_reportsSpecificMessage() {
        assertThrows(IllegalArgumentException.class, Name.MESSAGE_INVALID_START, () -> new Name("-Amy"));
    }

    @Test
    public void isValidName_lengthBoundariesCountUnicodeCodePoints() {
        String supplementaryLetter = "\uD801\uDC00";
        String nameWith99CodePoints = supplementaryLetter.repeat(99);
        String nameWith100CodePoints = supplementaryLetter.repeat(100);
        String nameWith101CodePoints = supplementaryLetter.repeat(101);

        assertEquals(198, nameWith99CodePoints.length());
        assertEquals(99, nameWith99CodePoints.codePointCount(0, nameWith99CodePoints.length()));
        assertTrue(Name.isValidName(nameWith99CodePoints));
        assertTrue(Name.isValidName(nameWith100CodePoints));
        assertFalse(Name.isValidName(nameWith101CodePoints));
    }

    @Test
    public void constructor_slashContainingNameAtLengthBoundary_enforcesMaximum() {
        String nameWith100CodePoints = "A/" + "B".repeat(Name.MAX_LENGTH - 2);
        String nameWith101CodePoints = nameWith100CodePoints + "B";

        assertEquals(Name.MAX_LENGTH,
                nameWith100CodePoints.codePointCount(0, nameWith100CodePoints.length()));
        assertEquals(nameWith100CodePoints, new Name(nameWith100CodePoints).fullName);
        assertEquals(Name.MAX_LENGTH + 1,
                nameWith101CodePoints.codePointCount(0, nameWith101CodePoints.length()));
        assertThrows(IllegalArgumentException.class, () -> new Name(nameWith101CodePoints));
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }
}
