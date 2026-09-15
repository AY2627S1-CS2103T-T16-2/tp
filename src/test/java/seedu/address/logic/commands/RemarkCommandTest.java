package seedu.address.logic.commands;

import org.junit.jupiter.api.Test;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;

public class RemarkCommandTest {
    private Model model = new ModelManager();

    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        assertCommandFailure(new RemarkCommand(), model, RemarkCommand.MESSAGE_NOT_IMPLEMENTED_YET);
    }

}
