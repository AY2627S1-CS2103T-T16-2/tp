package seedu.tuitionbook.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/** Tests the Help window's command summary and User Guide actions. */
public class HelpWindowTest {

    private static final List<String> IMPLEMENTED_COMMAND_SYNTAX = List.of(
            "add r/ROLE n/NAME p/PHONE [e/EMAIL] [a/ADDRESS] [t/TAG]...",
            "list [r/ROLE]",
            "view INDEX",
            "edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]...",
            "link STUDENT_INDEX GUARDIAN_INDEX",
            "unlink STUDENT_INDEX",
            "find KEYWORD [MORE_KEYWORDS]",
            "delete INDEX",
            "clear",
            "help",
            "exit");

    @BeforeAll
    public static void initializeJavaFx() {
        JavaFxTestUtil.initialize();
    }

    @Test
    public void helpWindow_hasScrollableSummaryAndGuideControls() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            HelpWindow helpWindow = new HelpWindow(new Stage(), url -> { });
            TextArea commandSummary = (TextArea) helpWindow.getRoot().getScene().lookup("#commandSummary");
            Button openButton = (Button) helpWindow.getRoot().getScene().lookup("#openUserGuideButton");
            Button copyButton = (Button) helpWindow.getRoot().getScene().lookup("#copyButton");
            Label helpMessage = (Label) helpWindow.getRoot().getScene().lookup("#helpMessage");

            assertFalse(commandSummary.isEditable());
            assertTrue(commandSummary.isWrapText());
            assertEquals(650, helpWindow.getRoot().getScene().getWidth());
            assertEquals(420, helpWindow.getRoot().getScene().getHeight());
            assertEquals(450, helpWindow.getRoot().getMinWidth());
            assertEquals(320, helpWindow.getRoot().getMinHeight());
            IMPLEMENTED_COMMAND_SYNTAX.forEach(syntax -> assertTrue(commandSummary.getText().contains(syntax)));
            assertEquals(HelpWindow.HELP_MESSAGE, helpMessage.getText());
            assertEquals("Open User Guide", openButton.getText());
            assertEquals("Copy URL", copyButton.getText());
        });
    }

    @Test
    public void openUserGuide_success_delegatesUserGuideUrl() throws Exception {
        AtomicReference<String> openedUrl = new AtomicReference<>();
        JavaFxTestUtil.runOnFxThread(() -> {
            HelpWindow helpWindow = new HelpWindow(new Stage(), openedUrl::set);
            Button openButton = (Button) helpWindow.getRoot().getScene().lookup("#openUserGuideButton");
            Label browserStatus = (Label) helpWindow.getRoot().getScene().lookup("#browserStatus");

            openButton.fire();

            assertEquals(HelpWindow.USERGUIDE_URL, openedUrl.get());
            assertEquals(HelpWindow.OPEN_GUIDE_SUCCESS, browserStatus.getText());
            assertTrue(browserStatus.isVisible());
        });
    }

    @Test
    public void openUserGuide_failure_showsFallbackAndKeepsCopyAvailable() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            HelpWindow helpWindow = new HelpWindow(new Stage(), url -> {
                throw new IOException("Synthetic browser failure");
            });
            Button openButton = (Button) helpWindow.getRoot().getScene().lookup("#openUserGuideButton");
            Button copyButton = (Button) helpWindow.getRoot().getScene().lookup("#copyButton");
            Label browserStatus = (Label) helpWindow.getRoot().getScene().lookup("#browserStatus");

            openButton.fire();

            assertEquals(HelpWindow.OPEN_GUIDE_FAILURE, browserStatus.getText());
            assertTrue(browserStatus.isVisible());
            assertFalse(copyButton.isDisabled());
            assertTrue(copyButton.isVisible());
        });
    }
}
