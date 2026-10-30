package seedu.tuitionbook.model.person;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.testutil.PersonBuilder;

public class NameContainsKeywordsPredicateTest {

    @Test
    public void equals() {
        List<String> firstPredicateKeywordList = List.of("first");
        List<String> secondPredicateKeywordList = List.of("first", "second");

        NameContainsKeywordsPredicate firstPredicate = new NameContainsKeywordsPredicate(firstPredicateKeywordList);
        NameContainsKeywordsPredicate secondPredicate = new NameContainsKeywordsPredicate(secondPredicateKeywordList);

        // same object -> returns true
        assertTrue(firstPredicate.equals(firstPredicate));

        // same values -> returns true
        NameContainsKeywordsPredicate firstPredicateCopy = new NameContainsKeywordsPredicate(firstPredicateKeywordList);
        assertTrue(firstPredicate.equals(firstPredicateCopy));
        assertEquals(firstPredicate.hashCode(), firstPredicateCopy.hashCode());

        // different types -> returns false
        assertFalse(firstPredicate.equals(1));

        // null -> returns false
        assertFalse(firstPredicate.equals(null));

        // different person -> returns false
        assertFalse(firstPredicate.equals(secondPredicate));
    }

    @Test
    public void test_nameContainsKeywords_returnsTrue() {
        // One keyword
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("Alice"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Multiple keywords
        predicate = new NameContainsKeywordsPredicate(List.of("Alice", "Bob"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Only one matching keyword
        predicate = new NameContainsKeywordsPredicate(List.of("Bob", "Carol"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Carol").build()));

        // Mixed-case keywords
        predicate = new NameContainsKeywordsPredicate(List.of("aLIce", "bOB"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));
    }

    @Test
    public void test_nameContainsKeywordAtStart_returnsTrue() {
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("gr"));

        assertTrue(predicate.test(new PersonBuilder().withName("Grace Tan").build()));
    }

    @Test
    public void test_nameContainsKeywordInMiddle_returnsTrue() {
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("rac"));

        assertTrue(predicate.test(new PersonBuilder().withName("Grace Tan").build()));
    }

    @Test
    public void test_nameContainsKeywordAtEnd_returnsTrue() {
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("ace"));

        assertTrue(predicate.test(new PersonBuilder().withName("Grace Tan").build()));
    }

    @Test
    public void test_nameContainsFullKeywordWithMixedCase_returnsTrue() {
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("GRACE"));

        assertTrue(predicate.test(new PersonBuilder().withName("Grace Tan").build()));
    }

    @Test
    public void test_nameContainsKeywordInLaterNameComponent_returnsTrue() {
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("AN"));

        assertTrue(predicate.test(new PersonBuilder().withName("Grace Tan").build()));
    }

    @Test
    public void test_turkishDotlessICaseVariants_returnsTrue() {
        Person isik = new PersonBuilder().withName("Işık").build();

        assertTrue(new NameContainsKeywordsPredicate(List.of("ışık")).test(isik));
        assertTrue(new NameContainsKeywordsPredicate(List.of("ışı")).test(isik));
    }

    @Test
    public void test_greekSigmaCaseVariantsInPartialName_returnsTrue() {
        Person sigma = new PersonBuilder().withName("Σigma Tan").build();

        assertTrue(new NameContainsKeywordsPredicate(List.of("σig")).test(sigma));
        assertTrue(new NameContainsKeywordsPredicate(List.of("ςig")).test(sigma));
        assertTrue(new NameContainsKeywordsPredicate(List.of("ΣIG")).test(sigma));
    }

    @Test
    public void test_supplementaryPlaneCasePair_returnsTrue() {
        String deseretCapitalLetter = "\uD801\uDC00";
        String deseretSmallLetter = "\uD801\uDC28";
        Person deseret = new PersonBuilder().withName(deseretCapitalLetter + " Name").build();

        assertTrue(new NameContainsKeywordsPredicate(List.of(deseretSmallLetter)).test(deseret));
    }

    @Test
    public void test_canonicallyEquivalentAccentsInEitherDirection_returnsTrue() {
        Person decomposedJose = new PersonBuilder().withName("Jose\u0301 Tan").build();
        Person composedJose = new PersonBuilder().withName("Jos\u00e9 Tan").build();

        assertTrue(new NameContainsKeywordsPredicate(List.of("\u00e9")).test(decomposedJose));
        assertTrue(new NameContainsKeywordsPredicate(List.of("e\u0301")).test(composedJose));
    }

    @Test
    public void test_sharpSCaseVariants_returnsTrue() {
        Person street = new PersonBuilder().withName("Straße").build();
        Person capitalSharpS = new PersonBuilder().withName("GROẞ").build();

        assertTrue(new NameContainsKeywordsPredicate(List.of("STRASSE")).test(street));
        assertTrue(new NameContainsKeywordsPredicate(List.of("Groß")).test(capitalSharpS));
        assertTrue(new NameContainsKeywordsPredicate(List.of("GROẞ")).test(
                new PersonBuilder().withName("Groß").build()));
    }

    @Test
    public void test_repeatedSpacesAreNotCollapsed_returnsFalse() {
        Person person = new PersonBuilder().withName("Amy  Bee").build();

        assertFalse(new NameContainsKeywordsPredicate(List.of("y b")).test(person));
    }

    @Test
    public void test_regexLikePunctuation_isTreatedLiterally() {
        Person person = new PersonBuilder().withName("A.B Tan").build();

        assertTrue(new NameContainsKeywordsPredicate(List.of(".")).test(person));
        assertFalse(new NameContainsKeywordsPredicate(List.of(".*")).test(person));
        assertFalse(assertDoesNotThrow(() -> new NameContainsKeywordsPredicate(List.of("[")).test(person)));
        assertFalse(assertDoesNotThrow(() -> new NameContainsKeywordsPredicate(List.of("\\")).test(person)));
    }

    @Test
    public void test_nameDoesNotContainKeywords_returnsFalse() {
        // Zero keywords
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of());
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").build()));

        // Non-matching keyword
        predicate = new NameContainsKeywordsPredicate(List.of("Carol"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // Keywords match non-name fields, including the linked guardian's name, but do not match the person's name
        Person guardian = new PersonBuilder().withName("Parent Match").withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Alice").withPhone("12345")
                .withEmail("alice@email.com").withAddress("Main Street").withTags("Tuition")
                .withRole(Role.STUDENT).withGuardian(guardian).build();
        predicate = new NameContainsKeywordsPredicate(
                List.of("12345", "alice@email.com", "Main", "Street", "Tuition", "student", "Parent", "Match"));
        assertFalse(predicate.test(student));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("keyword1", "keyword2");
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(keywords);

        String expected = NameContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=" + keywords + "}";
        assertEquals(expected, predicate.toString());
    }
}
