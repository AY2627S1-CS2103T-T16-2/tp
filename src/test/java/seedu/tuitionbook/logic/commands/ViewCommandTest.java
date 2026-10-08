package seedu.tuitionbook.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.assertCommandFailure;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.commons.core.index.Index;
import seedu.tuitionbook.logic.Messages;
import seedu.tuitionbook.logic.commands.exceptions.CommandException;
import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.PersonHasRolePredicate;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.testutil.PersonBuilder;

/**
 * Tests viewing a contact and its relationships through the model.
 */
public class ViewCommandTest {

    private static final Index FIRST_INDEX = Index.fromOneBased(1);
    private static final Index SECOND_INDEX = Index.fromOneBased(2);

    @Test
    public void execute_linkedStudent_showsGuardianOutsideFilteredList() throws CommandException {
        Person guardian = new PersonBuilder().withName("Grace Tan").withPhone("91234567")
                .withRole(Role.GUARDIAN).withTags("family").build();
        Person student = new PersonBuilder().withName("Sam Tan").withPhone("81234567")
                .withRole(Role.STUDENT).withGuardian(guardian).withoutEmail().withoutAddress()
                .withTags("math", "friends").build();
        Model model = modelWith(guardian, student);
        model.updateFilteredPersonList(new PersonHasRolePredicate(Role.STUDENT));

        String expected = "Name: Sam Tan\n"
                + "Role: STUDENT\n"
                + "Phone: 81234567\n"
                + "Email: -\n"
                + "Address: -\n"
                + "Tags: friends, math\n\n"
                + "Guardian details:\n"
                + "Name: Grace Tan\n"
                + "Role: GUARDIAN\n"
                + "Phone: 91234567\n"
                + "Email: amy@gmail.com\n"
                + "Address: 123, Jurong West Ave 6, #08-111\n"
                + "Tags: family";
        assertViewWithoutMutation(model, FIRST_INDEX, expected);
    }

    @Test
    public void execute_unlinkedStudent_showsNoGuardian() throws CommandException {
        Person student = new PersonBuilder().withName("Sam Tan").withRole(Role.STUDENT)
                .withoutEmail().withoutAddress().withTags().build();
        Model model = modelWith(student);

        String expected = "Name: Sam Tan\n"
                + "Role: STUDENT\n"
                + "Phone: 85355255\n"
                + "Email: -\n"
                + "Address: -\n"
                + "Tags: -\n\n"
                + ViewCommand.MESSAGE_NO_GUARDIAN;
        assertViewWithoutMutation(model, FIRST_INDEX, expected);
    }

    @Test
    public void execute_studentWithMissingGuardian_showsNoGuardian() throws CommandException {
        Person missingGuardian = new PersonBuilder().withName("Missing Guardian").withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Sam Tan").withRole(Role.STUDENT)
                .withGuardian(missingGuardian).build();
        Model model = modelWith(student);

        String expected = "Name: Sam Tan\n"
                + "Role: STUDENT\n"
                + "Phone: 85355255\n"
                + "Email: amy@gmail.com\n"
                + "Address: 123, Jurong West Ave 6, #08-111\n"
                + "Tags: -\n\n"
                + ViewCommand.MESSAGE_NO_GUARDIAN;
        assertViewWithoutMutation(model, FIRST_INDEX, expected);
    }

    @Test
    public void execute_guardianWithMultipleStudents_showsNamesInTuitionBookOrder() throws CommandException {
        Person guardian = new PersonBuilder().withName("Grace Tan").withRole(Role.GUARDIAN).build();
        Person firstStudent = new PersonBuilder().withName("Zoe Tan").withPhone("81234567")
                .withRole(Role.STUDENT).withGuardian(guardian).build();
        Person secondStudent = new PersonBuilder().withName("Amy Tan").withPhone("82345678")
                .withRole(Role.STUDENT).withGuardian(guardian).build();
        Model model = modelWith(firstStudent, guardian, secondStudent);
        model.updateFilteredPersonList(new PersonHasRolePredicate(Role.GUARDIAN));

        String expected = "Name: Grace Tan\n"
                + "Role: GUARDIAN\n"
                + "Phone: 85355255\n"
                + "Email: amy@gmail.com\n"
                + "Address: 123, Jurong West Ave 6, #08-111\n"
                + "Tags: -\n\n"
                + "Linked students:\n"
                + "- Zoe Tan\n"
                + "- Amy Tan";
        assertViewWithoutMutation(model, FIRST_INDEX, expected);
    }

