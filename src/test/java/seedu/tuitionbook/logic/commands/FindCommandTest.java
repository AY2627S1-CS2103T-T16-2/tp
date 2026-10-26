package seedu.tuitionbook.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.logic.Messages.MESSAGE_PERSONS_LISTED_OVERVIEW;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.tuitionbook.testutil.TypicalPersons.ALICE;
import static seedu.tuitionbook.testutil.TypicalPersons.CARL;
import static seedu.tuitionbook.testutil.TypicalPersons.ELLE;
import static seedu.tuitionbook.testutil.TypicalPersons.FIONA;
import static seedu.tuitionbook.testutil.TypicalPersons.GEORGE;
import static seedu.tuitionbook.testutil.TypicalPersons.getTypicalTuitionBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.UserPrefs;
import seedu.tuitionbook.model.person.NameContainsKeywordsPredicate;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code FindCommand}.
 */
public class FindCommandTest {
    private Model model = new ModelManager(getTypicalTuitionBook(), new UserPrefs());
    private Model expectedModel = new ModelManager(getTypicalTuitionBook(), new UserPrefs());

    @Test
    public void equals() {
        NameContainsKeywordsPredicate firstPredicate =
                new NameContainsKeywordsPredicate(List.of("first"));
        NameContainsKeywordsPredicate secondPredicate =
                new NameContainsKeywordsPredicate(List.of("second"));

        FindCommand findFirstCommand = new FindCommand(firstPredicate);
        FindCommand findSecondCommand = new FindCommand(secondPredicate);

        // same object -> returns true
        assertTrue(findFirstCommand.equals(findFirstCommand));

        // same values -> returns true
        FindCommand findFirstCommandCopy = new FindCommand(firstPredicate);
        assertTrue(findFirstCommand.equals(findFirstCommandCopy));

        // different types -> returns false
        assertFalse(findFirstCommand.equals(1));

        // null -> returns false
        assertFalse(findFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(findFirstCommand.equals(findSecondCommand));
    }

    @Test
    public void execute_zeroKeywords_noPersonFound() {
        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 0);
        NameContainsKeywordsPredicate predicate = preparePredicate(" ");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate, "find " + String.join(" ", predicate.getKeywords()));
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void execute_multipleKeywords_multiplePersonsFound() {
        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 3);
        NameContainsKeywordsPredicate predicate = preparePredicate("Kurz Elle Kunz");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate, "find " + String.join(" ", predicate.getKeywords()));
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(CARL, ELLE, FIONA), model.getFilteredPersonList());
    }

    @Test
    public void execute_substringKeywords_filtersNamesWithOrSemantics() {
        Person grace = new PersonBuilder().withName("Grace Tan").build();
        Person bob = new PersonBuilder().withName("Bob Lim").build();
        Person alice = new PersonBuilder().withName("Alice Ng").build();
        Model substringModel = modelWith(grace, bob, alice);
        Model expectedSubstringModel = modelWith(grace, bob, alice);
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("rac", "BOB"));
        expectedSubstringModel.updateFilteredPersonList(predicate, "find " + String.join(" ", predicate.getKeywords()));

        FindCommand command = new FindCommand(predicate);
        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 2);

        assertCommandSuccess(command, substringModel, expectedMessage, expectedSubstringModel);
        assertEquals(List.of(grace, bob), substringModel.getFilteredPersonList());
    }

    @Test
    public void execute_unicodeCaseVariant_filtersMatchingName() {
        Person isik = new PersonBuilder().withName("Işık").build();
        Person alice = new PersonBuilder().withName("Alice Ng").build();
        Model unicodeModel = modelWith(isik, alice);
        Model expectedUnicodeModel = modelWith(isik, alice);
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("ışı"));
        expectedUnicodeModel.updateFilteredPersonList(predicate, "find " + String.join(" ", predicate.getKeywords()));

        FindCommand command = new FindCommand(predicate);
        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 1);

        assertCommandSuccess(command, unicodeModel, expectedMessage, expectedUnicodeModel);
        assertEquals(List.of(isik), unicodeModel.getFilteredPersonList());
    }

    @Test
    public void execute_unicodeComparisonForm_filtersMatchingNameWithoutReorderingOrMutation() {
        Person street = new PersonBuilder().withName("Straße Tan").build();
        Person gross = new PersonBuilder().withName("Groß Lim").build();
        Person alice = new PersonBuilder().withName("Alice Ng").build();
        Model unicodeModel = modelWith(street, gross, alice);
        Model expectedUnicodeModel = modelWith(street, gross, alice);
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("STRASSE", "GROẞ"));
        expectedUnicodeModel.updateFilteredPersonList(predicate, "find " + String.join(" ", predicate.getKeywords()));

        FindCommand command = new FindCommand(predicate);
        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 2);

        assertCommandSuccess(command, unicodeModel, expectedMessage, expectedUnicodeModel);
        assertEquals(List.of(street, gross), unicodeModel.getFilteredPersonList());
        assertFalse(command.requiresSave());
        assertEquals("Straße Tan", street.getName().fullName);
        assertEquals("Groß Lim", gross.getName().fullName);
    }

    @Test
    public void execute_multipleKeywordsMatchSamePerson_personListedOnce() {
        Person grace = new PersonBuilder().withName("Grace Tan").build();
        Model singlePersonModel = modelWith(grace);
        Model expectedSinglePersonModel = modelWith(grace);
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("gr", "rac", "ace"));
        expectedSinglePersonModel.updateFilteredPersonList(predicate,
                "find " + String.join(" ", predicate.getKeywords()));

        FindCommand command = new FindCommand(predicate);
        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 1);

        assertCommandSuccess(command, singlePersonModel, expectedMessage, expectedSinglePersonModel);
        assertEquals(List.of(grace), singlePersonModel.getFilteredPersonList());
    }

    @Test
    public void execute_replacesExistingFilter_listRestoresAllPersons() {
        NameContainsKeywordsPredicate firstPredicate = new NameContainsKeywordsPredicate(List.of("Ali"));
        expectedModel.updateFilteredPersonList(firstPredicate,
                "find " + String.join(" ", firstPredicate.getKeywords()));
        assertCommandSuccess(new FindCommand(firstPredicate), model,
                String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 1), expectedModel);
        assertEquals(List.of(ALICE), model.getFilteredPersonList());

        NameContainsKeywordsPredicate replacementPredicate = new NameContainsKeywordsPredicate(List.of("Geo"));
        expectedModel.updateFilteredPersonList(replacementPredicate,
                "find " + String.join(" ", replacementPredicate.getKeywords()));
        assertCommandSuccess(new FindCommand(replacementPredicate), model,
                String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 1), expectedModel);
        assertEquals(List.of(GEORGE), model.getFilteredPersonList());

        Model unfilteredModel = new ModelManager(getTypicalTuitionBook(), new UserPrefs());
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, unfilteredModel);
        assertEquals(getTypicalTuitionBook().getPersonList(), model.getFilteredPersonList());
    }

    @Test
    public void requiresSave_returnsFalse() {
        FindCommand command = new FindCommand(new NameContainsKeywordsPredicate(List.of("Grace")));

        assertFalse(command.requiresSave());
    }

    @Test
    public void toStringMethod() {
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("keyword"));
        FindCommand findCommand = new FindCommand(predicate);
        String expected = FindCommand.class.getCanonicalName() + "{predicate=" + predicate + "}";
        assertEquals(expected, findCommand.toString());
    }

    /**
     * Parses {@code userInput} into a {@code NameContainsKeywordsPredicate}.
     */
    private NameContainsKeywordsPredicate preparePredicate(String userInput) {
        return new NameContainsKeywordsPredicate(List.of(userInput.split("\\s+")));
    }

    private static Model modelWith(Person... persons) {
        Model newModel = new ModelManager();
        for (Person person : persons) {
            newModel.addPerson(person);
        }
        return newModel;
    }
}
