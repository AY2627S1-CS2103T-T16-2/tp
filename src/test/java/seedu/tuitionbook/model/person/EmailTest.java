package seedu.tuitionbook.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class EmailTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Email(null));
    }

    @Test
    public void constructor_blankEmail_throwsIllegalArgumentException() {
        // A missing email is represented by the absence of an Email object, not a blank Email value.
        String invalidEmail = "";
        assertThrows(IllegalArgumentException.class, () -> new Email(invalidEmail));
    }

    @Test
    public void getConstraintMessage_reportsFirstViolatedConstraint() {
        assertEquals(Email.MESSAGE_FORMAT, Email.getConstraintMessage(""));
        assertEquals(Email.MESSAGE_FORMAT, Email.getConstraintMessage("example.com"));
        assertEquals(Email.MESSAGE_FORMAT, Email.getConstraintMessage("@example.com"));
        assertEquals(Email.MESSAGE_FORMAT, Email.getConstraintMessage("peter@"));
        assertEquals(Email.MESSAGE_FORMAT, Email.getConstraintMessage("a@b@example.com"));
        assertEquals(Email.MESSAGE_INVALID_NAME_CHARACTERS, Email.getConstraintMessage("peter jack@example.com"));
        assertEquals(Email.MESSAGE_INVALID_NAME_CHARACTERS, Email.getConstraintMessage("-peter!@example.com"));
        assertEquals(Email.MESSAGE_INVALID_NAME_EDGE, Email.getConstraintMessage(".peter@example.com"));
        assertEquals(Email.MESSAGE_INVALID_NAME_EDGE, Email.getConstraintMessage("peter-@example.com"));
        assertEquals(Email.MESSAGE_CONSECUTIVE_PERIODS, Email.getConstraintMessage("peter..jack@example.com"));
        assertEquals(Email.MESSAGE_INVALID_DOMAIN_CHARACTERS, Email.getConstraintMessage("peter@exa_mple.com"));
        assertEquals(Email.MESSAGE_INVALID_DOMAIN_CHARACTERS, Email.getConstraintMessage("peter@exämple.com"));
        assertEquals(Email.MESSAGE_EMPTY_DOMAIN_PART, Email.getConstraintMessage("peter@example..com"));
        assertEquals(Email.MESSAGE_EMPTY_DOMAIN_PART, Email.getConstraintMessage("peter@.example.com"));
        assertEquals(Email.MESSAGE_EMPTY_DOMAIN_PART, Email.getConstraintMessage("peter@example.com."));
        assertEquals(Email.MESSAGE_DOMAIN_PART_EDGE, Email.getConstraintMessage("peter@-example.com"));
        assertEquals(Email.MESSAGE_DOMAIN_PART_EDGE, Email.getConstraintMessage("peter@example-.com"));
        assertEquals(Email.MESSAGE_DOMAIN_PART_EDGE, Email.getConstraintMessage("peter@example.com-"));
        assertEquals(Email.MESSAGE_SHORT_DOMAIN_ENDING, Email.getConstraintMessage("peter@example.c"));
        assertEquals(Email.MESSAGE_SHORT_DOMAIN_ENDING, Email.getConstraintMessage("peter@e"));
        assertThrows(IllegalArgumentException.class, Email.MESSAGE_CONSECUTIVE_PERIODS, ()
                -> new Email("peter..jack@example.com"));
    }

    @Test
    public void getConstraintMessage_validEmail_failsAssertion() {
        assertThrows(AssertionError.class, () -> Email.getConstraintMessage("peter@example.com"));
    }

    @Test
    public void isValidEmail() {
        // null email
        assertThrows(NullPointerException.class, () -> Email.isValidEmail(null));

        // blank email
        assertFalse(Email.isValidEmail("")); // empty string
        assertFalse(Email.isValidEmail(" ")); // spaces only

        // missing parts
        assertFalse(Email.isValidEmail("@example.com")); // missing local part
        assertFalse(Email.isValidEmail("peterjackexample.com")); // missing '@' symbol
        assertFalse(Email.isValidEmail("peterjack@")); // missing domain name

        // invalid parts
        assertFalse(Email.isValidEmail("peterjack@-")); // invalid domain name
        assertFalse(Email.isValidEmail("peterjack@exam_ple.com")); // underscore in domain name
        assertFalse(Email.isValidEmail("peter jack@example.com")); // spaces in local part
        assertFalse(Email.isValidEmail("peterjack@exam ple.com")); // spaces in domain name
        assertFalse(Email.isValidEmail(" peterjack@example.com")); // leading space
        assertFalse(Email.isValidEmail("peterjack@example.com ")); // trailing space
        assertFalse(Email.isValidEmail("peterjack@@example.com")); // double '@' symbol
        assertFalse(Email.isValidEmail("peter@jack@example.com")); // '@' symbol in local part
        assertFalse(Email.isValidEmail("-peterjack@example.com")); // local part starts with a hyphen
        assertFalse(Email.isValidEmail("peterjack-@example.com")); // local part ends with a hyphen
        assertFalse(Email.isValidEmail("'peterjack@example.com")); // local part starts with an apostrophe
        assertFalse(Email.isValidEmail("peterjack'@example.com")); // local part ends with an apostrophe
        assertFalse(Email.isValidEmail(".peterjack@example.com")); // local part starts with a period
        assertFalse(Email.isValidEmail("peterjack.@example.com")); // local part ends with a period
        assertFalse(Email.isValidEmail("peter..jack@example.com")); // local part has two consecutive periods
        assertFalse(Email.isValidEmail("peterjack@example@com")); // '@' symbol in domain name
        assertFalse(Email.isValidEmail("peterjack@.example.com")); // domain name starts with a period
        assertFalse(Email.isValidEmail("peterjack@example.com.")); // domain name ends with a period
        assertFalse(Email.isValidEmail("peterjack@-example.com")); // domain name starts with a hyphen
        assertFalse(Email.isValidEmail("peterjack@example.com-")); // domain name ends with a hyphen
        assertFalse(Email.isValidEmail("peterjack@example..com")); // empty domain label
        assertFalse(Email.isValidEmail("peterjack@example.c")); // top level domain has less than two chars
        assertFalse(Email.isValidEmail("peter{jack@example.com")); // character just after the ASCII letter range
        assertFalse(Email.isValidEmail("peter:jack@example.com")); // character just after the ASCII digit range

        // valid email
        assertTrue(Email.isValidEmail("PeterJack_1190@example.com")); // underscore in local part
        assertTrue(Email.isValidEmail("PeterJack.1190@example.com")); // period in local part
        assertTrue(Email.isValidEmail("PeterJack+1190@example.com")); // '+' symbol in local part
        assertTrue(Email.isValidEmail("PeterJack-1190@example.com")); // hyphen in local part
        assertTrue(Email.isValidEmail("o'connor@example.com")); // apostrophe in local part
        assertTrue(Email.isValidEmail("peter+_-jack@example.com")); // consecutive non-dot special characters
        assertTrue(Email.isValidEmail("peter@example.xn--bcher-kva")); // punycode domain label
        assertTrue(Email.isValidEmail("a@bc")); // minimal
        assertTrue(Email.isValidEmail("test@localhost")); // alphabets only
        assertTrue(Email.isValidEmail("123@145")); // numeric local part and domain name
        assertTrue(Email.isValidEmail("a1+be.d@example1.com")); // mixture of alphanumeric and special characters
        assertTrue(Email.isValidEmail("peter_jack@very-very-very-long-example.com")); // long domain name
        assertTrue(Email.isValidEmail("if.you.dream.it_you.can.do.it@example.com")); // long local part
        assertTrue(Email.isValidEmail("e1234567@u.nus.edu")); // more than one period in domain
        assertTrue(Email.isValidEmail("AZaz09@AZaz09.co")); // ASCII alphanumeric boundaries
    }

    @Test
    public void isValidEmail_localPartLengthBoundary() {
        assertTrue(Email.isValidEmail("a".repeat(63) + "@example.com"));
        assertTrue(Email.isValidEmail("a".repeat(64) + "@example.com"));
        String tooLong = "a".repeat(65) + "@example.com";
        assertFalse(Email.isValidEmail(tooLong));
        assertThrows(IllegalArgumentException.class,
                "The part of an email before the @ must be at most 64 characters long.", () -> new Email(tooLong));
    }

    @Test
    public void isValidEmail_domainLabelLengthBoundary() {
        for (String prefix : new String[]{"a@", "a@sub."}) {
            for (String suffix : new String[]{"", ".com"}) {
                assertTrue(Email.isValidEmail(prefix + "b".repeat(62) + suffix));
                assertTrue(Email.isValidEmail(prefix + "b".repeat(63) + suffix));
                String tooLong = prefix + "b".repeat(64) + suffix;
                assertFalse(Email.isValidEmail(tooLong));
                assertThrows(IllegalArgumentException.class,
                        "Each part of an email's domain between periods must be at most 63 characters long.", ()
                        -> new Email(tooLong));
            }
        }
    }

    @Test
    public void isValidEmail_totalLengthBoundary() {
        String prefix = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63) + ".";
        assertEquals(193, prefix.length());
        assertTrue(Email.isValidEmail(prefix + "d".repeat(60))); // 253 characters
        assertTrue(Email.isValidEmail(prefix + "d".repeat(61))); // 254 characters
        String tooLong = prefix + "d".repeat(62); // All individual parts still meet their limits.
        assertFalse(Email.isValidEmail(tooLong));
        assertThrows(IllegalArgumentException.class, "Emails must be at most 254 characters long.", ()
                -> new Email(tooLong));
    }

    @Test
    public void equals() {
        Email email = new Email("valid@email");

        // same values -> returns true
        assertTrue(email.equals(new Email("valid@email")));

        // same object -> returns true
        assertTrue(email.equals(email));

        // null -> returns false
        assertFalse(email.equals(null));

        // different types -> returns false
        assertFalse(email.equals(5.0f));

        // different values -> returns false
        assertFalse(email.equals(new Email("other.valid@email")));
    }
}
