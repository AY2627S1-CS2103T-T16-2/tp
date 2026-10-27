package seedu.tuitionbook.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.tuitionbook.testutil.Assert.assertThrows;
import static seedu.tuitionbook.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.tuitionbook.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.logic.Messages;
import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.model.tag.Tag;
import seedu.tuitionbook.testutil.PersonBuilder;

public class TagModificationCommandTest {
    @Test
    public void execute_tagFilteredStudent_addsOnlyNewTagsAndPreservesOtherData() throws Exception {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Sam Student").withGuardian(guardian)
                .withTags("Math").build();
        Model model = modelWith(guardian, student);
        model.updateFilteredPersonList(person -> person.getId().equals(student.getId()), "list r/student");
        TagCommand command = new TagCommand(INDEX_FIRST_PERSON, tags("Math", "Science", "math"));

        CommandResult result = command.execute(model);

        Person expected = new PersonBuilder(student).withTags("Math", "Science", "math").build();
        assertEquals("Added tags to Sam Student: \"Science\", \"math\".", result.getFeedbackToUser());
        assertEquals(List.of(expected), model.getFilteredPersonList());
        assertEquals(student.getId(), model.getFilteredPersonList().getFirst().getId());
        assertTrue(command.requiresSave());
    }

    @Test
    public void execute_untagFilteredGuardian_removesExactMatchesAndKeepsOtherTags() throws Exception {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withRole(Role.GUARDIAN)
                .withTags("Math", "math", "Sec 4").build();
        Person student = new PersonBuilder().withName("Sam Student").withGuardian(guardian).build();
        Model model = modelWith(student, guardian);
        model.updateFilteredPersonList(person -> person.getRole() == Role.GUARDIAN, "list r/guardian");
        UntagCommand command = new UntagCommand(INDEX_FIRST_PERSON, tags("Math", "absent"));

        CommandResult result = command.execute(model);

        Person expected = new PersonBuilder(guardian).withTags("math", "Sec 4").build();
        assertEquals(String.format(UntagCommand.MESSAGE_SUCCESS, guardian.getName())
                + " Tags not present: \"absent\".", result.getFeedbackToUser());
        assertEquals(List.of(expected), model.getFilteredPersonList());
        assertEquals(guardian.getId(), model.getFilteredPersonList().getFirst().getId());
        assertTrue(command.requiresSave());
    }

    @Test
    public void execute_noChange_preservesFilterAndDoesNotRequestSave() throws Exception {
        Person person = new PersonBuilder().withTags("Math").build();
        Model model = modelWith(person);
        Predicate<Person> filter = contact -> true;
        model.updateFilteredPersonList(filter, "list r/student");
        TagCommand tag = new TagCommand(INDEX_FIRST_PERSON, tags("Math"));
        UntagCommand untag = new UntagCommand(INDEX_FIRST_PERSON, tags("math"));

        assertEquals(String.format(TagCommand.MESSAGE_NO_CHANGE, person.getName()),
                tag.execute(model).getFeedbackToUser());
        assertEquals(String.format(UntagCommand.MESSAGE_NO_CHANGE, person.getName()) + " Tags not present: \"math\"."
                        + " Tag \"math\" not found, did you mean \"Math\"?",
                untag.execute(model).getFeedbackToUser());
        assertFalse(tag.requiresSave());
        assertFalse(untag.requiresSave());
        assertEquals(List.of(person), model.getTuitionBook().getPersonList());
        assertSame(filter, model.getPersonListQueryPredicate());
    }

    @Test
    public void execute_untagReportsMissingTagsInSortedOrderAndRecomputesOnReuse() throws Exception {
        Person person = new PersonBuilder().withTags("Math", "Science").build();
        Model model = modelWith(person);
        UntagCommand command = new UntagCommand(INDEX_FIRST_PERSON, tags("Zebra", "Math", "Absent"));

        assertEquals(String.format(UntagCommand.MESSAGE_SUCCESS, person.getName())
                + " Tags not present: \"Absent\", \"Zebra\".", command.execute(model).getFeedbackToUser());
        assertEquals(tags("Science"), model.getFilteredPersonList().getFirst().getTags());
        assertEquals(String.format(UntagCommand.MESSAGE_NO_CHANGE, person.getName())
                + " Tags not present: \"Absent\", \"Math\", \"Zebra\".", command.execute(model).getFeedbackToUser());
        assertFalse(command.requiresSave());
    }

