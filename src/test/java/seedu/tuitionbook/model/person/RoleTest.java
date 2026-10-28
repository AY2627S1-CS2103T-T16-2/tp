package seedu.tuitionbook.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.testutil.Assert.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

public class RoleTest {

    @Test
    public void isValidRole() {
        // null -> returns false
        assertFalse(Role.isValidRole(null));

        // invalid roles
        assertFalse(Role.isValidRole("")); // empty string
        assertFalse(Role.isValidRole(" ")); // spaces only
        assertFalse(Role.isValidRole("teacher")); // not a supported role
        assertFalse(Role.isValidRole("stu dent")); // internal space
        assertFalse(Role.isValidRole("students")); // trailing character

        // valid roles
        assertTrue(Role.isValidRole("student"));
        assertTrue(Role.isValidRole("guardian"));
        assertTrue(Role.isValidRole("STUDENT")); // case-insensitive
        assertTrue(Role.isValidRole("Guardian")); // mixed case
        assertTrue(Role.isValidRole(" student ")); // surrounding whitespace
    }

    @Test
    public void isValidRole_nonAsciiLookalikes_returnsFalse() {
        // Each of these uppercases to STUDENT or GUARDIAN under Unicode case mapping.
        assertFalse(Role.isValidRole("\u017Ftudent")); // Latin small long s
        assertFalse(Role.isValidRole("\uFB06udent")); // st ligature
        assertFalse(Role.isValidRole("guard\u0131an")); // Turkish dotless i
        assertFalse(Role.isValidRole("\u0455tudent")); // Cyrillic dze
        assertFalse(Role.isValidRole("\uFF53tudent")); // full-width s
        assertThrows(IllegalArgumentException.class, () -> Role.fromString("\u017Ftudent"));
    }

    @Test
    public void fromString_validRole_returnsRole() {
        assertEquals(Role.STUDENT, Role.fromString("student"));
        assertEquals(Role.GUARDIAN, Role.fromString("guardian"));
        assertEquals(Role.STUDENT, Role.fromString("STUDENT"));
        assertEquals(Role.GUARDIAN, Role.fromString(" Guardian "));
    }

    @Test
    public void roleWithEdgeControlCharacters_isRejected() {
        for (char control : new char[] {'\u0000', '\u0001', '\u001b', '\u001c', '\u007f', '\u0080', '\u009f'}) {
            for (String role : new String[] {control + "student", "guardian" + control}) {
                assertFalse(Role.isValidRole(role));
                assertThrows(IllegalArgumentException.class, Role.MESSAGE_CONSTRAINTS, () -> Role.fromString(role));
            }
        }
    }

    @Test
    public void fromString_invalidRole_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Role.fromString("teacher"));
        assertThrows(IllegalArgumentException.class, () -> Role.fromString(""));
        assertThrows(IllegalArgumentException.class, () -> Role.fromString(null));
    }

    @Test
    public void toString_returnsLowercaseName() {
        assertEquals("student", Role.STUDENT.toString());
        assertEquals("guardian", Role.GUARDIAN.toString());
    }

    @Test
    @ResourceLock(Resources.LOCALE)
    public void roleOperations_turkishLocale_remainLocaleIndependent() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));

            assertTrue(Role.isValidRole("guardian"));
            assertTrue(Role.isValidRole("STUDENT"));
            assertTrue(Role.isValidRole(" GuArDiAn "));
            assertEquals(Role.GUARDIAN, Role.fromString("guardian"));
            assertEquals(Role.STUDENT, Role.fromString("STUDENT"));
            assertEquals(Role.GUARDIAN, Role.fromString(" GuArDiAn "));
            assertEquals("student", Role.STUDENT.toString());
            assertEquals("guardian", Role.GUARDIAN.toString());
        } finally {
            Locale.setDefault(originalLocale);
        }
    }
}
