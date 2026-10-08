package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Role;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteCommand}.
 */
public class DeleteCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(personToDelete));

        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(personToDelete));

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);
        showNoPerson(expectedModel);

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_guardianWithLinkedStudents_clearsStudentGuardianLinks() {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("90000001")
                .withRole(Role.GUARDIAN).build();
        Person firstStudent = new PersonBuilder().withName("Sam Student").withPhone("90000002")
                .withRole(Role.STUDENT).withGuardian(guardian).build();
        Person secondStudent = new PersonBuilder().withName("Sally Student").withPhone("90000003")
                .withRole(Role.STUDENT).withGuardian(guardian).build();
        Person unlinkedStudent = new PersonBuilder().withName("Una Student").withPhone("90000004")
                .withRole(Role.STUDENT).build();
        model = modelWith(guardian, firstStudent, secondStudent, unlinkedStudent);

        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);
        Model expectedModel = modelWith(firstStudent.clearGuardian(), secondStudent.clearGuardian(), unlinkedStudent);
        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(guardian))
                + String.format(DeleteCommand.MESSAGE_GUARDIAN_LINKS_CLEARED, 2, "Sam Student, Sally Student");

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_guardianWithNoLinkedStudents_deletesGuardian() {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("90000001")
                .withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Sam Student").withPhone("90000002")
                .withRole(Role.STUDENT).build();
        model = modelWith(guardian, student);

        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);
        Model expectedModel = modelWith(student);
        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(guardian));

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_guardianWithOneLinkedStudent_clearsStudentGuardianLink() {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("90000001")
                .withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Sam Student").withPhone("90000002")
                .withRole(Role.STUDENT).withGuardian(guardian).build();
        model = modelWith(guardian, student);

        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);
        Model expectedModel = modelWith(student.clearGuardian());
        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(guardian))
                + String.format(DeleteCommand.MESSAGE_GUARDIAN_LINKS_CLEARED, 1, "Sam Student");

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_guardianAtFilteredIndex_clearsStudentGuardianLinks() {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("90000001")
                .withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Sam Student").withPhone("90000002")
                .withRole(Role.STUDENT).withGuardian(guardian).build();
        Person otherStudent = new PersonBuilder().withName("Sally Student").withPhone("90000003")
                .withRole(Role.STUDENT).build();
        model = modelWith(student, guardian, otherStudent);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);
        Model expectedModel = modelWith(student.clearGuardian(), otherStudent);
        showNoPerson(expectedModel);
        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(guardian))
                + String.format(DeleteCommand.MESSAGE_GUARDIAN_LINKS_CLEARED, 1, "Sam Student");

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_studentWithGuardian_deletesStudentAndRetainsGuardian() {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("90000001")
                .withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Sam Student").withPhone("90000002")
                .withRole(Role.STUDENT).withGuardian(guardian).build();
        model = modelWith(student, guardian);

        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);
        Model expectedModel = modelWith(guardian);
        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(student));

        assertCommandSuccess(deleteCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        DeleteCommand deleteFirstCommand = new DeleteCommand(INDEX_FIRST_PERSON);
        DeleteCommand deleteSecondCommand = new DeleteCommand(INDEX_SECOND_PERSON);

        // same object -> returns true
        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));

        // same values -> returns true
        DeleteCommand deleteFirstCommandCopy = new DeleteCommand(INDEX_FIRST_PERSON);
        assertTrue(deleteFirstCommand.equals(deleteFirstCommandCopy));

        // different types -> returns false
        assertFalse(deleteFirstCommand.equals(1));

        // null -> returns false
        assertFalse(deleteFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));
    }

    @Test
    public void toStringMethod() {
        Index targetIndex = Index.fromOneBased(1);
        DeleteCommand deleteCommand = new DeleteCommand(targetIndex);
        String expected = DeleteCommand.class.getCanonicalName() + "{targetIndex=" + targetIndex + "}";
        assertEquals(expected, deleteCommand.toString());
    }

    /**
     * Updates {@code model}'s filtered list to show no one.
     */
    private void showNoPerson(Model model) {
        model.updateFilteredPersonList(p -> false);

        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    private Model modelWith(Person... persons) {
        Model model = new ModelManager();
        for (Person person : List.of(persons)) {
            model.addPerson(person);
        }
        return model;
    }
}
