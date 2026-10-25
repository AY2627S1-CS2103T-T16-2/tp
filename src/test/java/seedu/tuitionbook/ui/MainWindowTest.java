package seedu.tuitionbook.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
import seedu.tuitionbook.testutil.PersonBuilder;

/**
 * Tests main window command interactions and view transitions.
 */
public class MainWindowTest {
    private static final List<Integer> VIEWPORT_CONTACT_COUNTS =
            List.of(0, 1, 2, 5, 10, 20, 50, 100, 500, 1000);

    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void initializeJavaFx() {
        JavaFxTestUtil.initialize();
    }

    @Test
    public void mainWindow_usesProductNameAsTitle() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            Stage stage = new Stage();
            new MainWindow(stage, new FakeLogic(), Path.of("synthetic.json"));
            assertEquals("TuitionBook", stage.getTitle());
        });
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
    public void addToLongList_scrollsToAddedPerson() throws Exception {
        for (int existingContactCount : VIEWPORT_CONTACT_COUNTS) {
            ScrollFixture fixture = JavaFxTestUtil.callOnFxThread(() -> {
                Person addedPerson = new PersonBuilder().withName("Viewport Student").withPhone("99999999").build();
                FakeLogic logic = new FakeLogic(addedPerson);
                IntStream.range(0, existingContactCount)
                        .mapToObj(index -> new PersonBuilder().withName("Existing Student " + index)
                                .withPhone(String.format("8%07d", index)).build())
                        .forEach(logic.people::add);
                Stage stage = new Stage();
                MainWindow mainWindow = new MainWindow(stage, logic, Path.of("synthetic.json"));
                mainWindow.fillInnerParts();
                stage.setWidth(400);
                stage.setHeight(300);
                mainWindow.show();

                TextField commandBox = (TextField) mainWindow.getRoot().getScene().lookup("#commandTextField");
                ListView<Person> personListView = getPersonListView(mainWindow);
                personListView.scrollTo(0);
                execute(commandBox, "add");
                return new ScrollFixture(stage, personListView, addedPerson);
            });

            try {
                assertTrue(JavaFxTestUtil.waitForFxCondition(
                        () -> isPersonDisplayed(fixture.personListView(), fixture.addedPerson())),
                        "Added contact should be visible for " + existingContactCount + " existing contacts");
                assertNull(JavaFxTestUtil.callOnFxThread(
                        () -> fixture.personListView().getSelectionModel().getSelectedItem()));
            } finally {
                JavaFxTestUtil.runOnFxThread(fixture.stage()::hide);
            }
        }
    }

    @Test
    public void topLevelHelpControl_opensAndReusesHelpWindowWithoutDropdown() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            Stage primaryStage = new Stage();
            Stage helpStage = null;
            try {
                MainWindow mainWindow = new MainWindow(primaryStage, new FakeLogic(), Path.of("synthetic.json"));
                mainWindow.show();

                MenuBar menuBar = (MenuBar) mainWindow.getRoot().getScene().lookup("#menuBar");
                Button helpButton = (Button) mainWindow.getRoot().getScene().lookup("#helpButton");
                assertEquals(1, menuBar.getMenus().size());
                assertEquals("File", menuBar.getMenus().get(0).getText());

                helpButton.fire();
                helpStage = findShowingHelpStage();
                Stage firstHelpStage = helpStage;

                helpButton.fire();
                assertSame(firstHelpStage, findShowingHelpStage());
            } finally {
                if (helpStage != null) {
                    helpStage.hide();
                }
                primaryStage.hide();
            }
        });
    }

    @Test
    public void f1WithCommandBoxFocused_opensAndReusesSameHelpWindow() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            Stage primaryStage = new Stage();
            Stage helpStage = null;
            try {
                MainWindow mainWindow = new MainWindow(primaryStage, new FakeLogic(), Path.of("synthetic.json"));
                mainWindow.fillInnerParts();
                mainWindow.show();
                TextField commandBox = (TextField) mainWindow.getRoot().getScene().lookup("#commandTextField");
                commandBox.requestFocus();

                fireF1(commandBox);
                helpStage = findShowingHelpStage();
                Stage firstHelpStage = helpStage;

                fireF1(commandBox);
                assertSame(firstHelpStage, findShowingHelpStage());
            } finally {
                if (helpStage != null) {
                    helpStage.hide();
                }
                primaryStage.hide();
            }
        });
    }

    @Test
    public void nonF1KeyWithCommandBoxFocused_doesNotOpenHelpAndReachesDownstreamHandler() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            Stage primaryStage = new Stage();
            try {
                MainWindow mainWindow = new MainWindow(primaryStage, new FakeLogic(), Path.of("synthetic.json"));
                mainWindow.fillInnerParts();
                mainWindow.show();
                TextField commandBox = (TextField) mainWindow.getRoot().getScene().lookup("#commandTextField");
                AtomicBoolean downstreamHandlerReached = new AtomicBoolean();
                commandBox.addEventHandler(KeyEvent.KEY_PRESSED, event -> downstreamHandlerReached.set(true));
                commandBox.requestFocus();

                Event.fireEvent(commandBox, new KeyEvent(KeyEvent.KEY_PRESSED, "a", "a", KeyCode.A,
                        false, false, false, false));

                assertTrue(downstreamHandlerReached.get());
                assertFalse(isHelpStageShowing());
            } finally {
                primaryStage.hide();
            }
        });
    }

    @Test
    public void minimizedHelpWindow_helpActivationRestoresSameWindow() throws Exception {
        Stage primaryStage = JavaFxTestUtil.callOnFxThread(Stage::new);
        MainWindow mainWindow = JavaFxTestUtil.callOnFxThread(() ->
                new MainWindow(primaryStage, new FakeLogic(), Path.of("synthetic.json")));
        Stage helpStage = null;
        try {
            JavaFxTestUtil.runOnFxThread(() -> {
                mainWindow.show();
                Button helpButton = (Button) mainWindow.getRoot().getScene().lookup("#helpButton");
                helpButton.fire();
            });
            helpStage = JavaFxTestUtil.callOnFxThread(MainWindowTest::findShowingHelpStage);
            Stage sameHelpStage = helpStage;

            JavaFxTestUtil.runOnFxThread(() -> sameHelpStage.setIconified(true));
            Assumptions.assumeTrue(JavaFxTestUtil.waitForFxCondition(sameHelpStage::isIconified),
                    "The window manager does not support iconification");

            JavaFxTestUtil.runOnFxThread(mainWindow::handleHelp);
            assertTrue(JavaFxTestUtil.waitForFxCondition(() -> !sameHelpStage.isIconified()),
                    "Help window was not restored within the timeout");
            assertSame(sameHelpStage, JavaFxTestUtil.callOnFxThread(MainWindowTest::findShowingHelpStage));
        } finally {
            Stage helpStageToHide = helpStage;
            JavaFxTestUtil.runOnFxThread(() -> {
                if (helpStageToHide != null) {
                    helpStageToHide.hide();
                }
                primaryStage.hide();
            });
        }
    }

    @Test
    public void fileExit_hidesMainAndHelpWindows() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            Stage primaryStage = new Stage();
            Stage helpStage = null;
            try {
                MainWindow mainWindow = new MainWindow(primaryStage, new FakeLogic(), Path.of("synthetic.json"));
                mainWindow.show();
                Button helpButton = (Button) mainWindow.getRoot().getScene().lookup("#helpButton");
                helpButton.fire();
                helpStage = findShowingHelpStage();

                MenuBar menuBar = (MenuBar) mainWindow.getRoot().getScene().lookup("#menuBar");
                menuBar.getMenus().get(0).getItems().get(0).fire();

                assertFalse(primaryStage.isShowing());
                assertFalse(helpStage.isShowing());
            } finally {
                if (helpStage != null) {
                    helpStage.hide();
                }
                primaryStage.hide();
            }
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

                execute(commandBox, HelpCommand.COMMAND_WORD);
                assertSame(helpStage, findShowingHelpStage());

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

    private static void fireF1(TextField commandBox) {
        Event.fireEvent(commandBox, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.F1,
                false, false, false, false));
    }

    private static Stage findShowingHelpStage() {
        return Window.getWindows().stream()
                .filter(Window::isShowing)
                .filter(Stage.class::isInstance)
                .map(Stage.class::cast)
                .filter(stage -> "Help".equals(stage.getTitle()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Help window did not open"));
    }

    private static boolean isHelpStageShowing() {
        return Window.getWindows().stream()
                .filter(Window::isShowing)
                .filter(Stage.class::isInstance)
                .map(Stage.class::cast)
                .anyMatch(stage -> "Help".equals(stage.getTitle()));
    }

    private static void assertListVisible(MainWindow mainWindow, TextArea detailText,
                                          boolean listVisible, boolean detailVisible) {
        assertEquals(listVisible, mainWindow.getPersonListPanel().getRoot().isVisible());
        assertEquals(detailVisible, detailText.getParent().isVisible());
    }

    @SuppressWarnings("unchecked")
    private static ListView<Person> getPersonListView(MainWindow mainWindow) {
        return (ListView<Person>) mainWindow.getRoot().getScene().lookup("#personListView");
    }

    @SuppressWarnings("unchecked")
    private static boolean isPersonDisplayed(ListView<Person> personListView, Person person) {
        personListView.applyCss();
        personListView.layout();
        return personListView.lookupAll(".list-cell").stream()
                .filter(ListCell.class::isInstance)
                .map(node -> (ListCell<Person>) node)
                .anyMatch(cell -> person.equals(cell.getItem()));
    }

    private record ScrollFixture(Stage stage, ListView<Person> personListView, Person addedPerson) {
    }

    private static final class FakeLogic implements Logic {
        private final ObservableList<Person> people = FXCollections.observableArrayList();
        private final Person personToAdd;
        private GuiSettings guiSettings = new GuiSettings();

        private FakeLogic() {
            this(new PersonBuilder().withName("Added Student").withPhone("98888888").build());
        }

        private FakeLogic(Person personToAdd) {
            this.personToAdd = personToAdd;
        }

        @Override
        public CommandResult execute(String commandText) throws CommandException, ParseException {
            return switch (commandText) {
                case "add" -> {
                    people.add(personToAdd);
                    yield new CommandResult("New person added", personToAdd.getId());
                }
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
