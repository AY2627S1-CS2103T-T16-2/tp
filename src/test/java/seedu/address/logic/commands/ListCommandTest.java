package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonHasRolePredicate;
import seedu.address.model.person.Role;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
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
