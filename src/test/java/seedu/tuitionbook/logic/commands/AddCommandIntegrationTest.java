package seedu.tuitionbook.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.tuitionbook.testutil.TypicalPersons.getTypicalTuitionBook;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.tuitionbook.logic.Messages;
import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.UserPrefs;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalTuitionBook(), new UserPrefs());
    }

    @Test
    public void execute_newPerson_success() {
        Person validPerson = new PersonBuilder().build();

        Model expectedModel = new ModelManager(model.getTuitionBook(), new UserPrefs());
        expectedModel.addPerson(validPerson);

        assertCommandSuccess(new AddCommand(validPerson), model,
                new CommandResult(String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(validPerson)),
                        validPerson.getId()), expectedModel);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Person personInList = model.getTuitionBook().getPersonList().get(0);
        assertCommandFailure(new AddCommand(personInList), model,
                AddCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_sameNormalizedIdentityInDifferentRoles_bothAdditionsSucceed() throws Exception {
        model = new ModelManager();
        Person student = new PersonBuilder().withRole(Role.STUDENT).withName("Jos\u00e9 Tan")
                .withPhone("+65 9123-4567").build();
        Person guardian = new PersonBuilder(student).withId(UUID.randomUUID()).withRole(Role.GUARDIAN)
                .withName("JOSE\u0301  TAN ").withPhone("+65 91234567").build();

        new AddCommand(student).execute(model);
        new AddCommand(guardian).execute(model);

        assertEquals(List.of(student, guardian), model.getTuitionBook().getPersonList());
    }

}
