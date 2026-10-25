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
    public void execute_linkedStudent_showsGuardianNameOutsideFilteredList() throws CommandException {
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
                + "Tags: [friends], [math]\n\n"
                + "Guardian: Grace Tan (91234567)";
        assertViewWithoutMutation(model, FIRST_INDEX, expected);
    }

    @Test
    public void execute_verboseLinkedStudent_showsGuardianDetailsOutsideFilteredList() throws CommandException {
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
                + "Tags: [friends], [math]\n\n"
                + "Guardian details:\n"
                + "Name: Grace Tan\n"
                + "Role: GUARDIAN\n"
                + "Phone: 91234567\n"
                + "Email: amy@gmail.com\n"
                + "Address: 123, Jurong West Ave 6, #08-111\n"
                + "Tags: [family]";
        assertViewWithoutMutation(model, FIRST_INDEX, true, expected);
    }

    @Test
    public void execute_mixedCaseAndPunctuationTags_usesDisplayOrder() throws CommandException {
        Person student = new PersonBuilder().withName("Sam Tan").withRole(Role.STUDENT)
                .withTags("zebra", "Alpha", "alpha", "10", "!tag").build();
        Model model = modelWith(student);

        String expected = "Name: Sam Tan\n"
                + "Role: STUDENT\n"
                + "Phone: 85355255\n"
                + "Email: amy@gmail.com\n"
                + "Address: 123, Jurong West Ave 6, #08-111\n"
                + "Tags: [!tag], [10], [Alpha], [alpha], [zebra]\n\n"
                + ViewCommand.MESSAGE_NO_GUARDIAN;
        assertViewWithoutMutation(model, FIRST_INDEX, expected);
    }

    @Test
    public void execute_tagDisplay_usesBracketsAndEscapesAmbiguousCharacters() throws CommandException {
        List<List<String>> tagSets = List.of(
                List.<String>of(),
                List.of("-"),
                List.of("Math, Science"),
                List.of("["),
                List.of("]"),
                List.of("[Math]"),
                List.of("C:\\Math\\"),
                List.of("Math", "Science"),
                List.of("a\\b", "c[d]"),
                List.of("one,two", "three"));
        List<String> expectedTagsLines = List.of(
                "Tags: -",
                "Tags: [-]",
                "Tags: [Math, Science]",
                "Tags: [\\[]",
                "Tags: [\\]]",
                "Tags: [\\[Math\\]]",
                "Tags: [C:\\\\Math\\\\]",
                "Tags: [Math], [Science]",
                "Tags: [a\\\\b], [c\\[d\\]]",
                "Tags: [one,two], [three]");

        for (int index = 0; index < tagSets.size(); index++) {
            Person person = new PersonBuilder()
                    .withTags(tagSets.get(index).toArray(new String[0]))
                    .build();
            String details = new ViewCommand(FIRST_INDEX).execute(modelWith(person))
                    .getDetailsToShow().orElseThrow();
            String tagsLine = details.lines()
                    .filter(line -> line.startsWith("Tags: "))
                    .findFirst()
                    .orElseThrow();

            assertEquals(expectedTagsLines.get(index), tagsLine, "Unexpected tag display for case " + index);
        }
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
                + "- Zoe Tan (81234567)\n"
                + "- Amy Tan (82345678)";
        assertViewWithoutMutation(model, FIRST_INDEX, expected);
    }

    @Test
    public void execute_verboseGuardianWithMultipleStudents_showsDetailsInTuitionBookOrder()
            throws CommandException {
        Person guardian = new PersonBuilder().withName("Grace Tan").withRole(Role.GUARDIAN).build();
        Person firstStudent = new PersonBuilder().withName("Zoe Tan").withPhone("81234567")
                .withRole(Role.STUDENT).withGuardian(guardian).withoutEmail().withoutAddress()
                .withTags("science").build();
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
                + "Linked students:\n\n"
                + "Name: Zoe Tan\n"
                + "Role: STUDENT\n"
                + "Phone: 81234567\n"
                + "Email: -\n"
                + "Address: -\n"
                + "Tags: [science]\n\n"
                + "Name: Amy Tan\n"
                + "Role: STUDENT\n"
                + "Phone: 82345678\n"
                + "Email: amy@gmail.com\n"
                + "Address: 123, Jurong West Ave 6, #08-111\n"
                + "Tags: -";
        assertViewWithoutMutation(model, FIRST_INDEX, true, expected);
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

        String details = new ViewCommand(Index.fromOneBased(3), true)
                .execute(model).getDetailsToShow().orElseThrow();
        assertTrue(details.contains("Guardian details:\nName: Grace Tan\nRole: GUARDIAN\nPhone: 92345678"));
        assertFalse(details.contains("Phone: 91234567"));
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Person student = new PersonBuilder().withRole(Role.STUDENT).build();
        Person guardian = new PersonBuilder().withName("Grace Tan").withRole(Role.GUARDIAN).build();
        Model model = modelWith(student, guardian);
        model.updateFilteredPersonList(new PersonHasRolePredicate(Role.GUARDIAN));

        assertCommandFailure(new ViewCommand(SECOND_INDEX), model,
                Messages.getInvalidPersonDisplayedIndexMessage(model.getFilteredPersonList().size()));
        assertCommandFailure(new ViewCommand(FIRST_INDEX), modelWith(),
                Messages.getInvalidPersonDisplayedIndexMessage(0));
    }

    @Test
    public void equalsAndToString() {
        ViewCommand firstCommand = new ViewCommand(FIRST_INDEX);
        assertEquals(firstCommand, new ViewCommand(FIRST_INDEX));
        assertFalse(firstCommand.equals(new ViewCommand(SECOND_INDEX)));
        assertFalse(firstCommand.equals(new ViewCommand(FIRST_INDEX, true)));
        assertFalse(firstCommand.equals(null));
        assertEquals(ViewCommand.class.getCanonicalName() + "{targetIndex=" + FIRST_INDEX
                        + ", shouldShowFullLinkedDetails=false}",
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
        assertViewWithoutMutation(model, index, false, expected);
    }

    private static void assertViewWithoutMutation(Model model, Index index, boolean shouldShowFullLinkedDetails,
            String expected) throws CommandException {
        TuitionBook tuitionBookBefore = new TuitionBook(model.getTuitionBook());
        List<Person> displayedPersonsBefore = new ArrayList<>(model.getFilteredPersonList());

        CommandResult result = new ViewCommand(index, shouldShowFullLinkedDetails).execute(model);

        Person selectedPerson = displayedPersonsBefore.get(index.getZeroBased());
        assertEquals(String.format(ViewCommand.MESSAGE_SUCCESS, selectedPerson.getName()), result.getFeedbackToUser());
        assertEquals(expected, result.getDetailsToShow().orElseThrow());
        assertEquals(tuitionBookBefore, model.getTuitionBook());
        assertEquals(displayedPersonsBefore, model.getFilteredPersonList());
    }
}
