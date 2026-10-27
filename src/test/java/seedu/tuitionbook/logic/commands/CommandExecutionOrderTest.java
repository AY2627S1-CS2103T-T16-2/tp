package seedu.tuitionbook.logic.commands;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.tuitionbook.logic.commands.exceptions.CommandException;
import seedu.tuitionbook.logic.parser.TuitionBookParser;
import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.storage.JsonTuitionBookStorage;
import seedu.tuitionbook.storage.JsonUserPrefsStorage;
import seedu.tuitionbook.storage.Storage;
import seedu.tuitionbook.storage.StorageManager;
import seedu.tuitionbook.testutil.PersonBuilder;

/** Verifies execution order for stateful save decisions and unrestricted access to constant save decisions. */
public class CommandExecutionOrderTest {
    private static final List<String> SAVE_DEPENDENT_COMMANDS = List.of(
            "add r/student n/New Student p/90000003", "delete 1", "edit 1 n/Renamed Student", "clear confirm",
            "link 1 2", "unlink 1", "tag 1 t/Math", "untag 1 t/Math");
    private static final List<String> CONSTANT_SAVE_COMMANDS = List.of("find Student", "list", "list r/student",
            "view 1", "help", "exit", "reload");
    private static final List<String> COMMANDS = Stream.concat(
            SAVE_DEPENDENT_COMMANDS.stream(), CONSTANT_SAVE_COMMANDS.stream()).toList();

    @TempDir
    public Path temporaryFolder;

    @Test
    public void canMutate_beforeExecution_returnsCommandCapability() throws Exception {
        for (String input : COMMANDS) {
            Command command = new TuitionBookParser().parseCommand(input);
            boolean expected = switch (input.split(" ")[0]) {
                case "find", "list", "view", "help", "exit" -> false;
                default -> true;
            };
            assertEquals(expected, command.canMutate(), input);
        }
    }

    @Test
    public void requiresSave_beforeExecution_throwsAssertionErrorForSaveDependentCommands() throws Exception {
        for (String input : SAVE_DEPENDENT_COMMANDS) {
            Command command = new TuitionBookParser().parseCommand(input);
            assertDoesNotThrow(command::canMutate, input);
            assertThrows(AssertionError.class, command::requiresSave, input);
        }
    }

    @Test
    public void requiresSave_beforeExecution_returnsFalseForConstantSaveCommands() throws Exception {
        for (String input : CONSTANT_SAVE_COMMANDS) {
            Command command = new TuitionBookParser().parseCommand(input);
            assertFalse(command.requiresSave(), input);
        }
    }

    @Test
    public void requiresSave_afterExecutionWithStorage_doesNotAssertForEveryCommand() throws Exception {
        Storage storage = new StorageManager(
                new JsonTuitionBookStorage(temporaryFolder.resolve("contacts.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        for (String input : COMMANDS) {
            Command command = new TuitionBookParser().parseCommand(input);
            command.execute(modelWithContacts(), storage);
            assertDoesNotThrow(command::requiresSave, input);
        }
    }

    @Test
    public void requiresSave_afterExecutionWithoutStorage_doesNotAssertForEveryCommand() throws Exception {
        for (String input : COMMANDS) {
            if (input.equals("reload")) {
                continue; // Reload needs storage; its model-only failure is covered below.
            }
            Command command = new TuitionBookParser().parseCommand(input);
            command.execute(modelWithContacts());
            assertDoesNotThrow(command::requiresSave, input);
        }
    }

    @Test
    public void requiresSave_afterFailedFirstExecution_returnsFalse() throws Exception {
        for (String input : List.of("add r/student n/Sam Student p/90000001", "delete 99", "edit 99 n/New Name",
                "link 99 2", "unlink 99", "tag 99 t/Math", "untag 99 t/Math", "view 99", "reload")) {
            Command command = new TuitionBookParser().parseCommand(input);
            assertThrows(CommandException.class, () -> command.execute(modelWithContacts()), input);
            assertFalse(command.requiresSave(), input);
        }
    }

    private static Model modelWithContacts() {
        Model model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Sam Student").withPhone("90000001").build());
        model.addPerson(new PersonBuilder().withName("Grace Guardian").withPhone("90000002")
                .withRole(Role.GUARDIAN).build());
        return model;
    }
}
