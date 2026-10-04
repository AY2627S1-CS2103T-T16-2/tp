package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonHasRolePredicateTest {

    @Test
    public void constructor_nullRole_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PersonHasRolePredicate(null));
    }

    @Test
    public void test_personHasMatchingRole_returnsTrue() {
        PersonHasRolePredicate studentPredicate = new PersonHasRolePredicate(Role.STUDENT);
        assertTrue(studentPredicate.test(new PersonBuilder().withRole(Role.STUDENT).build()));

        PersonHasRolePredicate guardianPredicate = new PersonHasRolePredicate(Role.GUARDIAN);
        assertTrue(guardianPredicate.test(new PersonBuilder().withRole(Role.GUARDIAN).build()));
    }

    @Test
    public void test_personHasDifferentRole_returnsFalse() {
        PersonHasRolePredicate studentPredicate = new PersonHasRolePredicate(Role.STUDENT);
        assertFalse(studentPredicate.test(new PersonBuilder().withRole(Role.GUARDIAN).build()));

        PersonHasRolePredicate guardianPredicate = new PersonHasRolePredicate(Role.GUARDIAN);
        assertFalse(guardianPredicate.test(new PersonBuilder().withRole(Role.STUDENT).build()));
    }

    @Test
    public void equals() {
        PersonHasRolePredicate studentPredicate = new PersonHasRolePredicate(Role.STUDENT);
        PersonHasRolePredicate guardianPredicate = new PersonHasRolePredicate(Role.GUARDIAN);

        // same object -> returns true
        assertTrue(studentPredicate.equals(studentPredicate));

        // same values -> returns true
        assertTrue(studentPredicate.equals(new PersonHasRolePredicate(Role.STUDENT)));

        // different types -> returns false
        assertFalse(studentPredicate.equals(1));

        // null -> returns false
        assertFalse(studentPredicate.equals(null));

        // different role -> returns false
        assertFalse(studentPredicate.equals(guardianPredicate));
    }

    @Test
    public void toStringMethod() {
        PersonHasRolePredicate predicate = new PersonHasRolePredicate(Role.STUDENT);
        String expected = PersonHasRolePredicate.class.getCanonicalName() + "{role=" + Role.STUDENT + "}";
        assertEquals(expected, predicate.toString());
    }
}
