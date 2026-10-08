package seedu.tuitionbook.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.commons.core.index.Index;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.UserPrefs;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.testutil.PersonBuilder;

public class LinkCommandTest {
    @Test
    public void execute_linkReplaceAndRelink_success() throws Exception {
        Person student = new PersonBuilder().withName("Student").withPhone("90000001").build();
        Person oldGuardian = new PersonBuilder().withName("Old Guardian").withPhone("90000002")
                .withRole(Role.GUARDIAN).build();
        Person newGuardian = new PersonBuilder().withName("New Guardian").withPhone("90000003")
                .withRole(Role.GUARDIAN).build();
        ModelManager model = new ModelManager(new seedu.tuitionbook.model.TuitionBook(), new UserPrefs());
        model.addPerson(student);
        model.addPerson(oldGuardian);
        model.addPerson(newGuardian);

        new LinkCommand(Index.fromOneBased(1), Index.fromOneBased(2)).execute(model);
        assertEquals(oldGuardian.getId(), model.getFilteredPersonList().get(0).getGuardianId().orElseThrow());
        CommandResult result = new LinkCommand(Index.fromOneBased(1), Index.fromOneBased(3)).execute(model);
        assertTrue(result.getFeedbackToUser().contains("Previous guardian unlinked"));
        assertEquals(newGuardian.getId(), model.getFilteredPersonList().get(0).getGuardianId().orElseThrow());

        result = new LinkCommand(Index.fromOneBased(1), Index.fromOneBased(3)).execute(model);
        assertTrue(result.getFeedbackToUser().contains("already linked"));
    }
}
