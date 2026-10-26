package seedu.tuitionbook.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.stage.Stage;
import seedu.tuitionbook.logic.LogicManager;
import seedu.tuitionbook.logic.commands.CommandResult;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.storage.JsonTuitionBookStorage;
import seedu.tuitionbook.storage.JsonUserPrefsStorage;
import seedu.tuitionbook.storage.StorageManager;

/**
 * Tests command input handling for pasted separators.
 */
public class CommandTextFieldTest {

    private static Map<DataFormat, Object> initialClipboard;

    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void initializeJavaFx() {
        JavaFxTestUtil.initialize();
        try {
            initialClipboard = JavaFxTestUtil.callOnFxThread(CommandTextFieldTest::clipboardContents);
        } catch (Exception exception) {
            throw new AssertionError("Unable to capture the initial clipboard", exception);
        }
    }

    @AfterAll
    public static void restoreClipboard() throws Exception {
        if (initialClipboard != null) {
            JavaFxTestUtil.runOnFxThread(() -> Clipboard.getSystemClipboard().setContent(initialClipboard));
        }
    }

    @Test
    public void replaceText_separatorsBecomeSpacesAndPreserveCaret() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            for (String separator : List.of("\t", "\t\t", "\n", "\r", "\r\n",
                    "\u000B", "\u000C", "\u0085", "\u2028", "\u2029")) {
                CommandTextField field = new CommandTextField();
                field.setText("add Wei Ming");
                field.positionCaret(field.getLength());
                field.replaceSelection(separator + "Tan");

                String expectedSeparator = separator.equals("\t\t") ? "  " : " ";
                String expectedText = "add Wei Ming" + expectedSeparator + "Tan";
                assertEquals(expectedText, field.getText());
                assertEquals(expectedText.length(), field.getCaretPosition());
            }

            CommandTextField field = new CommandTextField();
            field.setText("add Wei Ming");
            field.positionCaret(field.getLength());
            field.replaceSelection("\r");

            assertEquals("add Wei Ming ", field.getText());
        });
    }

    @Test
    public void replaceSelection_pastedSeparatorsReplaceSelectedText() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            CommandTextField field = new CommandTextField();
            field.setText("add old -p 91234567");
            field.selectRange(4, 7);
            field.replaceSelection("Wei Ming\tTan");

            assertEquals("add Wei Ming Tan -p 91234567", field.getText());
            assertEquals("add Wei Ming Tan".length(), field.getCaretPosition());
        });
    }

    @Test
    public void replaceText_emptyTextPreservesContentAndCaret() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            CommandTextField field = new CommandTextField();
            field.setText("add Wei Ming");
            field.positionCaret(field.getLength());

            field.replaceText(field.getLength(), field.getLength(), "");

            assertEquals("add Wei Ming", field.getText());
            assertEquals(field.getLength(), field.getCaretPosition());
            assertThrows(NullPointerException.class, () -> field.replaceText(0, 0, null));
        });
    }

    // Scenarios adapted from https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/193
    // and https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/322.
    @Test
    public void paste_tabSeparatedName_addsNameWithSpace() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            LogicManager logic = new LogicManager(model, new StorageManager(
                    new JsonTuitionBookStorage(temporaryFolder.resolve("tuitionbook.json")),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));
            CommandBox commandBox = new CommandBox(logic::execute);
            Stage stage = new Stage();
            Scene scene = new Scene(commandBox.getRoot());
            stage.setScene(scene);
            stage.show();

            CommandTextField field = (CommandTextField) scene.lookup("#commandTextField");
            try {
                ClipboardContent pasted = new ClipboardContent();
                pasted.putString("Wei Ming\tTan");
                Clipboard.getSystemClipboard().clear();
                assertTrue(Clipboard.getSystemClipboard().setContent(pasted));
                assertTrue(Clipboard.getSystemClipboard().hasString());

                field.requestFocus();
                field.setText("add r/student n/");
                field.selectRange(field.getLength(), field.getLength());
                field.paste();
                field.appendText(" p/91234567");

                assertEquals("add r/student n/Wei Ming Tan p/91234567", field.getText());
                assertTrue(model.getTuitionBook().getPersonList().isEmpty());
                field.fireEvent(new ActionEvent());
                assertEquals("Wei Ming Tan", model.getTuitionBook().getPersonList().getFirst().getName().fullName);
            } finally {
                stage.hide();
            }
        });
    }

    // Scenario adapted from https://github.com/AY2627S1-CS2103T-T16-1/tp/issues/322.
    @Test
    public void replace_lineBreaksInQuotedName_normalizesBeforeParsingAndPersists() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            int separatorIndex = 0;
            for (String separator : List.of("\n", "\r", "\r\n", "\u000B", "\u000C",
                    "\u0085", "\u2028", "\u2029")) {
                ModelManager model = new ModelManager();
                Path file = temporaryFolder.resolve("tuitionbook-" + separatorIndex + ".json");
                separatorIndex++;
                LogicManager logic = new LogicManager(model, new StorageManager(
                        new JsonTuitionBookStorage(file),
                        new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));
                String rawCommand = "AdD r/student n/\"Wei" + separator + "Ming Tan\" p/91234567";
                assertThrows(ParseException.class, () -> logic.execute(rawCommand));
                assertTrue(model.getTuitionBook().getPersonList().isEmpty());

                CommandBox commandBox = new CommandBox(logic::execute);
                Stage stage = new Stage();
                Scene scene = new Scene(commandBox.getRoot());
                stage.setScene(scene);
                stage.show();
                CommandTextField field = (CommandTextField) scene.lookup("#commandTextField");
                try {
                    field.requestFocus();
                    field.replaceText(0, field.getLength(), rawCommand);

                    assertEquals("AdD r/student n/\"Wei Ming Tan\" p/91234567", field.getText());
                    assertTrue(model.getTuitionBook().getPersonList().isEmpty());
                    field.fireEvent(new ActionEvent());
                    assertEquals("", field.getText());
                    logic.execute("ReLoAd");
                    assertEquals(1, model.getTuitionBook().getPersonList().size());
                    assertEquals("Wei Ming Tan", model.getTuitionBook().getPersonList().getFirst().getName().fullName);
                } finally {
                    stage.hide();
                }
            }
        });
    }

    @Test
    public void paste_doesNotExecuteUntilEnter() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            List<String> executedCommands = new ArrayList<>();
            CommandBox commandBox = new CommandBox(command -> {
                executedCommands.add(command);
                return new CommandResult("ok");
            });
            Stage stage = new Stage();
            Scene scene = new Scene(commandBox.getRoot());
            stage.setScene(scene);
            stage.show();

            CommandTextField field = (CommandTextField) scene.lookup("#commandTextField");
            try {
                field.requestFocus();
                field.replaceSelection("list\nfind");
                assertEquals("list find", field.getText());
                assertTrue(executedCommands.isEmpty());

                field.fireEvent(new ActionEvent());
                assertEquals(List.of("list find"), executedCommands);
            } finally {
                stage.hide();
            }
        });
    }

    private static Map<DataFormat, Object> clipboardContents() {
        Map<DataFormat, Object> contents = new HashMap<>();
        Clipboard clipboard = Clipboard.getSystemClipboard();
        for (DataFormat dataFormat : clipboard.getContentTypes()) {
            Object content = clipboard.getContent(dataFormat);
            if (content != null) {
                contents.put(dataFormat, content);
            }
        }
        return contents;
    }
}
