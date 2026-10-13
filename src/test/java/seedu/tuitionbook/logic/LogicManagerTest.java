package seedu.tuitionbook.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.tuitionbook.commons.exceptions.DataLoadingException;
import seedu.tuitionbook.logic.commands.AddCommand;
import seedu.tuitionbook.logic.commands.CommandResult;
import seedu.tuitionbook.logic.commands.ExitCommand;
import seedu.tuitionbook.logic.commands.HelpCommand;
import seedu.tuitionbook.logic.commands.ListCommand;
import seedu.tuitionbook.logic.commands.ViewCommand;
import seedu.tuitionbook.logic.commands.exceptions.CommandException;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;
import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.ReadOnlyTuitionBook;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.UserPrefs;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.storage.JsonTuitionBookStorage;
import seedu.tuitionbook.storage.JsonUserPrefsStorage;
import seedu.tuitionbook.storage.StorageManager;
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
    public void execute_add_savesContactData() throws Exception {
        String addCommand = AddCommand.COMMAND_WORD + ROLE_DESC_STUDENT + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        Person expectedPerson = new PersonBuilder(AMY).withTags().build();
        ModelManager expectedModel = new ModelManager();
        expectedModel.addPerson(expectedPerson);

        assertCommandSuccess(addCommand, String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(expectedPerson)),
                expectedModel);
        Path dataFilePath = temporaryFolder.resolve("tuitionBook.json");
        assertEquals(model.getTuitionBook(), new JsonTuitionBookStorage(dataFilePath).readTuitionBook().orElseThrow());
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
    public void execute_nonMutatingCommandsAfterConfiguredLoadFailure_succeedWithoutWriting() throws Exception {
        Path tuitionBookFilePath = temporaryFolder.resolve("MalformedTuitionBook.json");
        Model fallbackModel = new ModelManager(new TuitionBook(), new UserPrefs());
        Person person = new PersonBuilder().withName("Alex Lim").build();
        fallbackModel.addPerson(person);
        Logic blockedLogic = createLogicWithBlockedStorage(tuitionBookFilePath, fallbackModel);
        byte[] originalContents = Files.readAllBytes(tuitionBookFilePath);

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
        assertArrayEquals(originalContents, Files.readAllBytes(tuitionBookFilePath));
    }

    @Test
    public void execute_addAfterConfiguredLoadFailure_reportsStorageErrorWithoutWriting() throws Exception {
        Path tuitionBookFilePath = temporaryFolder.resolve("MalformedTuitionBook.json");
        Model fallbackModel = new ModelManager(new TuitionBook(), new UserPrefs());
        Logic blockedLogic = createLogicWithBlockedStorage(tuitionBookFilePath, fallbackModel);
        byte[] originalContents = Files.readAllBytes(tuitionBookFilePath);
        String expectedMessage = String.format(LogicManager.FILE_OPS_ERROR_FORMAT,
                String.format(JsonTuitionBookStorage.MESSAGE_WRITE_BLOCKED, tuitionBookFilePath));
        String addCommand = AddCommand.COMMAND_WORD + ROLE_DESC_STUDENT + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;

        assertThrows(CommandException.class, expectedMessage, () -> blockedLogic.execute(addCommand));
        assertArrayEquals(originalContents, Files.readAllBytes(tuitionBookFilePath));
    }

    @Test
    public void execute_otherMutatingCommandsAfterConfiguredLoadFailure_reportStorageErrorWithoutWriting()
            throws Exception {
        String[] commands = {"edit 1 n/New Student", "delete 1", "clear", "link 1 2"};
        for (int i = 0; i < commands.length; i++) {
            String command = commands[i];
            Path tuitionBookFilePath = temporaryFolder.resolve("MalformedTuitionBook" + i + ".json");
            Model fallbackModel = new ModelManager(new TuitionBook(), new UserPrefs());
            fallbackModel.addPerson(new PersonBuilder().withName("Sam Student").withRole(Role.STUDENT).build());
            fallbackModel.addPerson(new PersonBuilder().withName("Grace Guardian").withRole(Role.GUARDIAN).build());
            Logic blockedLogic = createLogicWithBlockedStorage(tuitionBookFilePath, fallbackModel);
            byte[] originalContents = Files.readAllBytes(tuitionBookFilePath);
            String expectedMessage = String.format(LogicManager.FILE_OPS_ERROR_FORMAT,
                    String.format(JsonTuitionBookStorage.MESSAGE_WRITE_BLOCKED, tuitionBookFilePath));

            assertThrows(CommandException.class, expectedMessage, () -> blockedLogic.execute(command));
            assertArrayEquals(originalContents, Files.readAllBytes(tuitionBookFilePath));
        }
    }

    private Logic createLogicWithBlockedStorage(Path tuitionBookFilePath, Model fallbackModel) throws Exception {
        Files.writeString(tuitionBookFilePath, MALFORMED_TUITION_BOOK_JSON);
        JsonTuitionBookStorage tuitionBookStorage = new JsonTuitionBookStorage(tuitionBookFilePath);
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("BlockedUserPrefs.json"));
        StorageManager storage = new StorageManager(tuitionBookStorage, userPrefsStorage);
        assertThrows(DataLoadingException.class, storage::readTuitionBook);
        return new LogicManager(fallbackModel, storage);
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
        model.updateFilteredPersonList(person -> person.getRole() == Role.STUDENT);

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
        Person expectedPerson = new PersonBuilder(AMY).withTags().build();
        ModelManager expectedModel = new ModelManager();
        expectedModel.addPerson(expectedPerson);
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }
}
