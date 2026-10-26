package seedu.tuitionbook.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.tuitionbook.testutil.TypicalPersons.getTypicalTuitionBook;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.UserPrefs;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.testutil.PersonBuilder;

public class ClearCommandTest {

    @Test
    public void execute_emptyTuitionBook_success() {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_nonEmptyTuitionBook_success() {
        Model model = new ModelManager(getTypicalTuitionBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalTuitionBook(), new UserPrefs());
        expectedModel.setTuitionBook(new TuitionBook());

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_filteredTuitionBook_clearsHiddenContactsAndReportsScope() throws Exception {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("90000001")
                .withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Sam Student").withPhone("90000002")
                .withRole(Role.STUDENT).build();
        Model model = new ModelManager(new TuitionBook(), new UserPrefs());
        model.addPerson(guardian);
        model.addPerson(student);
        model.updateFilteredPersonList(person -> person.getRole() == Role.STUDENT, "list r/student");

        CommandResult result = new ClearCommand().execute(model);

        assertEquals(ClearCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertTrue(result.getFeedbackToUser().contains("hidden by the current filter"));
        assertTrue(model.getTuitionBook().getPersonList().isEmpty());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

}
