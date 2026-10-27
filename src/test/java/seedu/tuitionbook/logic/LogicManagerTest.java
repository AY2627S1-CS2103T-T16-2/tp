package seedu.tuitionbook.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static seedu.tuitionbook.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.tuitionbook.logic.Messages.getInvalidPersonDisplayedIndexMessage;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.ROLE_DESC_STUDENT;
import static seedu.tuitionbook.testutil.Assert.assertThrows;
import static seedu.tuitionbook.testutil.TypicalPersons.AMY;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import seedu.tuitionbook.commons.exceptions.DataLoadingException;
import seedu.tuitionbook.logic.commands.AddCommand;
import seedu.tuitionbook.logic.commands.ClearCommand;
import seedu.tuitionbook.logic.commands.CommandResult;
import seedu.tuitionbook.logic.commands.ExitCommand;
import seedu.tuitionbook.logic.commands.HelpCommand;
import seedu.tuitionbook.logic.commands.LinkCommand;
import seedu.tuitionbook.logic.commands.ListCommand;
import seedu.tuitionbook.logic.commands.TagCommand;
import seedu.tuitionbook.logic.commands.UnlinkCommand;
import seedu.tuitionbook.logic.commands.UntagCommand;
import seedu.tuitionbook.logic.commands.ViewCommand;
import seedu.tuitionbook.logic.commands.exceptions.CommandException;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;
import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.ReadOnlyTuitionBook;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.UserPrefs;
import seedu.tuitionbook.model.person.Address;
import seedu.tuitionbook.model.person.Name;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.model.tag.Tag;
import seedu.tuitionbook.storage.JsonTuitionBookStorage;
import seedu.tuitionbook.storage.JsonUserPrefsStorage;
import seedu.tuitionbook.storage.StorageManager;
import seedu.tuitionbook.storage.TuitionBookLoadResult;
import seedu.tuitionbook.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");
    private static final String MALFORMED_TUITION_BOOK_JSON = "{broken";

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonTuitionBookStorage tuitionBookStorage =
                new JsonTuitionBookStorage(temporaryFolder.resolve("tuitionBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(tuitionBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_mixedCaseCommands_preserveValuesAndPersistChanges() throws Exception {
        logic.execute("AdD r/student n/Case Sensitive p/91234567 a/Blk\u00a012\u3000Road"
                + " t/Math t/\"call p/Next Week\"");
        Person person = model.getTuitionBook().getPersonList().getFirst();
        assertEquals(new Name("Case Sensitive"), person.getName());
        assertEquals(new Address("Blk\u00a012\u3000Road"), person.getAddress().orElseThrow());
        assertEquals(Set.of(new Tag("Math"), new Tag("call p/Next Week")), person.getTags());
        logic.execute("EdIt 1 p/92345678");
        logic.execute("TaG 1 t/UPPER");
        Person updated = model.getTuitionBook().getPersonList().getFirst();
        logic.execute("ReLoAd");
        assertEquals(updated, model.getTuitionBook().getPersonList().getFirst());
        assertEquals(person.getId(), model.getTuitionBook().getPersonList().getFirst().getId());
        assertEquals(HelpCommand.SHOWING_HELP_MESSAGE, logic.execute("HeLp").getFeedbackToUser());
    }

    @Test
    public void execute_rejectedCommandSyntax_doesNotSaveOrChangeContactsAndFilter() throws Exception {
        logic.execute("add r/student n/Amy p/91234567 t/Math");
        logic.execute("add r/guardian n/Guardian p/98765432");
        logic.execute("find Amy");
        TuitionBook original = new TuitionBook(model.getTuitionBook());
        Predicate<Person> filter = model.getPersonListQueryPredicate();
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());
        Path file = temporaryFolder.resolve("tuitionBook.json");
        byte[] originalBytes = Files.readAllBytes(file);
        CountingTuitionBookStorage fileStorage = new CountingTuitionBookStorage(file, DUMMY_AD_EXCEPTION);
        logic = new LogicManager(model, new StorageManager(fileStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("SyntaxPrefs.json"))));

        for (String input : List.of("help extra", "exit extra", "reload extra", "HELP extra", "EXIT extra",
                "RELOAD extra", "CLEAR CONFIRM", "ADD R/student n/Bad p/123", "delete\u00a01",
                "delete 1\u3000", "edit 1\u00a0 n/Changed", "link 1\u30002", "clear confirm\u2028",
                "edit 1 n/Changed\u2029", "\nclear confirm", "edit 1 r/guardian", "edit 1 g/2",
                "edit 1 n/Changed r/guardian g/2")) {
            assertThrows(ParseException.class, () -> logic.execute(input));
            assertEquals(original, model.getTuitionBook());
            assertEquals(original.getPersonList().stream().map(Person::getId).toList(),
                    model.getTuitionBook().getPersonList().stream().map(Person::getId).toList());
            assertSame(filter, model.getPersonListQueryPredicate());
            assertEquals(displayed, model.getFilteredPersonList());
            assertArrayEquals(originalBytes, Files.readAllBytes(file));
        }
        assertEquals(0, fileStorage.getSaveCallCount());
    }

    @Test
    public void execute_unknownPrefixes_rejectWithoutChangingBookFilterOrFile() throws Exception {
        logic.execute("add r/student n/Amy p/91234567 t/Math");
        logic.execute("find Amy");
        TuitionBook original = new TuitionBook(model.getTuitionBook());
        Predicate<Person> filter = model.getPersonListQueryPredicate();
        Path file = temporaryFolder.resolve("tuitionBook.json");
        byte[] bytes = Files.readAllBytes(file);
        for (String input : List.of("add r/student n/Beth p/92345678 a/12 Road ee/beth@example.com",
                "edit 1 a/12 Road ee/amy@example.com", "tag 1 t/Math tt/Science",
                "untag 1 t/Math tt/Science", "list t/Math tt/Science")) {
            assertThrows(ParseException.class, () -> logic.execute(input));
            assertEquals(original, model.getTuitionBook());
            assertSame(filter, model.getPersonListQueryPredicate());
            assertEquals(original.getPersonList(), model.getFilteredPersonList());
            assertArrayEquals(bytes, Files.readAllBytes(file));
        }
    }

    @Test
    public void execute_quotedAddressAndLiteralPrefixes_persistWithoutChangingIdentity() throws Exception {
        logic.execute("add r/student n/Amy p/91234567 a/\"20 Main Road, c/o Rina a/p Kaur\""
                + " t/\"Advanced Math/Science\"");
        Person original = model.getTuitionBook().getPersonList().getFirst();
        assertEquals(new Address("20 Main Road, c/o Rina a/p Kaur"), original.getAddress().orElseThrow());
        logic.execute("edit 1 a/\"Unit \\\"A\\\" C:\\\\Road\\\\ ee/literal\"");
        Person updated = model.getTuitionBook().getPersonList().getFirst();
        assertEquals(new Address("Unit \"A\" C:\\Road\\ ee/literal"), updated.getAddress().orElseThrow());
        logic.execute("reload");
        assertEquals(updated, model.getTuitionBook().getPersonList().getFirst());
        assertEquals(original.getId(), model.getTuitionBook().getPersonList().getFirst().getId());
        logic.execute("list t/\"Advanced Math/Science\"");
        assertEquals(List.of(updated), model.getFilteredPersonList());
        logic.execute("edit 1 a/\"\"");
        logic.execute("reload");
        assertEquals(new PersonBuilder(updated).withoutAddress().build(),
                model.getTuitionBook().getPersonList().getFirst());
        assertEquals(original.getId(), model.getTuitionBook().getPersonList().getFirst().getId());
    }

    @Test
    public void execute_addAndEdit_preservesSpacesPunctuationAndCase() throws Exception {
        logic.execute("add r/student n/Alex Lim p/91234567 t/ Sec  4  t/Math--Science t/-active- t/C++ t/Mrs. Lim's");
        assertEquals(Set.of(new Tag("Sec  4"), new Tag("Math--Science"), new Tag("-active-"),
                new Tag("C++"), new Tag("Mrs. Lim's")), model.getTuitionBook().getPersonList().getFirst().getTags());

        logic.execute("edit 1 t/Math/Science t/!@#$%^&*()_+={}[]:;\"'<>,.?/\\|~` t/Math/Science");
        assertEquals(Set.of(new Tag("Math/Science"), new Tag("!@#$%^&*()_+={}[]:;\"'<>,.?/\\|~`")),
                model.getTuitionBook().getPersonList().getFirst().getTags());
        logic.execute("edit 1 t/");
        assertTrue(model.getTuitionBook().getPersonList().getFirst().getTags().isEmpty());
    }

    @Test
    public void execute_paddedQuotedTags_normalizesAcrossCommandsAndPreservesCaseIdentity() throws Exception {
        logic.execute("add r/student n/Alex Lim p/91234567 t/\" Math \"");
        assertEquals(Set.of(new Tag("Math")), logic.getPersonList().getFirst().getTags());

        logic.execute("tag 1 t/\" math \"");
        assertEquals(Set.of(new Tag("Math"), new Tag("math")), logic.getPersonList().getFirst().getTags());

        logic.execute("list t/\" MATH \"");
        assertEquals(1, logic.getFilteredPersonList().size());

        logic.execute("edit 1 t/\" Science \"");
        assertEquals(Set.of(new Tag("Science")), logic.getPersonList().getFirst().getTags());

        logic.execute("untag 1 t/\" Science \"");
        assertTrue(logic.getPersonList().getFirst().getTags().isEmpty());
    }

    @Test
    public void execute_invalidTags_preservesContactsAndFilter() throws Exception {
        logic.execute("add r/student n/Alex Lim p/91234567 t/Sec 4");
        logic.execute("list t/missing");
        TuitionBook original = new TuitionBook(model.getTuitionBook());
        for (String invalid : List.of("bad\ttag", "bad\u007ftag", "bad\u00a0tag", "bad\u0000tag")) {
            assertThrows(ParseException.class, () -> logic.execute("add r/student n/Beth Tan p/92345678 t/" + invalid));
            assertThrows(ParseException.class, () -> logic.execute("edit 1 t/" + invalid));
            assertThrows(ParseException.class, () -> logic.execute("list t/" + invalid));
            assertEquals(original, model.getTuitionBook());
            assertTrue(model.getFilteredPersonList().isEmpty());
        }
        assertThrows(ParseException.class, () -> logic.execute("add r/student n/Beth Tan p/92345678 t/   "));
        assertThrows(ParseException.class, () -> logic.execute("list t/   "));
    }

    @Test
    public void execute_longTagsAndManyDuplicates_preservesValues() throws Exception {
        String longTag = "Math -- Science! ".repeat(1000) + "end";
        logic.execute("add r/student n/Alex Lim p/91234567 t/" + longTag + " t/C++".repeat(200));
        assertEquals(Set.of(new Tag(longTag), new Tag("C++")),
                model.getTuitionBook().getPersonList().getFirst().getTags());
        assertEquals("1 person(s) listed!",
                logic.execute("list t/" + longTag + " t/c++".repeat(200)).getFeedbackToUser());
        logic.execute("edit 1 t/" + longTag);
        assertEquals(Set.of(new Tag(longTag)), model.getTuitionBook().getPersonList().getFirst().getTags());
    }

    @Test
    public void execute_tagAndUntag_persistIncrementalChangesAndKeepReplacementEditing() throws Exception {
        logic.execute("add r/guardian n/Grace Guardian p/90000001");
        logic.execute("add r/student n/Sam Student p/90000002 e/sam@example.com a/123 Road t/Math t/Sec 4");
        logic.execute("link 2 1");
        Person original = logic.getPersonList().get(1);
        logic.execute("find Sam");

        assertEquals("Added tags to Sam Student: \"Science\".",
                logic.execute("tag 1 t/Science t/Math t/Science").getFeedbackToUser());
        Person expected = new PersonBuilder(original).withTags("Math", "Sec 4", "Science").build();
        assertEquals(expected, logic.getPersonList().get(1));
        assertEquals(1, logic.getFilteredPersonList().size());
        logic.execute("reload");
        assertEquals(expected, logic.getPersonList().get(1));
        assertEquals(original.getId(), logic.getPersonList().get(1).getId());

        logic.execute("list r/student");
        assertEquals(String.format(UntagCommand.MESSAGE_SUCCESS, original.getName())
                + " Tags not present: \"absent\".", logic.execute("untag 1 t/Math t/absent").getFeedbackToUser());
        expected = new PersonBuilder(original).withTags("Sec 4", "Science").build();
        logic.execute("reload");
        assertEquals(expected, logic.getPersonList().get(1));
        assertEquals(original.getId(), logic.getPersonList().get(1).getId());

        logic.execute("edit 2 t/Only");
        logic.execute("reload");
        assertEquals(Set.of(new Tag("Only")), logic.getPersonList().get(1).getTags());
        logic.execute("untag 2 t/Only");
        logic.execute("reload");
        assertEquals(new PersonBuilder(original).withTags().build(), logic.getPersonList().get(1));
    }

    @Test
    public void execute_quotedTagAndUntag_preserveContactAndPersistDecodedTags() throws Exception {
        logic.execute("add r/guardian n/Grace Guardian p/90000001");
        logic.execute("add r/student n/Sam Student p/90000002 e/sam@example.com a/123 Road t/Math");
        logic.execute("link 2 1");
        Person original = logic.getPersonList().get(1);
        String arguments = " t/\"study t/weekly r/student\" t/\"\\\"Sec 4\\\"\" t/\"C:\\\\Math\\\\\"";
        logic.execute("find Sam");

        logic.execute("tag 1" + arguments);
        Person expected = new PersonBuilder(original)
                .withTags("Math", "study t/weekly r/student", "\"Sec 4\"", "C:\\Math\\").build();
        assertEquals(expected, logic.getPersonList().get(1));
        assertEquals(1, logic.getFilteredPersonList().size());
        logic.execute("reload");
        assertEquals(expected, logic.getPersonList().get(1));
        assertEquals(original.getId(), logic.getPersonList().get(1).getId());
        logic.execute("list" + arguments);
        assertEquals(List.of(expected), logic.getFilteredPersonList());

        logic.execute("untag 1" + arguments);
        assertEquals(1, logic.getFilteredPersonList().size());
        logic.execute("reload");
        assertEquals(original, logic.getPersonList().get(1));
        assertEquals(original.getId(), logic.getPersonList().get(1).getId());
    }

    @Test
    public void execute_tagAndUntagNoChanges_doNotSaveOrClearFilter() throws Exception {
        Person student = new PersonBuilder().withTags("Math").build();
        model.addPerson(student);
        model.addPerson(new PersonBuilder().withName("Hidden Guardian").withRole(Role.GUARDIAN).build());
        Predicate<Person> filter = person -> person.getRole() == Role.STUDENT;
        model.updateFilteredPersonList(filter, "list r/student");
        CountingTuitionBookStorage fileStorage = new CountingTuitionBookStorage(
                temporaryFolder.resolve("NoOpTags.json"), DUMMY_AD_EXCEPTION);
        logic = new LogicManager(model, new StorageManager(fileStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("NoOpTagsPrefs.json"))));

        assertEquals(String.format(TagCommand.MESSAGE_NO_CHANGE, student.getName()),
                logic.execute("tag 1 t/Math t/Math").getFeedbackToUser());
        assertEquals(String.format(UntagCommand.MESSAGE_NO_CHANGE, student.getName())
                + " Tags not present: \"absent\", \"math\"."
                + " Tag \"math\" not found, did you mean \"Math\"?",
                logic.execute("untag 1 t/math t/absent").getFeedbackToUser());
        assertEquals(0, fileStorage.getSaveCallCount());
        assertEquals(List.of(student), logic.getFilteredPersonList());
        assertSame(filter, model.getPersonListQueryPredicate());
    }

    @Test
    public void execute_tagAndUntagSaveFailure_restoreContactsRelationshipsAndFilter() throws Exception {
        logic.execute("add r/guardian n/Grace Guardian p/90000001");
        logic.execute("add r/student n/Sam Student p/90000002 t/\"study t/weekly\"");
        logic.execute("link 2 1");
        Path file = temporaryFolder.resolve("tuitionBook.json");
        byte[] originalBytes = Files.readAllBytes(file);
        TuitionBook original = new TuitionBook(model.getTuitionBook());
        Predicate<Person> filter = person -> person.getRole() == Role.STUDENT;
        model.updateFilteredPersonList(filter, "list r/student");

        for (IOException failure : List.of(DUMMY_IO_EXCEPTION, DUMMY_AD_EXCEPTION)) {
            CountingTuitionBookStorage fileStorage = new CountingTuitionBookStorage(file, failure);
            Logic failingLogic = new LogicManager(model, new StorageManager(fileStorage,
                    new JsonUserPrefsStorage(temporaryFolder.resolve("FailedTagsPrefs.json"))));
            for (String command : List.of("tag 1 t/\"call p/next week\"", "untag 1 t/\"study t/weekly\"")) {
                assertThrows(CommandException.class, () -> failingLogic.execute(command));
                assertEquals(original, model.getTuitionBook());
                assertEquals(original.getPersonList().stream().map(Person::getId).toList(),
                        failingLogic.getPersonList().stream().map(Person::getId).toList());
                assertSame(filter, model.getPersonListQueryPredicate());
                assertEquals(List.of(original.getPersonList().get(1)), failingLogic.getFilteredPersonList());
                assertArrayEquals(originalBytes, Files.readAllBytes(file));
            }
            assertEquals(2, fileStorage.getSaveCallCount());
        }
    }

    @Test
    public void execute_invalidIncrementalTagInput_doesNotPartiallyModifyOrSave() throws Exception {
        logic.execute("add r/student n/Sam Student p/90000002 t/Math");
        Person original = logic.getPersonList().getFirst();
        Path file = temporaryFolder.resolve("tuitionBook.json");
        byte[] originalBytes = Files.readAllBytes(file);
        for (String command : List.of("tag 1 t/Science t/", "untag 1 t/Math t/",
                "tag 1 t/Science t/bad\ttag", "untag 1 t/Math t/caf\u00e9",
                "tag 1 t/Science t/\"unfinished", "untag 1 t/Math t/\"unfinished",
                "tag 1 t/Science t/\"extra\" text", "untag 1 t/Math t/\"extra\" text",
                "tag 1 t/Science t/\"   \"", "untag 1 t/Math t/\"\"")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(List.of(original), logic.getPersonList());
            assertArrayEquals(originalBytes, Files.readAllBytes(file));
        }
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, getInvalidPersonDisplayedIndexMessage(0));
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_overLengthEdit_throwsParseExceptionAndLeavesContactUnchanged() {
        Person originalPerson = new PersonBuilder().withName("Original Name").withAddress("Original Address").build();
        model.addPerson(originalPerson);

        assertParseException("edit 1 n/" + "A".repeat(Name.MAX_LENGTH + 1), Name.MESSAGE_CONSTRAINTS);
        assertContactIsUnchanged(originalPerson);

        assertParseException("edit 1 a/" + "A".repeat(Address.MAX_LENGTH + 1), Address.MESSAGE_CONSTRAINTS);
        assertContactIsUnchanged(originalPerson);
    }

    private void assertContactIsUnchanged(Person originalPerson) {
        assertEquals(List.of(originalPerson), logic.getPersonList());
        assertEquals(originalPerson.getId(), logic.getPersonList().getFirst().getId());
    }

    @Test
    public void execute_add_savesContactData() throws Exception {
        String addCommand = AddCommand.COMMAND_WORD + ROLE_DESC_STUDENT + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        Person expectedPerson = new PersonBuilder(AMY).withTags().build();
        CommandResult result = logic.execute(addCommand);
        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(expectedPerson)),
                result.getFeedbackToUser());
        assertEquals(List.of(expectedPerson), model.getTuitionBook().getPersonList());
        assertEquals(List.of(expectedPerson), model.getFilteredPersonList());
        Path dataFilePath = temporaryFolder.resolve("tuitionBook.json");
        ReadOnlyTuitionBook savedBook = new JsonTuitionBookStorage(dataFilePath).readTuitionBook().orElseThrow();
        assertEquals(model.getTuitionBook(), savedBook);
        // The parser generates the UUID; verify that the same identity is displayed and persisted.
        assertEquals(model.getPersonListViewState().personIds(),
                savedBook.getPersonList().stream().map(Person::getId).toList());
        assertEquals("list", model.getPersonListViewState().refreshCommand());
    }

    @Test
    public void execute_quotedNames_preservesNamesAndRelationshipsAfterReload() throws Exception {
        logic.execute("add r/student n/\"Rina a/p Kaur\" p/91234567 a/123 Main Street");
        Person savedStudent = new JsonTuitionBookStorage(temporaryFolder.resolve("tuitionBook.json"))
                .readTuitionBook().orElseThrow().getPersonList().get(0);
        assertEquals("Rina a/p Kaur", savedStudent.getName().fullName);
        logic.execute("add r/guardian n/Guardian p/98765432");
        logic.execute("link 1 2");
        Person original = model.getTuitionBook().getPersonList().get(0);
        assertEquals("Rina a/p Kaur", original.getName().fullName);
        assertEquals("123 Main Street", original.getAddress().orElseThrow().value);

        logic.execute("edit 1 n/\"Rina s/o Kaur\"");
        ReadOnlyTuitionBook reloaded = new JsonTuitionBookStorage(temporaryFolder.resolve("tuitionBook.json"))
                .readTuitionBook().orElseThrow();
        Person restored = reloaded.getPersonList().get(0);
        assertEquals("Rina s/o Kaur", restored.getName().fullName);
        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getGuardianId(), restored.getGuardianId());
        assertEquals(original.getPhone(), restored.getPhone());
        assertEquals(original.getAddress(), restored.getAddress());
        assertEquals(2, reloaded.getPersonList().size());
    }

    @Test
    public void execute_editQuotedTags_preservesOtherFieldsAndPersistsCompleteTags() throws Exception {
        logic.execute("add r/student n/Sam Student p/91234567 e/sam@example.com a/123 Main Street t/Old");
        logic.execute("add r/guardian n/Grace Guardian p/98765432");
        logic.execute("link 1 2");
        Person originalStudent = logic.getPersonList().get(0);
        Person originalGuardian = logic.getPersonList().get(1);
        Person expectedStudent = new PersonBuilder(originalStudent)
                .withTags("meet at a/c room", "call p/next week").build();

        logic.execute("edit 1 t/\"meet at a/c room\" t/\"call p/next week\"");

        assertEquals(List.of(expectedStudent, originalGuardian), logic.getPersonList());
        Person savedStudent = new JsonTuitionBookStorage(temporaryFolder.resolve("tuitionBook.json"))
                .readTuitionBook().orElseThrow().getPersonList().get(0);
        assertEquals(Set.of(new Tag("meet at a/c room"), new Tag("call p/next week")), savedStudent.getTags());
        assertEquals(originalStudent.getPhone(), savedStudent.getPhone());
        assertEquals(originalStudent.getAddress(), savedStudent.getAddress());
        assertEquals(originalStudent.getId(), savedStudent.getId());
        assertEquals(expectedStudent, savedStudent);

        logic.execute("reload");

        assertEquals(List.of(expectedStudent, originalGuardian), logic.getPersonList());
        assertEquals(originalStudent.getId(), logic.getPersonList().get(0).getId());
        assertEquals(originalGuardian.getId(), logic.getPersonList().get(1).getId());
    }

    @Test
    public void execute_escapedTagQuotes_addEditFilterAndReloadPreserveLiteralValues() throws Exception {
        logic.execute("add r/student n/Sam Student p/91234567 t/\"\\\"Sec 4\\\"\""
                + " t/\"meet \\\"at a/c room\\\"\" t/\"C:\\\\Math\\\\\"");
        Person added = logic.getPersonList().getFirst();
        assertEquals(Set.of(new Tag("\"Sec 4\""), new Tag("meet \"at a/c room\""), new Tag("C:\\Math\\")),
                added.getTags());
        logic.execute("list t/\"\\\"Sec 4\\\"\"");
        assertEquals(List.of(added), logic.getFilteredPersonList());

        logic.execute("edit 1 t/\"\\\"Sec 4\\\"\" t/\"call \\\"p/next week\\\"\"");
        Person edited = logic.getPersonList().getFirst();
        assertEquals(Set.of(new Tag("\"Sec 4\""), new Tag("call \"p/next week\"")), edited.getTags());
        assertEquals(added.getPhone(), edited.getPhone());
        logic.execute("reload");
        logic.execute("list t/\"\\\"Sec 4\\\"\" t/\"call \\\"p/next week\\\"\"");
        assertEquals(List.of(edited), logic.getFilteredPersonList());
        assertEquals(added.getId(), logic.getFilteredPersonList().getFirst().getId());
    }

    @Test
    public void execute_filterLiteralQuotedTagLoadedFromJson_matchesWithoutChangingStoredValue() throws Exception {
        Person original = new PersonBuilder().withTags("\"Sec 4\"").build();
        TuitionBook saved = new TuitionBook();
        saved.addPerson(original);
        Path file = temporaryFolder.resolve("tuitionBook.json");
        new JsonTuitionBookStorage(file).saveTuitionBook(saved);
        byte[] originalBytes = Files.readAllBytes(file);

        logic.execute("reload");
        logic.execute("list t/\"\\\"Sec 4\\\"\"");

        assertEquals(List.of(original), logic.getFilteredPersonList());
        assertEquals(Set.of(new Tag("\"Sec 4\"")), logic.getFilteredPersonList().getFirst().getTags());
        assertArrayEquals(originalBytes, Files.readAllBytes(file));
    }

    @Test
    public void execute_invalidQuotedNames_leavesModelAndSavedDataUnchanged() throws Exception {
        logic.execute("add r/student n/Original p/91234567");
        logic.execute("add r/guardian n/Hidden Guardian p/90000001");
        Person hiddenGuardian = model.getTuitionBook().getPersonList().get(1);
        Person original = model.getTuitionBook().getPersonList().get(0);
        Path dataFile = temporaryFolder.resolve("tuitionBook.json");
        byte[] originalBytes = Files.readAllBytes(dataFile);
        model.updateFilteredPersonList(person -> person.getRole() == Role.STUDENT, "list r/student");
        assertEquals(List.of(original), model.getFilteredPersonList());
        for (String name : List.of("\"Rina a/p Kaur", "\"Rina\" extra", "\"@invalid\"", "\"Rina \\\"alias\\\"\"",
                "\"\"", "\"" + "A".repeat(Name.MAX_LENGTH + 1) + "\"",
                "\"Rina\" n/Other", "\"Rina\"p/98765432")) {
            for (String command : List.of("add r/student p/98765432 n/", "edit 1 n/")) {
                assertThrows(ParseException.class, () -> logic.execute(command + name));
                assertEquals(List.of(original, hiddenGuardian), logic.getPersonList());
                assertEquals(original.getId(), logic.getPersonList().get(0).getId());
                assertEquals(hiddenGuardian.getId(), logic.getPersonList().get(1).getId());
                assertEquals(List.of(original), model.getFilteredPersonList());
                assertArrayEquals(originalBytes, Files.readAllBytes(dataFile));
            }
        }
    }

    @Test
    public void execute_clearArguments_preserveDataUntilConfirmation() throws Exception {
        logic.execute("add r/student n/Sam Student p/90000002");
        logic.execute("add r/guardian n/Grace Guardian p/90000001");
        Person visibleStudent = model.getTuitionBook().getPersonList().get(0);
        Person hiddenGuardian = model.getTuitionBook().getPersonList().get(1);
        Path dataFile = temporaryFolder.resolve("tuitionBook.json");
        byte[] originalBytes = Files.readAllBytes(dataFile);
        model.updateFilteredPersonList(person -> person.getRole() == Role.STUDENT, "list r/student");

        List<Person> expectedPersons = List.of(visibleStudent, hiddenGuardian);
        List<Person> expectedFilteredPersons = List.of(visibleStudent);
        Predicate<Person> expectedFilter = model.getPersonListQueryPredicate();
        List<String> confirmationCommands = List.of(
                "clear", "clear   ", "clear CONFIRM", "clear Confirm", "clear confirm confirm",
                "clear confirm extra", "clear yes", "clear confirmno", "clear confirmation", "clear\tno");
        for (String command : confirmationCommands) {
            String expectedMessage = command.trim().equals("clear")
                    ? ClearCommand.MESSAGE_CONFIRMATION_REQUIRED
                    : ClearCommand.MESSAGE_INVALID_CONFIRMATION;
            try {
                logic.execute(command);
                throw new AssertionError("Expected clear confirmation to be rejected: " + command);
            } catch (ParseException exception) {
                assertEquals(expectedMessage, exception.getMessage());
            }
            assertEquals(expectedPersons, logic.getPersonList());
            assertEquals(expectedFilteredPersons, logic.getFilteredPersonList());
            assertSame(expectedFilter, model.getPersonListQueryPredicate());
            assertArrayEquals(originalBytes, Files.readAllBytes(dataFile));
        }

        assertEquals(ClearCommand.MESSAGE_SUCCESS, logic.execute("clear confirm").getFeedbackToUser());
        assertTrue(logic.getPersonList().isEmpty());
        assertTrue(logic.getFilteredPersonList().isEmpty());
        assertTrue(new JsonTuitionBookStorage(dataFile).readTuitionBook().orElseThrow().getPersonList().isEmpty());
    }

    @Test
    public void execute_alreadyLinkedPair_skipsSaveAndPreservesContactsAndFilter() throws Exception {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("90000001")
                .withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Sam Student").withPhone("90000002")
                .withGuardian(guardian).build();
        Person hidden = new PersonBuilder().withName("Hidden Student").withPhone("90000003").build();
        model.addPerson(guardian);
        model.addPerson(student);
        model.addPerson(hidden);
        Predicate<Person> filter = person -> !person.getId().equals(hidden.getId());
        model.updateFilteredPersonList(filter, "test-only UUID selection");
        CountingTuitionBookStorage fileStorage = new CountingTuitionBookStorage(
                temporaryFolder.resolve("NoOpLink.json"), DUMMY_AD_EXCEPTION);
        logic = new LogicManager(model, new StorageManager(fileStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("NoOpLinkPrefs.json"))));

        CommandResult result = logic.execute("link 2 1");

        assertEquals(String.format(LinkCommand.MESSAGE_ALREADY_LINKED, student.getName(), guardian.getName()),
                result.getFeedbackToUser());
        assertEquals(0, fileStorage.getSaveCallCount());
        assertEquals(List.of(guardian, student, hidden), logic.getPersonList());
        assertEquals(List.of(guardian, student), logic.getFilteredPersonList());
        assertSame(filter, model.getPersonListQueryPredicate());
    }

    @Test
    public void execute_unchangedEdits_skipSaveEvenWhenStorageIsUnwritable() throws Exception {
        String[] commands = {"edit 1 n/Sam Student", "edit 1 p/90000002", "edit 1 e/", "edit 1 a/",
            "edit 1 t/Science t/Math t/Math", "edit 1 n/  Sam Student  p/90000002 t/Math t/Science"};
        Person student = new PersonBuilder().withName("Sam Student").withPhone("90000002")
                .withoutEmail().withoutAddress().withTags("Math", "Science").build();
        Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("90000001")
                .withRole(Role.GUARDIAN).build();
        model.addPerson(student);
        model.addPerson(guardian);
        CountingTuitionBookStorage fileStorage = new CountingTuitionBookStorage(
                temporaryFolder.resolve("NoOpEdit.json"), DUMMY_AD_EXCEPTION);
        logic = new LogicManager(model, new StorageManager(fileStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("NoOpEditPrefs.json"))));

        for (String command : commands) {
            model.updateFilteredPersonList(person -> person.getRole() == Role.STUDENT, "list r/student");
            assertEquals(List.of(student), logic.getFilteredPersonList(), command);
            logic.execute(command);
            assertEquals(0, fileStorage.getSaveCallCount(), command);
            assertEquals(List.of(student, guardian), logic.getPersonList(), command);
            assertEquals(List.of(student), logic.getFilteredPersonList(), command);
        }
    }

    @Test
    public void execute_emptyTagEdit_skipsSave() throws Exception {
        Person student = new PersonBuilder().withTags().build();
        model.addPerson(student);
        CountingTuitionBookStorage fileStorage = new CountingTuitionBookStorage(
                temporaryFolder.resolve("EmptyChanges.json"), null);
        logic = new LogicManager(model, new StorageManager(fileStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("EmptyChangesPrefs.json"))));

        logic.execute("edit 1 t/");
        assertEquals(0, fileStorage.getSaveCallCount());
        assertEquals(List.of(student), logic.getPersonList());
        assertTrue(logic.getPersonList().getFirst().getTags().isEmpty());
    }

    @Test
    public void execute_clearThenEmptyClear_savesOnlyFirstClear() throws Exception {
        model.addPerson(new PersonBuilder().build());
        CountingTuitionBookStorage fileStorage = new CountingTuitionBookStorage(
                temporaryFolder.resolve("EmptyClear.json"), null);
        logic = new LogicManager(model, new StorageManager(fileStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("EmptyClearPrefs.json"))));

        logic.execute("clear confirm");
        assertEquals(1, fileStorage.getSaveCallCount());
        assertTrue(logic.getPersonList().isEmpty());
        assertTrue(fileStorage.readTuitionBook().orElseThrow().getPersonList().isEmpty());
        logic.execute("clear confirm");
        assertEquals(1, fileStorage.getSaveCallCount());
        assertTrue(fileStorage.readTuitionBook().orElseThrow().getPersonList().isEmpty());
    }

    @Test
    public void execute_changedEdit_preservesPersistentIdAndSaves() throws Exception {
        UUID originalId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Person original = new PersonBuilder().withName("Sam Student").withId(originalId).build();
        Model replacementModel = new ModelManager();
        replacementModel.addPerson(original);
        CountingTuitionBookStorage fileStorage = new CountingTuitionBookStorage(
                temporaryFolder.resolve("IdentityReplacement.json"), null);
        Logic replacementLogic = new LogicManager(replacementModel, new StorageManager(fileStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("IdentityReplacementPrefs.json"))));

        replacementLogic.execute("edit 1 n/Renamed Student");

        Person replacement = replacementLogic.getPersonList().getFirst();
        Person expected = new PersonBuilder(original).withName("Renamed Student").build();
        assertEquals(expected, replacement);
        assertEquals(originalId, replacement.getId());
        assertEquals(1, fileStorage.getSaveCallCount());
        Person reloaded = fileStorage.readTuitionBook().orElseThrow().getPersonList().getFirst();
        assertEquals(expected, reloaded);
        assertEquals(originalId, reloaded.getId());
    }

    @Test
    public void execute_changedContacts_savesEachChangeAndReloadsCorrectly() throws Exception {
        CountingTuitionBookStorage fileStorage = new CountingTuitionBookStorage(
                temporaryFolder.resolve("ChangedContacts.json"), null);
        logic = new LogicManager(model, new StorageManager(fileStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("ChangedContactsPrefs.json"))));
        String[] commands = {"add r/guardian n/Grace Guardian p/90000001",
            "add r/student n/Sam Student p/90000002 t/Math", "link 2 1", "edit 2 n/sam Student",
            "edit 2 p/+65 9000-0002", "edit 2 e/sam@example.com", "edit 2 a/1 Main Street",
            "edit 2 t/Math t/Science", "edit 2 t/Science", "edit 2 t/", "edit 2 e/ a/",
            "add r/guardian n/Other Guardian p/90000003", "link 2 3", "unlink 2", "delete 3", "clear confirm"};

        for (int i = 0; i < commands.length; i++) {
            logic.execute(commands[i]);
            assertEquals(i + 1, fileStorage.getSaveCallCount(), commands[i]);
            ReadOnlyTuitionBook reloaded = fileStorage.readTuitionBook().orElseThrow();
            assertEquals(model.getTuitionBook().getPersonList(), reloaded.getPersonList(), commands[i]);
            assertEquals(model.getTuitionBook().getPersonList().stream().map(Person::getId).toList(),
                    reloaded.getPersonList().stream().map(Person::getId).toList(), commands[i]);
        }
    }

    @Test
    public void execute_unlinkAlreadyUnlinkedStudent_skipsSaveAndPreservesContactsAndFilter() throws Exception {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("90000001")
                .withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Sam Student").withPhone("90000002")
                .withRole(Role.STUDENT).build();
        model.addPerson(student);
        model.addPerson(guardian);
        model.updateFilteredPersonList(person -> person.getRole() == Role.STUDENT, "list r/student");
        CountingTuitionBookStorage tuitionBookStorage = new CountingTuitionBookStorage(
                temporaryFolder.resolve("NoOpUnlink.json"), DUMMY_IO_EXCEPTION);
        logic = new LogicManager(model, new StorageManager(tuitionBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("NoOpUnlinkPrefs.json"))));

        CommandResult result = logic.execute(UnlinkCommand.COMMAND_WORD + " 1");

        assertEquals(String.format(UnlinkCommand.MESSAGE_NO_LINKED_GUARDIAN, student.getName()),
                result.getFeedbackToUser());
        assertEquals(0, tuitionBookStorage.getSaveCallCount());
        assertEquals(List.of(student, guardian), logic.getPersonList());
        assertEquals(List.of(student), logic.getFilteredPersonList());
    }

    @Test
    public void execute_unlink_savesUnlinkedStudentAndPreservesContactsAfterReload() throws Exception {
        Path dataFilePath = temporaryFolder.resolve("UnlinkRoundTrip.json");
        Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("90000001")
                .withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Sam Student").withPhone("90000002")
                .withRole(Role.STUDENT).withGuardian(guardian).build();
        model.addPerson(student);
        model.addPerson(guardian);
        JsonTuitionBookStorage tuitionBookStorage = new JsonTuitionBookStorage(dataFilePath);
        logic = new LogicManager(model, new StorageManager(tuitionBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("UnlinkRoundTripPrefs.json"))));

        logic.execute(UnlinkCommand.COMMAND_WORD + " 1");

        ReadOnlyTuitionBook reloadedTuitionBook = tuitionBookStorage.readTuitionBook().orElseThrow();
        Person reloadedStudent = reloadedTuitionBook.getPersonList().get(0);
        Person reloadedGuardian = reloadedTuitionBook.getPersonList().get(1);
        assertEquals(student.clearGuardian(), reloadedStudent);
        assertEquals(guardian, reloadedGuardian);
        assertEquals(student.getId(), reloadedStudent.getId());
        assertEquals(guardian.getId(), reloadedGuardian.getId());
        assertTrue(reloadedStudent.getGuardianId().isEmpty());
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_failedMutatingCommands_restoreContactsAndFilter() throws Exception {
        String[] commands = {"add r/student n/New Student p/90000003", "edit 1 n/Renamed Student", "delete 1",
            "clear confirm", "link 1 2"};
        for (String command : commands) {
            Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("90000001")
                    .withRole(Role.GUARDIAN).build();
            Person student = new PersonBuilder().withName("Sam Student").withPhone("90000002")
                    .withRole(Role.STUDENT).build();
            Model failedSaveModel = new ModelManager(new TuitionBook(), new UserPrefs());
            failedSaveModel.addPerson(student);
            failedSaveModel.addPerson(guardian);
            if (!command.startsWith("link")) {
                failedSaveModel.updateFilteredPersonList(person -> person.getRole() == Role.STUDENT, "list r/student");
            }
            Logic failedSaveLogic = new LogicManager(failedSaveModel, storageThatFailsOnSave(
                    temporaryFolder.resolve(command.split(" ")[0] + ".json")));

            assertThrows(CommandException.class, () -> failedSaveLogic.execute(command));

            List<Person> restoredPersons = failedSaveLogic.getPersonList();
            assertEquals(List.of(student, guardian), restoredPersons);
            assertEquals(student.getId(), restoredPersons.get(0).getId());
            assertEquals(guardian.getId(), restoredPersons.get(1).getId());
            assertEquals(command.startsWith("link") ? List.of(student, guardian) : List.of(student),
                    failedSaveLogic.getFilteredPersonList());
        }
    }

    @Test
    public void execute_failedDeleteOfLinkedGuardian_restoresContactsLinksFilterAndIds() throws Exception {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("90000001")
                .withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Sam Student").withPhone("90000002")
                .withRole(Role.STUDENT).withGuardian(guardian).build();
        Model failedSaveModel = new ModelManager(new TuitionBook(), new UserPrefs());
        failedSaveModel.addPerson(student);
        failedSaveModel.addPerson(guardian);
        failedSaveModel.updateFilteredPersonList(person -> person.getRole() == Role.GUARDIAN, "list r/guardian");
        Logic failedSaveLogic = new LogicManager(failedSaveModel,
                storageThatFailsOnSave(temporaryFolder.resolve("deleteLinkedGuardian.json")));

        assertThrows(CommandException.class, () -> failedSaveLogic.execute("delete 1"));

        List<Person> restoredPersons = failedSaveLogic.getPersonList();
        assertEquals(List.of(student, guardian), restoredPersons);
        assertEquals(student.getId(), restoredPersons.get(0).getId());
        assertEquals(guardian.getId(), restoredPersons.get(1).getId());
        assertEquals(guardian.getId(), restoredPersons.get(0).getGuardianId().orElseThrow());
        assertEquals(List.of(guardian), failedSaveLogic.getFilteredPersonList());
    }

    @Test
    public void execute_failedUnlink_restoresContactsLinksFilterAndIds() throws Exception {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("90000001")
                .withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Sam Student").withPhone("90000002")
                .withRole(Role.STUDENT).withGuardian(guardian).build();
        Model failedSaveModel = new ModelManager(new TuitionBook(), new UserPrefs());
        failedSaveModel.addPerson(student);
        failedSaveModel.addPerson(guardian);
        failedSaveModel.updateFilteredPersonList(person -> person.getRole() == Role.STUDENT, "list r/student");
        Logic failedSaveLogic = new LogicManager(failedSaveModel,
                storageThatFailsOnSave(temporaryFolder.resolve("unlink.json")));

        assertThrows(CommandException.class, () -> failedSaveLogic.execute("unlink 1"));

        List<Person> restoredPersons = failedSaveLogic.getPersonList();
        assertEquals(List.of(student, guardian), restoredPersons);
        assertEquals(student.getId(), restoredPersons.get(0).getId());
        assertEquals(guardian.getId(), restoredPersons.get(1).getId());
        assertEquals(guardian.getId(), restoredPersons.get(0).getGuardianId().orElseThrow());
        assertEquals(List.of(student), failedSaveLogic.getFilteredPersonList());
    }

    @Test
    public void execute_nonMutatingCommandsAfterConfiguredLoadFailure_leaveBackupUnchanged() throws Exception {
        Path tuitionBookFilePath = temporaryFolder.resolve("MalformedTuitionBook.json");
        Model fallbackModel = new ModelManager(new TuitionBook(), new UserPrefs());
        Person person = new PersonBuilder().withName("Alex Lim").build();
        fallbackModel.addPerson(person);
        Logic blockedLogic = createLogicWithRecoveredStorage(tuitionBookFilePath, fallbackModel);
        byte[] originalContents = Files.readAllBytes(tuitionBookFilePath.resolveSibling(
                tuitionBookFilePath.getFileName() + ".backup"));

        CommandResult helpResult = blockedLogic.execute(HelpCommand.COMMAND_WORD);
        assertEquals(HelpCommand.SHOWING_HELP_MESSAGE, helpResult.getFeedbackToUser());
        assertTrue(helpResult.isShowHelp());
        assertFalse(helpResult.isExit());

        CommandResult listResult = blockedLogic.execute(ListCommand.COMMAND_WORD);
        assertEquals(ListCommand.MESSAGE_SUCCESS, listResult.getFeedbackToUser());

        CommandResult findResult = blockedLogic.execute("find Alex");
        assertEquals(String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, 1), findResult.getFeedbackToUser());
        assertEquals(1, blockedLogic.getFilteredPersonList().size());
        assertEquals(person, blockedLogic.getFilteredPersonList().get(0));

        CommandResult viewResult = blockedLogic.execute("view 1");
        assertEquals(String.format(ViewCommand.MESSAGE_SUCCESS, person.getName()), viewResult.getFeedbackToUser());
        assertTrue(viewResult.getDetailsToShow().isPresent());

        CommandResult exitResult = blockedLogic.execute(ExitCommand.COMMAND_WORD);
        assertEquals(ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT, exitResult.getFeedbackToUser());
        assertTrue(exitResult.isExit());
        assertFalse(exitResult.isShowHelp());
        assertArrayEquals(originalContents, Files.readAllBytes(tuitionBookFilePath.resolveSibling(
                tuitionBookFilePath.getFileName() + ".backup")));
    }

    @Test
    public void execute_helpAndExitAfterFailedBackup_leaveOriginalDataUnchanged() throws Exception {
        Path tuitionBookFilePath = temporaryFolder.resolve("BlockedMalformedTuitionBook.json");
        Logic blockedLogic = createLogicWithBlockedStorage(tuitionBookFilePath, new ModelManager());

        CommandResult helpResult = blockedLogic.execute(HelpCommand.COMMAND_WORD);
        assertEquals(HelpCommand.SHOWING_HELP_MESSAGE, helpResult.getFeedbackToUser());
        assertTrue(helpResult.isShowHelp());

        CommandResult exitResult = blockedLogic.execute(ExitCommand.COMMAND_WORD);
        assertEquals(ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT, exitResult.getFeedbackToUser());
        assertTrue(exitResult.isExit());
        assertEquals(MALFORMED_TUITION_BOOK_JSON, Files.readString(tuitionBookFilePath));
    }

    @Test
    public void execute_addAfterConfiguredLoadFailure_savesNewDataAndPreservesBackup() throws Exception {
        Path tuitionBookFilePath = temporaryFolder.resolve("MalformedTuitionBook.json");
        Model fallbackModel = new ModelManager(new TuitionBook(), new UserPrefs());
        Logic blockedLogic = createLogicWithRecoveredStorage(tuitionBookFilePath, fallbackModel);
        Path backupPath = tuitionBookFilePath.resolveSibling(tuitionBookFilePath.getFileName() + ".backup");
        byte[] originalContents = Files.readAllBytes(backupPath);
        String addCommand = AddCommand.COMMAND_WORD + ROLE_DESC_STUDENT + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;

        blockedLogic.execute(addCommand);
        assertArrayEquals(originalContents, Files.readAllBytes(backupPath));
        assertTrue(Files.exists(tuitionBookFilePath));
    }

    @Test
    public void execute_mutatingCommandsAfterConfiguredLoadFailure_preserveBackup()
            throws Exception {
        String[] commands = {"edit 1 n/New Student", "delete 1", "clear confirm", "link 1 2", "unlink 1"};
        for (int i = 0; i < commands.length; i++) {
            String command = commands[i];
            Path tuitionBookFilePath = temporaryFolder.resolve("MalformedTuitionBook" + i + ".json");
            Model fallbackModel = new ModelManager(new TuitionBook(), new UserPrefs());
            Person guardian = new PersonBuilder().withName("Grace Guardian").withRole(Role.GUARDIAN).build();
            Person student = new PersonBuilder().withName("Sam Student").withRole(Role.STUDENT)
                    .withGuardian(guardian).build();
            if (command.startsWith("link")) {
                student = student.clearGuardian();
            }
            fallbackModel.addPerson(student);
            fallbackModel.addPerson(guardian);
            Logic blockedLogic = createLogicWithRecoveredStorage(tuitionBookFilePath, fallbackModel);
            Path backupPath = tuitionBookFilePath.resolveSibling(tuitionBookFilePath.getFileName() + ".backup");
            byte[] originalContents = Files.readAllBytes(backupPath);

            blockedLogic.execute(command);
            assertArrayEquals(originalContents, Files.readAllBytes(backupPath));
            assertTrue(Files.exists(tuitionBookFilePath));
        }
    }

    private Logic createLogicWithRecoveredStorage(Path tuitionBookFilePath, Model fallbackModel) throws Exception {
        Files.writeString(tuitionBookFilePath, MALFORMED_TUITION_BOOK_JSON);
        JsonTuitionBookStorage tuitionBookStorage = new JsonTuitionBookStorage(tuitionBookFilePath);
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("BlockedUserPrefs.json"));
        StorageManager storage = new StorageManager(tuitionBookStorage, userPrefsStorage);
        assertThrows(DataLoadingException.class, storage::readTuitionBook);
        return new LogicManager(fallbackModel, storage);
    }

    private StorageManager storageThatFailsOnSave(Path filePath) {
        JsonTuitionBookStorage tuitionBookStorage = new JsonTuitionBookStorage(filePath) {
            @Override
            public void saveTuitionBook(ReadOnlyTuitionBook tuitionBook) throws IOException {
                throw DUMMY_IO_EXCEPTION;
            }
        };
        return new StorageManager(tuitionBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve(filePath.getFileName() + "Prefs.json")));
    }

    private Logic createLogicWithBlockedStorage(Path tuitionBookFilePath, Model fallbackModel) throws Exception {
        Files.writeString(tuitionBookFilePath, MALFORMED_TUITION_BOOK_JSON);
        JsonTuitionBookStorage tuitionBookStorage = new JsonTuitionBookStorage(tuitionBookFilePath) {
            @Override
            protected void moveFile(Path source, Path target) throws IOException {
                throw new IOException("simulated backup failure");
            }
        };
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("BlockedUserPrefs.json"));
        StorageManager storage = new StorageManager(tuitionBookStorage, userPrefsStorage);
        assertThrows(DataLoadingException.class, storage::readTuitionBook);
        return new LogicManager(fallbackModel, storage);
    }

    @Test
    public void execute_reloadCorrectedFile_unblocksSavingAndReplacesFilteredContacts() throws Exception {
        Path file = temporaryFolder.resolve("reload.json");
        model.addPerson(AMY);
        model.updateFilteredPersonList(person -> false, "find Missing");
        Logic blocked = createLogicWithBlockedStorage(file, model);
        assertThrows(CommandException.class, () -> blocked.execute("clear confirm"));
        TuitionBook corrected = new TuitionBook();
        Person recovered = new PersonBuilder().withName("Recovered Student").build();
        corrected.addPerson(recovered);
        new JsonTuitionBookStorage(file).saveTuitionBook(corrected);
        byte[] originalBytes = Files.readAllBytes(file);

        assertTrue(blocked.execute("reload").getFeedbackToUser().contains("Reloaded 1 contact(s)"));
        assertEquals(List.of(recovered), model.getFilteredPersonList());
        assertArrayEquals(originalBytes, Files.readAllBytes(file));
        blocked.execute("edit 1 n/Saved Student");
        assertEquals("Saved Student", new JsonTuitionBookStorage(file).readTuitionBook().orElseThrow()
                .getPersonList().getFirst().getName().fullName);
    }

    @Test
    public void execute_reloadStillBlocked_preservesModelFilterAndOriginalFile() throws Exception {
        Path file = temporaryFolder.resolve("stillBlocked.json");
        model.addPerson(AMY);
        model.updateFilteredPersonList(person -> false, "find Missing");
        Logic blocked = createLogicWithBlockedStorage(file, model);
        assertThrows(CommandException.class, () -> blocked.execute("reload"));
        assertEquals(List.of(AMY), model.getTuitionBook().getPersonList());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(MALFORMED_TUITION_BOOK_JSON, Files.readString(file));
        assertThrows(CommandException.class, () -> blocked.execute("clear confirm"));
    }

    @Test
    public void execute_reloadMissingFile_unblocksSavingAndShowsEmptyList() throws Exception {
        Path file = temporaryFolder.resolve("removed.json");
        Logic blocked = createLogicWithBlockedStorage(file, model);
        Files.delete(file);
        assertTrue(blocked.execute("reload").getFeedbackToUser().contains("Reloaded 0 contact(s)"));
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertFalse(Files.exists(file));
        blocked.execute("add r/student n/New Student p/91234567");
        assertEquals(1, new JsonTuitionBookStorage(file).readTuitionBook().orElseThrow().getPersonList().size());
    }

    @Test
    public void execute_reloadWithArguments_rejectedBeforeReading() throws Exception {
        model.addPerson(AMY);
        assertThrows(ParseException.class, () -> logic.execute("reload extra"));
        assertEquals(List.of(AMY), model.getTuitionBook().getPersonList());
    }

    @Test
    public void execute_reloadAccessDenied_preservesContactsAndFilter() throws Exception {
        model.addPerson(AMY);
        model.updateFilteredPersonList(person -> false, "find Missing");
        StorageManager inaccessibleStorage = new StorageManager(
                new JsonTuitionBookStorage(temporaryFolder.resolve("inaccessible.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))) {
            @Override
            public Optional<TuitionBookLoadResult> readTuitionBookWithResult() throws DataLoadingException {
                throw new DataLoadingException(new AccessDeniedException("simulated unreadable data file"));
            }
        };
        Logic inaccessibleLogic = new LogicManager(model, inaccessibleStorage);
        CommandException exception = org.junit.jupiter.api.Assertions.assertThrows(
                CommandException.class, () -> inaccessibleLogic.execute("reload"));
        assertTrue(exception.getMessage().contains("did not have permission to read the data file"));
        assertFalse(exception.getMessage().contains("JSON"));
        assertEquals(List.of(AMY), model.getTuitionBook().getPersonList());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    public void execute_reloadInaccessibleDirectory_doesNotTreatUnknownExistenceAsAbsence() throws Exception {
        Path directory = Files.createDirectory(temporaryFolder.resolve("restricted"));
        Path file = directory.resolve("contacts.json");
        JsonTuitionBookStorage fileStorage = new JsonTuitionBookStorage(file);
        fileStorage.saveTuitionBook(new TuitionBook());
        Logic restrictedLogic = new LogicManager(model, new StorageManager(fileStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("restrictedPrefs.json"))));
        model.addPerson(AMY);
        model.updateFilteredPersonList(person -> false, "find Missing");
        Set<PosixFilePermission> originalPermissions = Files.getPosixFilePermissions(directory);
        try {
            Files.setPosixFilePermissions(directory, Set.of());
            assumeFalse(Files.exists(file), "Privileged process bypasses directory permissions");
            assertFalse(Files.notExists(file));
            assertThrows(CommandException.class, () -> restrictedLogic.execute("reload"));
            assertEquals(List.of(AMY), model.getTuitionBook().getPersonList());
            assertTrue(model.getFilteredPersonList().isEmpty());
        } finally {
            Files.setPosixFilePermissions(directory, originalPermissions);
        }
        assertThrows(IOException.class, () -> fileStorage.saveTuitionBook(model.getTuitionBook()));
    }

    @Test
    public void execute_reloadInvalidGuardian_repairsOnlyLinkWithoutSaving() throws Exception {
        Path file = temporaryFolder.resolve("tuitionBook.json");
        Person missingGuardian = new PersonBuilder().withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Linked Student").withGuardian(missingGuardian).build();
        TuitionBook persisted = new TuitionBook();
        persisted.addPerson(student);
        persisted.addPerson(AMY);
        JsonTuitionBookStorage fileStorage = new JsonTuitionBookStorage(file);
        fileStorage.saveTuitionBook(persisted);
        byte[] originalBytes = Files.readAllBytes(file);
        model.addPerson(AMY);
        model.updateFilteredPersonList(person -> false, "find Missing");

        CommandResult result = logic.execute("reload");

        assertTrue(result.getFeedbackToUser().contains("Removed 1 invalid guardian link(s) in memory."));
        assertTrue(result.getFeedbackToUser().contains(student.getName().fullName));
        assertTrue(result.getFeedbackToUser().contains(student.getId().toString()));
        assertTrue(result.getFeedbackToUser().contains(student.getGuardianId().orElseThrow().toString()));
        assertTrue(result.getFeedbackToUser().contains("the referenced contact does not exist"));
        assertEquals(List.of(student.clearGuardian(), AMY), model.getFilteredPersonList());
        assertEquals(student.getId(), model.getFilteredPersonList().getFirst().getId());
        assertTrue(model.getFilteredPersonList().getFirst().getGuardianId().isEmpty());
        assertArrayEquals(originalBytes, Files.readAllBytes(file));

        logic.execute("edit 1 n/Saved Student");
        assertFalse(java.util.Arrays.equals(originalBytes, Files.readAllBytes(file)));
        assertEquals(0, fileStorage.readTuitionBookWithResult().orElseThrow().repairedGuardianLinkCount());
        assertEquals(AMY, fileStorage.readTuitionBook().orElseThrow().getPersonList().get(1));
    }

    @Test
    public void execute_reloadMalformedGuardian_reportsRepairWithoutGuardianId() throws Exception {
        Path file = temporaryFolder.resolve("tuitionBook.json");
        Files.writeString(file, """
                {"persons":[{"id":"123e4567-e89b-12d3-a456-426614174000","role":"STUDENT",
                "guardianId":"not-a-uuid","name":"Malformed Link","phone":"91234567","tags":[]}]}
                """);

        CommandResult result = logic.execute("reload");

        assertTrue(result.getFeedbackToUser().contains("Malformed Link"));
        assertTrue(result.getFeedbackToUser().contains("the guardian ID is malformed"));
        assertTrue(result.getFeedbackToUser().contains("guardianId: not-a-uuid"));
        assertTrue(model.getFilteredPersonList().getFirst().getGuardianId().isEmpty());
    }

    @Test
    public void execute_reloadMalformedFile_preservesBackupAndCurrentContacts() throws Exception {
        Path file = temporaryFolder.resolve("tuitionBook.json");
        model.addPerson(AMY);
        model.updateFilteredPersonList(person -> false, "find Missing");
        Files.writeString(file, MALFORMED_TUITION_BOOK_JSON);
        assertThrows(CommandException.class, () -> logic.execute("reload"));
        assertEquals(List.of(AMY), model.getTuitionBook().getPersonList());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(MALFORMED_TUITION_BOOK_JSON,
                Files.readString(temporaryFolder.resolve("tuitionBook.json.backup")));
        assertFalse(Files.exists(file));
        logic.execute("clear confirm");
        assertTrue(Files.exists(file));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    @Test
    public void getPersonList_filteredPersonList_returnsAllPersons() {
        Person student = new PersonBuilder().withName("Sam Student").withRole(Role.STUDENT).build();
        Person guardian = new PersonBuilder().withName("Gail Guardian").withRole(Role.GUARDIAN).build();
        model.addPerson(student);
        model.addPerson(guardian);
        model.updateFilteredPersonList(person -> person.getRole() == Role.STUDENT, "list r/student");

        assertEquals(List.of(student), logic.getFilteredPersonList());
        assertEquals(List.of(student, guardian), logic.getPersonList());
    }

    @Test
    public void getPersonList_modifyList_throwsUnsupportedOperationException() {
        model.addPerson(new PersonBuilder().build());

        assertThrows(UnsupportedOperationException.class, () -> logic.getPersonList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getTuitionBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonTuitionBookStorage that throws the IOException e when saving
        JsonTuitionBookStorage tuitionBookStorage = new JsonTuitionBookStorage(prefPath) {
            @Override
            public void saveTuitionBook(ReadOnlyTuitionBook tuitionBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(tuitionBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveTuitionBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + ROLE_DESC_STUDENT + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        ModelManager expectedModel = new ModelManager();
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }

    private static class CountingTuitionBookStorage extends JsonTuitionBookStorage {
        private final IOException exceptionToThrow;
        private int saveCallCount;

        CountingTuitionBookStorage(Path filePath, IOException exceptionToThrow) {
            super(filePath);
            this.exceptionToThrow = exceptionToThrow;
        }

        @Override
        public void saveTuitionBook(ReadOnlyTuitionBook tuitionBook) throws IOException {
            saveCallCount++;
            if (exceptionToThrow != null) {
                throw exceptionToThrow;
            }
            super.saveTuitionBook(tuitionBook);
        }

        int getSaveCallCount() {
            return saveCallCount;
        }
    }
}
