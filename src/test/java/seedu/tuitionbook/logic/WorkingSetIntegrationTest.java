package seedu.tuitionbook.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.tuitionbook.logic.commands.exceptions.CommandException;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.PersonListViewState;
import seedu.tuitionbook.model.ReadOnlyTuitionBook;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.UserPrefs;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.storage.JsonTuitionBookStorage;
import seedu.tuitionbook.storage.JsonUserPrefsStorage;
import seedu.tuitionbook.storage.StorageManager;

/** Acceptance tests for #187's stable UUID working set, persistence and rollback contract. */
public class WorkingSetIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private ModelManager model;
    private TestStorage fileStorage;
    private StorageManager storage;
    private LogicManager logic;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        fileStorage = new TestStorage(temporaryFolder.resolve("contacts.json"));
        storage = new StorageManager(fileStorage, new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_queryWithNoncanonicalSyntax_storesEquivalentCanonicalRefreshCommand() throws Exception {
        execute("add r/student n/Alex Student p/90000001 t/Active", " LiSt   t/Active   r/STUDENT ");
        assertEquals("list r/student t/Active", model.getPersonListViewState().refreshCommand());
        logic.execute(" FIND   Alex\tJohn ");
        assertEquals("find Alex John", model.getPersonListViewState().refreshCommand());
        assertTrue(logic.execute("edit 1 n/Renamed Student").getFeedbackToUser()
                .contains("Refresh with: find Alex John"));
    }

    @Test
    public void execute_correctFilteredContact_keepsTargetAndPersistsCorrection() throws Exception {
        // Scenario adapted from https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/183
        execute("add r/student n/Alex Yeoh p/87438807", "add r/student n/John Doe p/98765432", "find John");
        List<UUID> originalIds = ids();
        execute("edit 1 p/9876543", "edit 1 p/98765432");
        assertEquals(originalIds, ids());
        assertEquals("87438807", model.getTuitionBook().getPersonList().getFirst().getPhone().value);
        assertEquals("98765432", model.getFilteredPersonList().getFirst().getPhone().value);
        assertEquals(model.getTuitionBook(), storage.readTuitionBook().orElseThrow());
    }

    @Test
    public void execute_addToFilteredGroup_appendsMatchingAndKeepsDeletionTarget() throws Exception {
        // Scenario adapted from https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/315
        execute("add r/guardian n/Grace Guardian p/90000001", "add r/student n/Sam Student p/90000022 t/T1",
                "add r/student n/Carl Student p/90000003 t/T1", "list t/T1");
        List<UUID> originalIds = ids();
        logic.execute("add r/student n/Eve Student p/90000005 t/T1");
        assertEquals(originalIds, ids().subList(0, 2));
        assertEquals(List.of("Sam Student", "Carl Student", "Eve Student"), names());
        assertTrue(logic.execute("add r/guardian n/Hidden Guardian p/90000006").getFeedbackToUser()
                .contains("saved but does not match"));
        assertEquals(List.of("Sam Student", "Carl Student", "Eve Student"), names());
        assertTrue(logic.execute("delete 2").getFeedbackToUser().contains("subsequent indexes may have changed"));
        assertEquals(List.of("Sam Student", "Eve Student"), names());
        assertFalse(storage.readTuitionBook().orElseThrow().getPersonList().stream()
                .anyMatch(person -> person.getName().fullName.equals("Carl Student")));
    }

    @Test
    public void execute_activeInactiveWorkflow_retainsExceptionsUntilExplicitRefresh() throws Exception {
        execute("add r/student n/Alex Student p/90000001 t/Active", "add r/student n/John Student p/90000002 t/Active",
                "list t/Active");
        List<UUID> originalIds = ids();
        String feedback = logic.execute("untag 1 t/Active").getFeedbackToUser();
        assertTrue(feedback.contains("no longer match"));
        assertTrue(feedback.contains("Refresh with: list t/Active"));
        execute("tag 1 t/Inactive", "untag 2 t/Active");
        assertEquals(originalIds, ids());
        assertEquals(2, model.getFilteredPersonList().stream()
                .filter(model.getPersonListQueryPredicate().negate()).count());
        logic.execute("tag 1 t/Active");
        assertTrue(model.getPersonListQueryPredicate().test(model.getFilteredPersonList().getFirst()));
        logic.execute("list t/Active");
        assertEquals(List.of("Alex Student"), names());
        logic.execute("list");
        assertEquals(List.of("Alex Student", "John Student"), names());
    }

    @Test
    public void execute_relationshipAndTagChanges_preserveEntireUuidMapping() throws Exception {
        prepareWorkingSet();
        List<UUID> originalIds = ids();
        for (String command : List.of("link 2 3", "unlink 2", "edit 2 n/Renamed Student", "tag 2 t/Math",
                "untag 4 t/Active", "edit 4 n/Other Student")) {
            logic.execute(command);
            assertEquals(originalIds, ids(), command);
        }
        assertTrue(model.getFilteredPersonList().get(1).getGuardianId().isEmpty());
        assertEquals("Renamed Student", model.getFilteredPersonList().get(1).getName().fullName);
        assertEquals("list t/Active", model.getPersonListViewState().refreshCommand());
    }

    @Test
    public void execute_deleteFirstMiddleLastAndOnly_compactsSurvivorsWithoutHiddenRows() throws Exception {
        prepareWorkingSet();
        TuitionBook original = new TuitionBook(model.getTuitionBook());
        PersonListViewState originalView = model.getPersonListViewState();
        for (int index : List.of(1, 3, 5)) {
            model.setTuitionBook(original);
            model.restorePersonListViewState(originalView);
            List<UUID> expected = new ArrayList<>(ids());
            expected.remove(index - 1);
            logic.execute("delete " + index);
            assertEquals(expected, ids());
        }
        model.setTuitionBook(original);
        model.restorePersonListViewState(originalView);
        logic.execute("find Eve");
        assertEquals(1, ids().size());
        logic.execute("delete 1");
        assertTrue(ids().isEmpty());
        assertFalse(model.getTuitionBook().getPersonList().isEmpty());
    }

    @Test
    public void execute_deleteGuardian_updatesRelationshipsWithoutRequerying() throws Exception {
        prepareWorkingSet();
        List<UUID> expected = new ArrayList<>(ids());
        expected.removeFirst();
        logic.execute("delete 1");
        assertEquals(expected, ids());
        assertTrue(model.getFilteredPersonList().getFirst().getGuardianId().isEmpty());
    }

    @Test
    public void execute_actualSaveFailures_restoreDataAndExactWorkingSet() throws Exception {
        prepareWorkingSet();
        TuitionBook original = new TuitionBook(model.getTuitionBook());
        PersonListViewState originalView = model.getPersonListViewState();
        String originalJson = Files.readString(fileStorage.getTuitionBookFilePath());
        fileStorage.failSaving = true;
        for (String command : List.of("add r/student n/New Student p/90000009 t/Active",
                "add r/student n/Nonmatching Student p/90000010", "edit 2 n/Changed Student", "tag 2 t/Math",
                "untag 4 t/Active", "link 2 3", "unlink 2", "delete 1", "clear confirm")) {
            assertThrows(CommandException.class, () -> logic.execute(command), command);
            assertEquals(original, model.getTuitionBook(), command);
            assertEquals(originalView, model.getPersonListViewState(), command);
            assertEquals(originalView.personIds(), ids(), command);
            assertEquals(originalJson, Files.readString(fileStorage.getTuitionBookFilePath()), command);
        }
        int savesBefore = fileStorage.saveCalls;
        for (String command : List.of("edit 2 n/Alex Student", "untag 2 t/Absent", "tag 4 t/Active", "link 2 1")) {
            logic.execute(command);
            assertEquals(originalView, model.getPersonListViewState());
        }
        assertEquals(savesBefore, fileStorage.saveCalls);
    }

    @Test
    public void execute_rejectedAndReadOnlyCommands_preserveView() throws Exception {
        prepareWorkingSet();
        PersonListViewState originalView = model.getPersonListViewState();
        for (String command : List.of("edit 99 n/Missing Student", "tag 99 t/Math", "link 1 2", "unlink 1",
                "add r/student n/Alex Student p/90000002")) {
            assertThrows(CommandException.class, () -> logic.execute(command), command);
            assertEquals(originalView, model.getPersonListViewState());
        }
        assertThrows(ParseException.class, () -> logic.execute("edit 0 n/Invalid Student"));
        execute("view 2", "help");
        assertEquals(originalView, model.getPersonListViewState());
    }

    @Test
    public void execute_reloadFailureSuccessAndRestart_followResetContract() throws Exception {
        prepareWorkingSet();
        TuitionBook original = new TuitionBook(model.getTuitionBook());
        PersonListViewState originalView = model.getPersonListViewState();
        Files.writeString(fileStorage.getTuitionBookFilePath(), "{broken");
        assertThrows(CommandException.class, () -> logic.execute("reload"));
        assertEquals(original, model.getTuitionBook());
        assertEquals(originalView, model.getPersonListViewState());
        fileStorage.saveTuitionBook(original);
        logic.execute("reload");
        assertEquals(original.getPersonList(), model.getFilteredPersonList());
        assertEquals("list", model.getPersonListViewState().refreshCommand());
        ModelManager restarted = new ModelManager(storage.readTuitionBook().orElseThrow(), new UserPrefs());
        assertEquals(original.getPersonList(), restarted.getFilteredPersonList());
        assertTrue(Files.readString(fileStorage.getTuitionBookFilePath()).contains("Alex Student"));
        logic.execute("find Missing");
        logic.execute("clear confirm");
        assertTrue(model.getTuitionBook().getPersonList().isEmpty());
        assertTrue(ids().isEmpty());
        assertEquals("list", model.getPersonListViewState().refreshCommand());
    }

    @Test
    public void execute_emptyCombinedQuery_classifiesAdditionsWithoutReordering() throws Exception {
        execute("list r/student t/Active t/Math", "add r/guardian n/Hidden Guardian p/90000001 t/Active t/Math",
                "add r/student n/Other Student p/90000002 t/Active");
        assertTrue(ids().isEmpty());
        execute("add r/student n/First Match p/90000003 t/Active t/Math",
                "add r/student n/Second Match p/90000004 t/Active t/Math", "edit 1 t/Inactive");
        assertEquals(List.of("First Match", "Second Match"), names());
        logic.execute("list r/student t/Active t/Math");
        assertEquals(List.of("Second Match"), names());
        logic.execute("find NoMatch");
        logic.execute("add r/student n/NoMatch Student p/90000005");
        assertEquals(List.of("NoMatch Student"), names());
    }

    private void prepareWorkingSet() throws Exception {
        execute("add r/student n/Hidden Student p/90000008", "add r/guardian n/Grace Guardian p/90000001 t/Active",
                "add r/student n/Alex Student p/90000002 t/Active",
                "add r/guardian n/Other Guardian p/90000003 t/Active",
                "add r/student n/John Student p/90000004 t/Active", "list t/Active", "untag 2 t/Active",
                "link 2 1", "add r/student n/Eve Student p/90000005 t/Active");
    }

    private void execute(String... commands) throws Exception {
        for (String command : commands) {
            logic.execute(command);
        }
    }

    private List<UUID> ids() {
        return model.getFilteredPersonList().stream().map(Person::getId).toList();
    }

    private List<String> names() {
        return model.getFilteredPersonList().stream().map(person -> person.getName().fullName).toList();
    }

    private static class TestStorage extends JsonTuitionBookStorage {
        private boolean failSaving;
        private int saveCalls;

        TestStorage(Path path) {
            super(path);
        }

        @Override
        public void saveTuitionBook(ReadOnlyTuitionBook tuitionBook) throws IOException {
            saveCalls++;
            if (failSaving) {
                throw new IOException("Synthetic save failure");
            }
            super.saveTuitionBook(tuitionBook);
        }
    }
}
