package seedu.address.logic.commands;

import org.junit.jupiter.api.Test;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;

public class RemarkCommandTest {
    private Model model = new ModelManager();

    @Test
    public void execute_remarkCommand_withNullIndexAndNullRemark_throwsIllegalArgumentException() {
        RemarkCommand remarkCommand = new RemarkCommand(null, null);
        assertCommandFailure(remarkCommand, model, "Index: 1, Remark: null");
    }

}
