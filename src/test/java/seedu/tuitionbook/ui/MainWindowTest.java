package seedu.tuitionbook.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.tuitionbook.commons.core.GuiSettings;
import seedu.tuitionbook.commons.exceptions.DataLoadingException;
import seedu.tuitionbook.logic.Logic;
import seedu.tuitionbook.logic.LogicManager;
import seedu.tuitionbook.logic.commands.CommandResult;
import seedu.tuitionbook.logic.commands.ExitCommand;
import seedu.tuitionbook.logic.commands.HelpCommand;
import seedu.tuitionbook.logic.commands.exceptions.CommandException;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.UserPrefs;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.storage.JsonTuitionBookStorage;
import seedu.tuitionbook.storage.JsonUserPrefsStorage;
import seedu.tuitionbook.storage.StorageManager;

/**
 * Tests main window command interactions and view transitions.
 */
public class MainWindowTest {

    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void initializeJavaFx() {
        JavaFxTestUtil.initialize();
    }

    @Test
    public void viewAndOtherCommands_swapMainContentView() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            MainWindow mainWindow = new MainWindow(new Stage(), new FakeLogic(), Path.of("synthetic.json"));
            mainWindow.fillInnerParts();

            TextField commandBox = (TextField) mainWindow.getRoot().getScene().lookup("#commandTextField");
            TextArea detailText = (TextArea) mainWindow.getRoot().getScene().lookup("#detailDisplay");
            TextArea resultText = (TextArea) mainWindow.getRoot().getScene().lookup("#resultDisplay");

            assertListVisible(mainWindow, detailText, true, false);

            execute(commandBox, "view 1");
            assertListVisible(mainWindow, detailText, false, true);
            assertEquals("Name: Alex Lim", detailText.getText());
            assertEquals("Viewing contact: Alex Lim", resultText.getText());

            execute(commandBox, "list");
            assertListVisible(mainWindow, detailText, true, false);

            execute(commandBox, "view 1");
            execute(commandBox, "invalid");
            assertListVisible(mainWindow, detailText, true, false);
            assertEquals("Invalid command", resultText.getText());
        });
    }

    @Test
    public void helpAndExit_afterMalformedContactStorageIsBackedUp_openHelpAndCloseMainWindow() throws Exception {
        Path dataFilePath = temporaryFolder.resolve("tuitionBook.json");
        Files.writeString(dataFilePath, "{broken");
        byte[] originalContents = Files.readAllBytes(dataFilePath);
        JsonTuitionBookStorage tuitionBookStorage = new JsonTuitionBookStorage(dataFilePath);
        StorageManager storage = new StorageManager(tuitionBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        assertThrows(DataLoadingException.class, storage::readTuitionBook);
        Logic logic = new LogicManager(new ModelManager(new TuitionBook(), new UserPrefs()), storage);

        JavaFxTestUtil.runOnFxThread(() -> {
            Stage primaryStage = new Stage();
            Stage helpStage = null;
            try {
                MainWindow mainWindow = new MainWindow(primaryStage, logic, dataFilePath);
                mainWindow.show();
                mainWindow.fillInnerParts();
                TextField commandBox = (TextField) mainWindow.getRoot().getScene().lookup("#commandTextField");
                TextArea resultText = (TextArea) mainWindow.getRoot().getScene().lookup("#resultDisplay");
                List<Window> windowsBeforeHelp = List.copyOf(Window.getWindows());

                execute(commandBox, HelpCommand.COMMAND_WORD);
                helpStage = Window.getWindows().stream()
                        .filter(window -> !windowsBeforeHelp.contains(window))
                        .filter(Stage.class::isInstance)
                        .map(Stage.class::cast)
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("Help window did not open"));
                assertTrue(helpStage.isShowing());
                assertEquals(HelpCommand.SHOWING_HELP_MESSAGE, resultText.getText());

                execute(commandBox, ExitCommand.COMMAND_WORD);
                assertEquals(ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT, resultText.getText());
                assertFalse(primaryStage.isShowing());
                assertFalse(helpStage.isShowing());
            } finally {
                if (helpStage != null) {
                    helpStage.hide();
                }
                primaryStage.hide();
            }
        });
        assertArrayEquals(originalContents, Files.readAllBytes(dataFilePath.resolveSibling(
                dataFilePath.getFileName() + ".backup")));
    }

    private static void execute(TextField commandBox, String command) {
        commandBox.setText(command);
        commandBox.getOnAction().handle(new ActionEvent());
    }

    private static void assertListVisible(MainWindow mainWindow, TextArea detailText,
                                          boolean listVisible, boolean detailVisible) {
        assertEquals(listVisible, mainWindow.getPersonListPanel().getRoot().isVisible());
        assertEquals(detailVisible, detailText.getParent().isVisible());
    }

    private static final class FakeLogic implements Logic {
        private final ObservableList<Person> people = FXCollections.observableArrayList();
        private GuiSettings guiSettings = new GuiSettings();

        @Override
        public CommandResult execute(String commandText) throws CommandException, ParseException {
            return switch (commandText) {
                case "view 1" -> new CommandResult("Viewing contact: Alex Lim", "Name: Alex Lim");
                case "list" -> new CommandResult("Listed all contacts");
                default -> throw new ParseException("Invalid command");
            };
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return people;
        }

        /**
         * Returns the complete person list for Logic versions that expose it.
         *
         * <p>This method intentionally omits {@code @Override} so the fake also compiles
         * against Logic versions from before {@code getPersonList()} was added.</p>
         */
        public ObservableList<Person> getPersonList() {
            return people;
        }

        @Override
        public GuiSettings getGuiSettings() {
            return guiSettings;
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            this.guiSettings = guiSettings;
        }
    }
}