    @Test
    public void execute_untagAllPresent_hasNoMissingTagWarning() throws Exception {
        Person person = new PersonBuilder().withTags("Math").build();
        Model model = modelWith(person);
        assertEquals(String.format(UntagCommand.MESSAGE_SUCCESS, person.getName()),
                new UntagCommand(INDEX_FIRST_PERSON, tags("Math")).execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_untagWrongCase_suggestsAllStoredVariantsInSortedOrder() throws Exception {
        Person person = new PersonBuilder().withName("Sam Student").withTags("Math", "MATH", "Science").build();
        Model model = modelWith(person);
        UntagCommand command = new UntagCommand(INDEX_FIRST_PERSON, tags("science", "math"));

        assertEquals("None of the specified tags are present on Sam Student. Tags not present: \"math\", \"science\"."
                + " Tag \"math\" not found, did you mean \"MATH\", \"Math\"?"
                + " Tag \"science\" not found, did you mean \"Science\"?",
                command.execute(model).getFeedbackToUser());
        assertEquals(List.of(person), model.getFilteredPersonList());
        assertFalse(command.requiresSave());
    }

    @Test
    public void execute_untagPartialRemoval_suggestsOnlyTagsStillPresent() throws Exception {
        Person person = new PersonBuilder().withName("Sam Student").withTags("Math", "SCIENCE").build();
        Model model = modelWith(person);
        UntagCommand command = new UntagCommand(INDEX_FIRST_PERSON, tags("Math", "math", "science"));

        assertEquals("Removed tags from Sam Student. Tags not present: \"math\", \"science\"."
                + " Tag \"science\" not found, did you mean \"SCIENCE\"?",
                command.execute(model).getFeedbackToUser());
        assertEquals(tags("SCIENCE"), model.getFilteredPersonList().getFirst().getTags());
        assertTrue(command.requiresSave());
    }

    @Test
    public void execute_untagWrongCase_escapesSuggestedSpellings() throws Exception {
        Person person = new PersonBuilder().withName("Sam Student").withTags("A\"B", "C\\D").build();
        Model model = modelWith(person);
        assertEquals("None of the specified tags are present on Sam Student. Tags not present: \"a\\\"b\", \"c\\\\d\"."
                + " Tag \"a\\\"b\" not found, did you mean \"A\\\"B\"?"
                + " Tag \"c\\\\d\" not found, did you mean \"C\\\\D\"?",
                new UntagCommand(INDEX_FIRST_PERSON, tags("a\"b", "c\\d")).execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_untagNoCaseMatch_doesNotSuggestOtherContactsOrDifferentSpacing() throws Exception {
        Person target = new PersonBuilder().withName("Sam Student").withTags("Maths", "Sec 4").build();
        Person other = new PersonBuilder().withName("Other Student").withTags("Math", "Sec  4").build();
        Model model = modelWith(target, other);
        assertEquals("None of the specified tags are present on Sam Student. Tags not present: \"math\", \"sec  4\".",
                new UntagCommand(INDEX_FIRST_PERSON, tags("math", "sec  4")).execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_untagReused_recomputesCaseSuggestions() throws Exception {
        Person person = new PersonBuilder().withTags("Math").build();
        Model model = modelWith(person);
        UntagCommand command = new UntagCommand(INDEX_FIRST_PERSON, tags("math"));
        assertTrue(command.execute(model).getFeedbackToUser().endsWith("did you mean \"Math\"?"));

        model.setPerson(person, new PersonBuilder(person).withTags("MATH").build());
        assertTrue(command.execute(model).getFeedbackToUser().endsWith("did you mean \"MATH\"?"));
        assertFalse(command.requiresSave());
    }

    @Test
    public void execute_missingTagPunctuation_isUnambiguousInFeedback() throws Exception {
        Person person = new PersonBuilder().withTags().build();
        Model model = modelWith(person);
        assertEquals(String.format(UntagCommand.MESSAGE_NO_CHANGE, person.getName())
                + " Tags not present: \"Math, Science\", \"a\\\"b\", \"c\\\\d\".",
                new UntagCommand(INDEX_FIRST_PERSON, tags("c\\d", "a\"b", "Math, Science"))
                        .execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_addedTagPunctuation_isUnambiguousInFeedback() throws Exception {
        Person person = new PersonBuilder().withName("Sam Student").withTags("Existing").build();
        Model model = modelWith(person);
        TagCommand command = new TagCommand(INDEX_FIRST_PERSON, tags("Existing", "c\\d", "a\"b", "Math, Science"));

        assertEquals("Added tags to Sam Student: \"Math, Science\", \"a\\\"b\", \"c\\\\d\".",
                command.execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_reusedCommands_resetSaveDecisionAndAllowRemovingLastTag() throws Exception {
        Model model = modelWith(new PersonBuilder().withTags().build());
        TagCommand tag = new TagCommand(INDEX_FIRST_PERSON, tags("Math"));
        UntagCommand untag = new UntagCommand(INDEX_FIRST_PERSON, tags("Math"));
        assertTrue(tag.canMutate());
        assertTrue(untag.canMutate());
        assertEquals(String.format(TagCommand.MESSAGE_SUCCESS,
                model.getFilteredPersonList().getFirst().getName(), "\"Math\""),
                tag.execute(model).getFeedbackToUser());
        assertTrue(tag.requiresSave());
        assertEquals(String.format(TagCommand.MESSAGE_NO_CHANGE,
                model.getFilteredPersonList().getFirst().getName()), tag.execute(model).getFeedbackToUser());
        assertFalse(tag.requiresSave());
        untag.execute(model);
        assertTrue(untag.requiresSave());
        assertTrue(model.getFilteredPersonList().getFirst().getTags().isEmpty());
        untag.execute(model);
        assertFalse(untag.requiresSave());
    }

    @Test
    public void execute_invalidDisplayedIndex_preservesModel() {
        Model model = modelWith(new PersonBuilder().build());
        assertCommandFailure(new TagCommand(INDEX_SECOND_PERSON, tags("Math")), model,
                Messages.getInvalidPersonDisplayedIndexMessage(1));
        model.updateFilteredPersonList(person -> false, "find Missing");
        assertCommandFailure(new UntagCommand(INDEX_FIRST_PERSON, tags("Math")), model,
                Messages.getInvalidPersonDisplayedIndexMessage(0));
    }

    @Test
    public void constructors_copyTagsAndRejectEmptySets() throws Exception {
        Set<Tag> supplied = new HashSet<>(tags("Math"));
        TagCommand command = new TagCommand(INDEX_FIRST_PERSON, supplied);
        supplied.clear();
        Model model = modelWith(new PersonBuilder().withTags().build());
        command.execute(model);
        assertEquals(tags("Math"), model.getFilteredPersonList().getFirst().getTags());
        assertThrows(IllegalArgumentException.class, () -> new TagCommand(INDEX_FIRST_PERSON, Set.of()));
        assertThrows(IllegalArgumentException.class, () -> new UntagCommand(INDEX_FIRST_PERSON, Set.of()));
    }

    @Test
    public void equals_distinguishesOperationIndexAndTags() {
        TagCommand tag = new TagCommand(INDEX_FIRST_PERSON, tags("Math"));
        assertEquals(tag, tag);
        assertEquals(tag, new TagCommand(INDEX_FIRST_PERSON, tags("Math")));
        assertEquals(new UntagCommand(INDEX_FIRST_PERSON, tags("Math")),
                new UntagCommand(INDEX_FIRST_PERSON, tags("Math")));
        assertNotEquals(tag, new UntagCommand(INDEX_FIRST_PERSON, tags("Math")));
        assertNotEquals(tag, new TagCommand(INDEX_SECOND_PERSON, tags("Math")));
        assertNotEquals(tag, new TagCommand(INDEX_FIRST_PERSON, tags("Science")));
        assertNotEquals(tag, null);
        assertNotEquals(tag, new ClearCommand());
        assertTrue(tag.toString().contains("tags"));
    }

    private static Model modelWith(Person... persons) {
        Model model = new ModelManager();
        for (Person person : persons) {
            model.addPerson(person);
        }
        return model;
    }

    private static Set<Tag> tags(String... names) {
        Set<Tag> tags = new HashSet<>();
        for (String name : names) {
            tags.add(new Tag(name));
        }
        return tags;
    }
}
