package seedu.tuitionbook.logic.commands;

import static seedu.tuitionbook.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.tuitionbook.testutil.TypicalPersons.getTypicalTuitionBook;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.UserPrefs;

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

}
