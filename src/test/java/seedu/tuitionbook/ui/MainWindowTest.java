package seedu.tuitionbook.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.tuitionbook.commons.core.GuiSettings;
import seedu.tuitionbook.logic.Logic;
import seedu.tuitionbook.logic.commands.CommandResult;
import seedu.tuitionbook.logic.commands.exceptions.CommandException;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;
import seedu.tuitionbook.model.person.Person;

/**
 * Tests the main window's list and relationship-detail view transitions.
 */
public class MainWindowTest {

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