    @Test
    public void execute_guardianWithNoStudents_showsNoStudents() throws CommandException {
        Person guardian = new PersonBuilder().withRole(Role.GUARDIAN).build();
        Model model = modelWith(guardian);

        String expected = "Name: Amy Bee\n"
                + "Role: GUARDIAN\n"
                + "Phone: 85355255\n"
                + "Email: amy@gmail.com\n"
                + "Address: 123, Jurong West Ave 6, #08-111\n"
                + "Tags: -\n\n"
                + ViewCommand.MESSAGE_NO_STUDENTS;
        assertViewWithoutMutation(model, FIRST_INDEX, expected);
    }

    @Test
    public void execute_guardiansWithSameName_resolvesById() throws CommandException {
        Person firstGuardian = new PersonBuilder().withName("Grace Tan").withPhone("91234567")
                .withRole(Role.GUARDIAN).build();
        Person secondGuardian = new PersonBuilder().withName("Grace Tan").withPhone("92345678")
                .withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Sam Tan").withRole(Role.STUDENT)
                .withGuardian(secondGuardian).build();
        Model model = modelWith(firstGuardian, secondGuardian, student);

        String details = new ViewCommand(Index.fromOneBased(3)).execute(model).getDetailsToShow().orElseThrow();
        assertTrue(details.contains("Guardian details:\nName: Grace Tan\nRole: GUARDIAN\nPhone: 92345678"));
        assertFalse(details.contains("Phone: 91234567"));
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Person student = new PersonBuilder().withRole(Role.STUDENT).build();
        Person guardian = new PersonBuilder().withName("Grace Tan").withRole(Role.GUARDIAN).build();
        Model model = modelWith(student, guardian);
        model.updateFilteredPersonList(new PersonHasRolePredicate(Role.GUARDIAN));

        assertCommandFailure(new ViewCommand(SECOND_INDEX), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandFailure(new ViewCommand(FIRST_INDEX), modelWith(),
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equalsAndToString() {
        ViewCommand firstCommand = new ViewCommand(FIRST_INDEX);
        assertEquals(firstCommand, new ViewCommand(FIRST_INDEX));
        assertFalse(firstCommand.equals(new ViewCommand(SECOND_INDEX)));
        assertFalse(firstCommand.equals(null));
        assertEquals(ViewCommand.class.getCanonicalName() + "{targetIndex=" + FIRST_INDEX + "}",
                firstCommand.toString());
    }

    private static Model modelWith(Person... persons) {
        Model model = new ModelManager();
        for (Person person : persons) {
            model.addPerson(person);
        }
        return model;
    }

    private static void assertViewWithoutMutation(Model model, Index index, String expected) throws CommandException {
        TuitionBook tuitionBookBefore = new TuitionBook(model.getTuitionBook());
        List<Person> displayedPersonsBefore = new ArrayList<>(model.getFilteredPersonList());

        CommandResult result = new ViewCommand(index).execute(model);

        Person selectedPerson = displayedPersonsBefore.get(index.getZeroBased());
        assertEquals(String.format(ViewCommand.MESSAGE_SUCCESS, selectedPerson.getName()), result.getFeedbackToUser());
        assertEquals(expected, result.getDetailsToShow().orElseThrow());
        assertEquals(tuitionBookBefore, model.getTuitionBook());
        assertEquals(displayedPersonsBefore, model.getFilteredPersonList());
    }
}
