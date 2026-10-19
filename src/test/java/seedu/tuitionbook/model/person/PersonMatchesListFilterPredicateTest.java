package seedu.tuitionbook.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.testutil.PersonBuilder;

public class PersonMatchesListFilterPredicateTest {

    @Test
    public void constructor_nullTags_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new PersonMatchesListFilterPredicate(null, null));
        assertThrows(NullPointerException.class, () ->
                new PersonMatchesListFilterPredicate(null, Arrays.asList("active", null)));
    }

    @Test
    public void constructor_mutatingOriginalList_doesNotChangeFilter() {
        List<String> tags = new ArrayList<>(List.of("active"));
        PersonMatchesListFilterPredicate predicate = new PersonMatchesListFilterPredicate(null, tags);
        tags.clear();
        assertFalse(predicate.test(new PersonBuilder().withTags("inactive").build()));
        assertTrue(predicate.test(new PersonBuilder().withTags("active").build()));
    }

    @Test
    public void test_emptyTags_matchesOnlyOptionalRole() {
        Person student = new PersonBuilder().withRole(Role.STUDENT).withTags().build();
        Person guardian = new PersonBuilder().withRole(Role.GUARDIAN).withTags().build();
        PersonMatchesListFilterPredicate unrestricted = new PersonMatchesListFilterPredicate(null, List.of());
        assertTrue(unrestricted.test(student));
        assertTrue(unrestricted.test(guardian));
        PersonMatchesListFilterPredicate students = new PersonMatchesListFilterPredicate(Role.STUDENT, List.of());
        assertTrue(students.test(student));
        assertFalse(students.test(guardian));
    }

    @Test
    public void test_tags_requiresAllExactMatchesIgnoringCase() {
        PersonMatchesListFilterPredicate predicate = new PersonMatchesListFilterPredicate(
                null, List.of("ACTIVE", "math", "active"));
        assertTrue(predicate.test(new PersonBuilder().withTags("active", "MATH", "extra").build()));
        assertFalse(predicate.test(new PersonBuilder().withTags("inactive", "math").build()));
        assertFalse(predicate.test(new PersonBuilder().withTags("active").build()));
        assertFalse(predicate.test(new PersonBuilder().withTags().build()));
    }

    @Test
    public void equals() {
        PersonMatchesListFilterPredicate predicate = new PersonMatchesListFilterPredicate(
                Role.STUDENT, List.of("active", "math"));
        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(new PersonMatchesListFilterPredicate(Role.STUDENT, List.of("active", "math"))));
        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals(1));
        assertFalse(predicate.equals(new PersonMatchesListFilterPredicate(Role.GUARDIAN, List.of("active", "math"))));
        assertFalse(predicate.equals(new PersonMatchesListFilterPredicate(null, List.of("active", "math"))));
        assertFalse(predicate.equals(new PersonMatchesListFilterPredicate(Role.STUDENT, List.of("active"))));
        assertFalse(predicate.equals(new PersonMatchesListFilterPredicate(Role.STUDENT, List.of("ACTIVE", "math"))));
        assertFalse(predicate.equals(new PersonMatchesListFilterPredicate(Role.STUDENT, List.of("math", "active"))));
    }

    @Test
    public void toStringMethod() {
        PersonMatchesListFilterPredicate predicate = new PersonMatchesListFilterPredicate(
                Role.STUDENT, List.of("active", "math"));
        assertEquals(PersonMatchesListFilterPredicate.class.getCanonicalName()
                + "{role=student, tags=[active, math]}", predicate.toString());
        assertEquals(PersonMatchesListFilterPredicate.class.getCanonicalName()
                + "{role=null, tags=[]}", new PersonMatchesListFilterPredicate(null, List.of()).toString());
    }
}
