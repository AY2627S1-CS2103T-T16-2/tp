package seedu.tuitionbook.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.stage.Stage;

/** Tests the Help window's command summary and User Guide actions. */
public class HelpWindowTest {

    private static final List<String> IMPLEMENTED_COMMAND_SYNTAX = List.of(
            "add r/ROLE n/NAME p/PHONE [e/EMAIL] [a/ADDRESS] [t/TAG]...",
            "list [r/ROLE] [t/TAG]...",
            "view INDEX",
            "edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]...",
            "link STUDENT_INDEX GUARDIAN_INDEX",
            "unlink STUDENT_INDEX",
            "find KEYWORD [MORE_KEYWORDS]...",
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
            HelpWindow helpWindow = new HelpWindow(new Stage(), successfulDesktop(new AtomicReference<>()));
            TextArea commandSummary = lookup(helpWindow, "#commandSummary", TextArea.class);
            Button openButton = lookup(helpWindow, "#openUserGuideButton", Button.class);
            Button copyButton = lookup(helpWindow, "#copyButton", Button.class);
            Label helpMessage = lookup(helpWindow, "#helpMessage", Label.class);

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
    public void openUserGuide_successRunsOffFxThreadAndDelegatesUserGuideUri() throws Exception {
        AtomicReference<URI> openedUri = new AtomicReference<>();
        AtomicReference<Thread> browserThread = new AtomicReference<>();
        CountDownLatch browserStarted = new CountDownLatch(1);
        CountDownLatch allowBrowserToFinish = new CountDownLatch(1);
        DesktopAdapter desktopAdapter = new DesktopAdapter(() -> true, () -> true, uri -> {
            browserThread.set(Thread.currentThread());
            browserStarted.countDown();
            await(allowBrowserToFinish);
            openedUri.set(uri);
        });
        AtomicReference<HelpWindow> helpWindowReference = new AtomicReference<>();
        AtomicReference<Thread> fxThread = new AtomicReference<>();

        JavaFxTestUtil.runOnFxThread(() -> {
            fxThread.set(Thread.currentThread());
            HelpWindow helpWindow = new HelpWindow(new Stage(), desktopAdapter);
            helpWindowReference.set(helpWindow);
            lookup(helpWindow, "#openUserGuideButton", Button.class).fire();
        });

        assertTrue(browserStarted.await(3, TimeUnit.SECONDS));
        HelpWindow helpWindow = helpWindowReference.get();
        JavaFxTestUtil.runOnFxThread(() -> {
            Button openButton = lookup(helpWindow, "#openUserGuideButton", Button.class);
            Label browserStatus = lookup(helpWindow, "#browserStatus", Label.class);
            assertTrue(openButton.isDisabled());
            assertEquals(HelpWindow.OPEN_GUIDE_PENDING, browserStatus.getText());
        });
        assertFalse(fxThread.get() == browserThread.get());

        allowBrowserToFinish.countDown();
        assertTrue(JavaFxTestUtil.waitForFxCondition(() ->
                HelpWindow.OPEN_GUIDE_SUCCESS.equals(
                        lookup(helpWindow, "#browserStatus", Label.class).getText())));
        assertEquals(URI.create(HelpWindow.USERGUIDE_URL), openedUri.get());
        JavaFxTestUtil.runOnFxThread(() ->
                assertFalse(lookup(helpWindow, "#openUserGuideButton", Button.class).isDisabled()));
    }

    @Test
    public void openUserGuide_desktopUnavailable_showsFallbackAndPreventsDuplicateRequest() throws Exception {
        ControllableExecutor executor = new ControllableExecutor();
        HelpWindow helpWindow = JavaFxTestUtil.callOnFxThread(() -> new HelpWindow(new Stage(),
                new DesktopAdapter(() -> false, () -> true, uri -> { }), executor));

        JavaFxTestUtil.runOnFxThread(() -> {
            Button openButton = lookup(helpWindow, "#openUserGuideButton", Button.class);
            openButton.fire();
            assertTrue(openButton.isDisabled());
            assertEquals(HelpWindow.OPEN_GUIDE_PENDING,
                    lookup(helpWindow, "#browserStatus", Label.class).getText());
            openButton.getOnAction().handle(new ActionEvent());
            assertEquals(1, executor.getSubmittedTaskCount());
        });

        executor.runPendingTask();
        waitForFxEvents();
        assertFailureStatus(helpWindow);
    }

    @Test
    public void openUserGuide_browseUnavailable_showsFallback() throws Exception {
        ControllableExecutor executor = new ControllableExecutor();
        HelpWindow helpWindow = JavaFxTestUtil.callOnFxThread(() -> new HelpWindow(new Stage(),
                new DesktopAdapter(() -> true, () -> false, uri -> { }), executor));

        fireAndComplete(helpWindow, executor);

        assertFailureStatus(helpWindow);
    }

    @Test
    public void openUserGuide_ioFailureThenSuccess_recoversAndCopiesFallbackUrl() throws Exception {
        AtomicInteger attempts = new AtomicInteger();
        AtomicReference<URI> openedUri = new AtomicReference<>();
        DesktopAdapter desktopAdapter = new DesktopAdapter(() -> true, () -> true, uri -> {
            if (attempts.getAndIncrement() == 0) {
                throw new IOException("Synthetic browser failure");
            }
            openedUri.set(uri);
        });
        ControllableExecutor executor = new ControllableExecutor();
        HelpWindow helpWindow = JavaFxTestUtil.callOnFxThread(() ->
                new HelpWindow(new Stage(), desktopAdapter, executor));

        fireAndComplete(helpWindow, executor);
        assertFailureStatus(helpWindow);
        JavaFxTestUtil.runOnFxThread(() -> {
            Button copyButton = lookup(helpWindow, "#copyButton", Button.class);
            copyButton.fire();
            assertEquals(HelpWindow.USERGUIDE_URL, Clipboard.getSystemClipboard().getString());
        });

        fireAndComplete(helpWindow, executor);

        JavaFxTestUtil.runOnFxThread(() -> {
            Label browserStatus = lookup(helpWindow, "#browserStatus", Label.class);
            assertEquals(HelpWindow.OPEN_GUIDE_SUCCESS, browserStatus.getText());
            assertTrue(browserStatus.isVisible());
            assertTrue(browserStatus.isManaged());
            assertFalse(browserStatus.getStyleClass().contains("error"));
            assertFalse(lookup(helpWindow, "#openUserGuideButton", Button.class).isDisabled());
        });
        assertEquals(URI.create(HelpWindow.USERGUIDE_URL), openedUri.get());
    }

    @Test
    public void openUserGuide_runtimeFailure_showsFallback() throws Exception {
        ControllableExecutor executor = new ControllableExecutor();
        HelpWindow helpWindow = JavaFxTestUtil.callOnFxThread(() -> new HelpWindow(new Stage(),
                new DesktopAdapter(() -> true, () -> true, uri -> {
                    throw new IllegalStateException("Synthetic runtime failure");
                }), executor));

        fireAndComplete(helpWindow, executor);

        assertFailureStatus(helpWindow);
    }

    @Test
    public void openUserGuide_executorFailure_showsFallbackAndEnablesRetry() throws Exception {
        HelpWindow helpWindow = JavaFxTestUtil.callOnFxThread(() -> new HelpWindow(new Stage(),
                successfulDesktop(new AtomicReference<>()), action -> {
                    throw new IllegalStateException("Synthetic executor failure");
                }));

        JavaFxTestUtil.runOnFxThread(() ->
                lookup(helpWindow, "#openUserGuideButton", Button.class).fire());

        assertFailureStatus(helpWindow);
    }

    @Test
    public void minimumSize_failureFallbackAndControlsRemainUsable() throws Exception {
        ControllableExecutor executor = new ControllableExecutor();
        HelpWindow helpWindow = JavaFxTestUtil.callOnFxThread(() -> new HelpWindow(new Stage(),
                new DesktopAdapter(() -> false, () -> false, uri -> { }), executor));
        fireAndComplete(helpWindow, executor);

        JavaFxTestUtil.runOnFxThread(() -> {
            Stage stage = helpWindow.getRoot();
            try {
                helpWindow.show();
                stage.setWidth(stage.getMinWidth());
                stage.setHeight(stage.getMinHeight());
                stage.getScene().getRoot().applyCss();
                stage.getScene().getRoot().layout();

                TextArea commandSummary = lookup(helpWindow, "#commandSummary", TextArea.class);
                Label browserStatus = lookup(helpWindow, "#browserStatus", Label.class);
                Button openButton = lookup(helpWindow, "#openUserGuideButton", Button.class);
                Button copyButton = lookup(helpWindow, "#copyButton", Button.class);
                ScrollBar verticalScrollBar = commandSummary.lookupAll(".scroll-bar").stream()
                        .filter(ScrollBar.class::isInstance)
                        .map(ScrollBar.class::cast)
                        .filter(scrollBar -> scrollBar.getOrientation() == Orientation.VERTICAL)
                        .findFirst()
                        .orElseThrow();

                assertEquals(HelpWindow.OPEN_GUIDE_FAILURE, browserStatus.getText());
                assertTrue(browserStatus.isVisible());
                assertTrue(browserStatus.isManaged());
                assertTrue(verticalScrollBar.isVisible());
                assertWithinScene(openButton);
                assertWithinScene(copyButton);
                assertWithinScene(browserStatus);
            } finally {
                stage.hide();
            }
        });
    }

    private static DesktopAdapter successfulDesktop(AtomicReference<URI> openedUri) {
        return new DesktopAdapter(() -> true, () -> true, openedUri::set);
    }

    private static void fireAndComplete(HelpWindow helpWindow, ControllableExecutor executor) throws Exception {
        JavaFxTestUtil.runOnFxThread(() ->
                lookup(helpWindow, "#openUserGuideButton", Button.class).fire());
        executor.runPendingTask();
        waitForFxEvents();
    }

    private static void waitForFxEvents() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> { });
    }

    private static void assertFailureStatus(HelpWindow helpWindow) throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            Label browserStatus = lookup(helpWindow, "#browserStatus", Label.class);
            Button copyButton = lookup(helpWindow, "#copyButton", Button.class);
            assertEquals(HelpWindow.OPEN_GUIDE_FAILURE, browserStatus.getText());
            assertTrue(browserStatus.isVisible());
            assertTrue(browserStatus.isManaged());
            assertTrue(browserStatus.getStyleClass().contains("error"));
            assertFalse(copyButton.isDisabled());
            assertTrue(copyButton.isVisible());
            assertFalse(lookup(helpWindow, "#openUserGuideButton", Button.class).isDisabled());
        });
    }

    private static void assertWithinScene(Node node) {
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        assertTrue(bounds.getMinX() >= 0);
        assertTrue(bounds.getMinY() >= 0);
        assertTrue(bounds.getMaxX() <= node.getScene().getWidth());
        assertTrue(bounds.getMaxY() <= node.getScene().getHeight());
    }

    private static <T> T lookup(HelpWindow helpWindow, String selector, Class<T> nodeType) {
        return nodeType.cast(helpWindow.getRoot().getScene().lookup(selector));
    }

    private static void await(CountDownLatch latch) throws IOException {
        try {
            if (!latch.await(3, TimeUnit.SECONDS)) {
                throw new IOException("Timed out waiting for the browser test to continue");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for the browser test", exception);
        }
    }

    private static final class ControllableExecutor implements Executor {
        private final AtomicInteger submittedTaskCount = new AtomicInteger();
        private final AtomicReference<Runnable> pendingTask = new AtomicReference<>();

        @Override
        public void execute(Runnable command) {
            submittedTaskCount.incrementAndGet();
            if (!pendingTask.compareAndSet(null, command)) {
                throw new IllegalStateException("A browser task is already pending");
            }
        }

        int getSubmittedTaskCount() {
            return submittedTaskCount.get();
        }

        void runPendingTask() {
            Runnable task = pendingTask.getAndSet(null);
            assertNotNull(task);
            task.run();
        }
    }
}
