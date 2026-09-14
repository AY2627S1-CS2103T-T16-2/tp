package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;

public class RemarkCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_nonEmptyRemark_success() {
        assertRemarkSuccess(new Remark("Likes baseball"), RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS);
    }

    @Test
    public void execute_emptyRemark_success() {
        assertRemarkSuccess(new Remark(""), RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new RemarkCommand(outOfBoundIndex, new Remark("note")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_filteredList_usesDisplayedIndexAndRefreshesList() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertRemarkSuccess(new Remark("Filtered person"), RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS);
    }

    @Test
    public void equals() {
        RemarkCommand first = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("one"));
        assertTrue(first.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("one"))));
        assertFalse(first.equals(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("one"))));
        assertFalse(first.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("two"))));
        assertFalse(first.equals(null));
        assertFalse(first.equals(1));
    }

    private void assertRemarkSuccess(Remark remark, String successMessage) {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new Person(personToEdit.getName(), personToEdit.getPhone(), personToEdit.getEmail(),
                personToEdit.getAddress(), remark, personToEdit.getTags());
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, remark);
        String expectedMessage = String.format(successMessage, Messages.format(editedPerson));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);
        expectedModel.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }
}
