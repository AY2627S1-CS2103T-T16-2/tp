package seedu.tuitionbook.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.tuitionbook.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.tuitionbook.testutil.TypicalPersons.getTypicalTuitionBook;

import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.tuitionbook.logic.Messages;
import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.UserPrefs;
import seedu.tuitionbook.model.person.NameContainsKeywordsPredicate;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.PersonHasRolePredicate;
import seedu.tuitionbook.model.person.PersonMatchesListFilterPredicate;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalTuitionBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getTuitionBook(), new UserPrefs());
    }

    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandSuccess(new ListCommand(), model, ListCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_filterByRole_showsOnlyMatchingPersons() {
        Person student = new PersonBuilder().withName("Sam Student").withRole(Role.STUDENT).build();
        Person guardian = new PersonBuilder().withName("Gail Guardian").withRole(Role.GUARDIAN).build();
        Model roleModel = modelWith(student, guardian);
        Model expectedRoleModel = modelWith(student, guardian);

        PersonHasRolePredicate predicate = new PersonHasRolePredicate(Role.GUARDIAN);
        expectedRoleModel.updateFilteredPersonList(predicate);
        String expectedMessage = String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, 1);

        assertCommandSuccess(new ListCommand(predicate), roleModel, expectedMessage, expectedRoleModel);
        assertEquals(List.of(guardian), roleModel.getFilteredPersonList());
    }

    @Test
    public void execute_filterByRoleWithNoMatches_showsEmptyList() {
        Person student = new PersonBuilder().withName("Sam Student").withRole(Role.STUDENT).build();
        Model studentOnlyModel = modelWith(student);
        Model expectedStudentOnlyModel = modelWith(student);

        PersonHasRolePredicate predicate = new PersonHasRolePredicate(Role.GUARDIAN);
        expectedStudentOnlyModel.updateFilteredPersonList(predicate);
        String expectedMessage = String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, 0);

        assertCommandSuccess(new ListCommand(predicate), studentOnlyModel, expectedMessage,
                expectedStudentOnlyModel);
        assertEquals(List.of(), studentOnlyModel.getFilteredPersonList());
    }

    @Test
    public void execute_filterByTag_usesExactCaseInsensitiveMatching() {
        Person active = new PersonBuilder().withName("Active Student").withTags("active").build();
        Person inactive = new PersonBuilder().withName("Inactive Student").withTags("inactive").build();
        Model tagModel = modelWith(active, inactive);
        Model expectedTagModel = modelWith(active, inactive);

        PersonMatchesListFilterPredicate predicate =
                new PersonMatchesListFilterPredicate(null, List.of("ACTIVE"));
        expectedTagModel.updateFilteredPersonList(predicate);
        String expectedMessage = String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, 1);

        assertCommandSuccess(new ListCommand(predicate), tagModel, expectedMessage, expectedTagModel);
        assertEquals(List.of(active), tagModel.getFilteredPersonList());
    }

    @Test
    public void execute_filterByMultipleTags_returnsOnlyPersonsWithAllTags() {
        Person bothTags = new PersonBuilder().withName("Both Tags")
                .withTags("active", "math").build();
        Person onlyActive = new PersonBuilder().withName("Only Active")
                .withTags("active").build();
        Person onlyMath = new PersonBuilder().withName("Only Math")
                .withTags("math").build();
        Model tagModel = modelWith(bothTags, onlyActive, onlyMath);
        Model expectedTagModel = modelWith(bothTags, onlyActive, onlyMath);

        PersonMatchesListFilterPredicate predicate =
                new PersonMatchesListFilterPredicate(null, List.of("ACTIVE", "MATH"));
        expectedTagModel.updateFilteredPersonList(predicate);
        String expectedMessage = String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, 1);

        assertCommandSuccess(new ListCommand(predicate), tagModel, expectedMessage, expectedTagModel);
        assertEquals(List.of(bothTags), tagModel.getFilteredPersonList());
    }

    @Test
    public void execute_filterByCaseInsensitiveDuplicateTags_returnsMatchingPerson() {
        Person active = new PersonBuilder().withName("Active Student")
                .withTags("active").build();
        Person inactive = new PersonBuilder().withName("Inactive Student")
                .withTags("inactive").build();
        Model tagModel = modelWith(active, inactive);
        Model expectedTagModel = modelWith(active, inactive);

        PersonMatchesListFilterPredicate predicate =
                new PersonMatchesListFilterPredicate(null, List.of("ACTIVE", "active"));
        expectedTagModel.updateFilteredPersonList(predicate);
        String expectedMessage = String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, 1);

        assertCommandSuccess(new ListCommand(predicate), tagModel, expectedMessage, expectedTagModel);
        assertEquals(List.of(active), tagModel.getFilteredPersonList());
    }

    @Test
    public void execute_filterByRoleAndMultipleTags_returnsOnlyMatchingPerson() {
        Person matchingStudent = new PersonBuilder().withName("Matching Student")
                .withRole(Role.STUDENT).withTags("active", "inactive", "math").build();
        Person wrongRole = new PersonBuilder().withName("Matching Guardian")
                .withRole(Role.GUARDIAN).withTags("active", "inactive", "math").build();
        Person missingTag = new PersonBuilder().withName("Missing Math")
                .withRole(Role.STUDENT).withTags("active").build();
        Person missingInactive = new PersonBuilder().withName("Missing Inactive")
                .withRole(Role.STUDENT).withTags("active", "math").build();
        Model filterModel = modelWith(matchingStudent, wrongRole, missingTag, missingInactive);
        Model expectedFilterModel = modelWith(matchingStudent, wrongRole, missingTag, missingInactive);

        PersonMatchesListFilterPredicate predicate = new PersonMatchesListFilterPredicate(
                Role.STUDENT, List.of("ACTIVE", "INACTIVE", "MATH"));
        expectedFilterModel.updateFilteredPersonList(predicate);
        String expectedMessage = String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, 1);

        assertCommandSuccess(new ListCommand(predicate), filterModel, expectedMessage, expectedFilterModel);
        assertEquals(List.of(matchingStudent), filterModel.getFilteredPersonList());
    }

    /**
     * Returns a model containing exactly the given persons, so role-filter tests do not
     * depend on the roles assigned to {@code TypicalPersons}.
     */
    private static Model modelWith(Person... persons) {
        Model newModel = new ModelManager();
        for (Person person : persons) {
            newModel.addPerson(person);
        }
        return newModel;
    }

    @Test
    public void execute_tagFilter_replacesNameAndRoleFiltersAndBareListRestoresOrder() {
        Person student = new PersonBuilder().withName("Sam Student").withRole(Role.STUDENT)
                .withTags("math").build();
        Person guardian = new PersonBuilder().withName("Gail Guardian").withRole(Role.GUARDIAN)
                .withTags("active").build();
        Person anotherStudent = new PersonBuilder().withName("Sue Student").withRole(Role.STUDENT)
                .withTags("active").build();
        List<Predicate<Person>> initialFilters = List.of(
                new NameContainsKeywordsPredicate(List.of("Sam")), new PersonHasRolePredicate(Role.STUDENT));

        for (Predicate<Person> initialFilter : initialFilters) {
            Model actual = modelWith(student, guardian, anotherStudent);
            actual.updateFilteredPersonList(initialFilter);
            assertFalse(actual.getFilteredPersonList().contains(guardian));
            Model expected = modelWith(student, guardian, anotherStudent);
            PersonMatchesListFilterPredicate tagFilter =
                    new PersonMatchesListFilterPredicate(null, List.of("ACTIVE"));
            expected.updateFilteredPersonList(tagFilter);

            assertCommandSuccess(new ListCommand(tagFilter), actual, "2 person(s) listed!", expected);
            assertEquals(List.of(guardian, anotherStudent), actual.getFilteredPersonList());
            assertCommandSuccess(new ListCommand(), actual, ListCommand.MESSAGE_SUCCESS,
                    modelWith(student, guardian, anotherStudent));
            assertEquals(List.of(student, guardian, anotherStudent), actual.getFilteredPersonList());
        }
    }

    @Test
    public void execute_tagFilterWithNoMatches_returnsSuccessfulZeroCount() {
        Person person = new PersonBuilder().withTags("inactive").build();
        Model actual = modelWith(person);
        Model expected = modelWith(person);
        PersonMatchesListFilterPredicate filter = new PersonMatchesListFilterPredicate(null, List.of("active"));
        expected.updateFilteredPersonList(filter);
        assertCommandSuccess(new ListCommand(filter), actual, "0 person(s) listed!", expected);
        assertTrue(actual.getFilteredPersonList().isEmpty());
        assertEquals(List.of(person), actual.getTuitionBook().getPersonList());
    }

    @Test
    public void equals() {
        PersonHasRolePredicate studentPredicate = new PersonHasRolePredicate(Role.STUDENT);
        PersonHasRolePredicate guardianPredicate = new PersonHasRolePredicate(Role.GUARDIAN);

        ListCommand listAllCommand = new ListCommand();
        ListCommand listStudentsCommand = new ListCommand(studentPredicate);

        // same object -> returns true
        assertTrue(listAllCommand.equals(listAllCommand));
        assertTrue(listStudentsCommand.equals(listStudentsCommand));

        // same values -> returns true
        assertTrue(listAllCommand.equals(new ListCommand()));
        assertTrue(listStudentsCommand.equals(new ListCommand(new PersonHasRolePredicate(Role.STUDENT))));

        // different types -> returns false
        assertFalse(listAllCommand.equals(1));

        // null -> returns false
        assertFalse(listAllCommand.equals(null));

        // different predicate -> returns false
        assertFalse(listStudentsCommand.equals(new ListCommand(guardianPredicate)));
        assertFalse(listAllCommand.equals(listStudentsCommand));
    }

    @Test
    public void toStringMethod() {
        PersonHasRolePredicate predicate = new PersonHasRolePredicate(Role.STUDENT);
        ListCommand listCommand = new ListCommand(predicate);
        String expected = ListCommand.class.getCanonicalName() + "{predicate=" + predicate + "}";
        assertEquals(expected, listCommand.toString());

        ListCommand listAllCommand = new ListCommand();
        String expectedListAll = ListCommand.class.getCanonicalName() + "{predicate=null}";
        assertEquals(expectedListAll, listAllCommand.toString());
    }
}
