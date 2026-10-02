package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

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
    public void fromString_validRole_returnsRole() {
        assertEquals(Role.STUDENT, Role.fromString("student"));
        assertEquals(Role.GUARDIAN, Role.fromString("guardian"));
        assertEquals(Role.STUDENT, Role.fromString("STUDENT"));
        assertEquals(Role.GUARDIAN, Role.fromString(" Guardian "));
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
}
