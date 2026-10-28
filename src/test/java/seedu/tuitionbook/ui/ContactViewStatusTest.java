package seedu.tuitionbook.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.tuitionbook.logic.LogicManager;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.ReadOnlyTuitionBook;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.UserPrefs;
import seedu.tuitionbook.storage.JsonTuitionBookStorage;
import seedu.tuitionbook.storage.JsonUserPrefsStorage;
import seedu.tuitionbook.storage.StorageManager;
import seedu.tuitionbook.testutil.PersonBuilder;

/** Verifies the persistent query/count indicator through real command, storage and UI boundaries. */
public class ContactViewStatusTest {
    private static final int NARROW_WINDOW_WIDTH = 450;
    private static final int NARROW_WINDOW_HEIGHT = 600;
    private static final int LONG_QUERY_REPETITIONS = 200;
    // Allow font metrics to vary while detecting a query that wraps into excessive vertical space.
    private static final int MAX_SINGLE_LINE_QUERY_HEIGHT = 40;
    private static final int MAX_COMPACT_STATUS_HEIGHT = 100;

    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void initializeJavaFx() {
        JavaFxTestUtil.initialize();
    }

    @Test
    public void queries_distinguishUnfilteredEmptyAndAllMatchingViews() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            try (WindowFixture fixture = new WindowFixture(temporaryFolder)) {
                fixture.assertAllContacts(0);
                fixture.execute("add r/student n/Alex Student p/90000001 t/Active");
                fixture.execute("add r/student n/John Student p/90000002 t/Active");
                fixture.assertAllContacts(2);
                fixture.execute("LIST r/STUDENT");
                fixture.assertQuery("list r/student", 2, 2, 2, 0);
                fixture.execute("find Missing");
                fixture.assertQuery("find Missing", 0, 2, 0, 0);
                fixture.execute("list r/invalid");
                fixture.assertQuery("find Missing", 0, 2, 0, 0);
                fixture.execute("list");
                fixture.assertAllContacts(2);
            }
        });
    }

    @Test
    public void mutationsAndDetails_keepQueryVisibleAndSeparateRetainedContacts() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            try (WindowFixture fixture = new WindowFixture(temporaryFolder)) {
                fixture.execute("add r/student n/Alex Student p/90000001 t/Active");
                fixture.execute("add r/student n/John Student p/90000002 t/Active");
                fixture.execute("add r/guardian n/Grace Guardian p/90000003");
                fixture.execute("list t/Active");
                fixture.assertQuery("list t/Active", 2, 3, 2, 0);
                fixture.execute("untag 1 t/Active");
                fixture.assertQuery("list t/Active", 2, 3, 1, 1);
                fixture.execute("tag 1 t/Active");
                fixture.assertQuery("list t/Active", 2, 3, 2, 0);
                fixture.execute("add r/student n/Eve Student p/90000004 t/Active");
                fixture.assertQuery("list t/Active", 3, 4, 3, 0);
                fixture.execute("add r/student n/Hidden Student p/90000005");
                fixture.assertQuery("list t/Active", 3, 5, 3, 0);
                fixture.execute("delete 2");
                fixture.assertQuery("list t/Active", 2, 4, 2, 0);
                fixture.execute("find Alex");
                fixture.execute("edit 1 n/Renamed Student");
                fixture.assertQuery("find Alex", 1, 4, 0, 1);
                fixture.execute("view 1");
                fixture.assertQuery("find Alex", 1, 4, 0, 1);
                assertTrue(fixture.label("contactViewCounts").getParent().isVisible());
                fixture.execute("untag 99 t/Active");
                fixture.assertQuery("find Alex", 1, 4, 0, 1);
                fixture.execute("invalid");
                fixture.assertQuery("find Alex", 1, 4, 0, 1);
                fixture.execute("find Alex");
                fixture.assertQuery("find Alex", 0, 4, 0, 0);
            }
        });
    }

    @Test
    public void failedSaves_restoreDisplayedTotalAndRetainedCounts() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            try (WindowFixture fixture = new WindowFixture(temporaryFolder)) {
                fixture.execute("add r/student n/Alex Student p/90000001 t/Active");
                fixture.execute("add r/student n/John Student p/90000002 t/Active");
                fixture.execute("list t/Active");
                fixture.execute("untag 1 t/Active");
                fixture.fileStorage.failSaving = true;
                for (String command : new String[] {"add r/student n/Eve Student p/90000003 t/Active",
                    "delete 2", "tag 1 t/Active"}) {
                    fixture.execute(command);
                    TextArea result = (TextArea) fixture.window.getRoot().getScene().lookup("#resultDisplay");
                    assertTrue(result.getText().contains("Could not save your changes"));
                    fixture.assertQuery("list t/Active", 2, 2, 1, 1);
                }
            }
        });
    }

    @Test
    public void reloadClearAndRestart_resetIndicatorOnlyWhenViewResets() throws Exception {
        JavaFxTestUtil.runOnFxThread(() -> {
            try (WindowFixture fixture = new WindowFixture(temporaryFolder)) {
                fixture.execute("add r/student n/Alex Student p/90000001");
                fixture.execute("add r/student n/John Student p/90000002");
                fixture.execute("find Alex");
                Files.writeString(fixture.fileStorage.getTuitionBookFilePath(), "{broken");
                fixture.execute("reload");
                fixture.assertQuery("find Alex", 1, 2, 1, 0);

                TuitionBook replacement = new TuitionBook();
                replacement.addPerson(new PersonBuilder().withName("Reloaded Student").build());
                fixture.fileStorage.saveTuitionBook(replacement);
                fixture.execute("reload");
                fixture.assertAllContacts(1);
                fixture.execute("find Missing");
            }
            try (WindowFixture restarted = new WindowFixture(temporaryFolder)) {
                restarted.assertAllContacts(1);
                restarted.execute("clear confirm");
                restarted.assertAllContacts(0);
                restarted.execute("find Missing");
                restarted.assertQuery("find Missing", 0, 0, 0, 0);
            }
        });
    }

    @Test
    public void narrowWindow_longQueryDoesNotPushCountsOrCommandBoxOutsideWindow() throws Exception {
        String query = "find " + "LongQuery".repeat(LONG_QUERY_REPETITIONS);
        WindowFixture fixture = JavaFxTestUtil.callOnFxThread(() -> {
            WindowFixture created = new WindowFixture(temporaryFolder);
            created.execute(query);
            created.window.getPrimaryStage().setWidth(NARROW_WINDOW_WIDTH);
            created.window.getPrimaryStage().setHeight(NARROW_WINDOW_HEIGHT);
            created.window.show();
            return created;
        });
        try {
            assertTrue(JavaFxTestUtil.waitForFxCondition(() ->
                    fixture.window.getRoot().getScene().getWidth() <= NARROW_WINDOW_WIDTH));
            JavaFxTestUtil.runOnFxThread(() -> {
                fixture.window.getRoot().getScene().getRoot().applyCss();
                fixture.window.getRoot().getScene().getRoot().layout();
                Label queryLabel = fixture.label("contactViewQuery");
                Label countsLabel = fixture.label("contactViewCounts");
                VBox status = (VBox) queryLabel.getParent();
                assertEquals("Query: " + query, queryLabel.getTooltip().getText());
                assertTrue(queryLabel.getHeight() < MAX_SINGLE_LINE_QUERY_HEIGHT, "Query should occupy a single line");
                assertTrue(countsLabel.getHeight() > 0);
                assertTrue(status.getHeight() < MAX_COMPACT_STATUS_HEIGHT,
                        "A long query should not consume the contact list");
                assertTrue(countsLabel.localToScene(countsLabel.getBoundsInLocal()).getMaxX()
                        <= fixture.window.getRoot().getScene().getWidth());
                assertTrue(countsLabel.localToScene(countsLabel.getBoundsInLocal()).getMaxY()
                        <= fixture.window.getRoot().getScene().getHeight());
                assertTrue(fixture.commandBox.localToScene(fixture.commandBox.getBoundsInLocal()).getMinY() >= 0);
                assertTrue(fixture.commandBox.localToScene(fixture.commandBox.getBoundsInLocal()).getMaxY()
                        <= fixture.window.getRoot().getScene().getHeight());
                assertFalse(queryLabel.isFocusTraversable());
            });
        } finally {
            JavaFxTestUtil.runOnFxThread(fixture::close);
        }
    }

    private static class WindowFixture implements AutoCloseable {
        private final FailingStorage fileStorage;
        private final MainWindow window;
        private final TextField commandBox;
        private String lastCommand = "initial load";

        WindowFixture(Path folder) throws Exception {
            fileStorage = new FailingStorage(folder.resolve("contacts.json"));
            StorageManager storage = new StorageManager(fileStorage,
                    new JsonUserPrefsStorage(folder.resolve("prefs.json")));
            ModelManager model = new ModelManager(
                    storage.readTuitionBook().orElseGet(TuitionBook::new), new UserPrefs());
            window = new MainWindow(new Stage(), new LogicManager(model, storage),
                    fileStorage.getTuitionBookFilePath());
            window.fillInnerParts();
            commandBox = (TextField) window.getRoot().getScene().lookup("#commandTextField");
        }

        void execute(String command) {
            lastCommand = command;
            commandBox.setText(command);
            commandBox.getOnAction().handle(new ActionEvent());
        }

        Label label(String id) {
            Label label = (Label) window.getRoot().getScene().lookup("#" + id);
            assertNotNull(label, "Missing persistent contact-view indicator: " + id);
            return label;
        }

        void assertAllContacts(int total) {
            String context = "after: " + lastCommand;
            assertEquals("All contacts: " + total, label("contactViewCounts").getText(), context);
            assertFalse(label("contactViewQuery").isVisible(), context);
            assertFalse(label("contactViewQuery").isManaged(), context);
        }

        void assertQuery(String query, int shown, int total, int matching, int retained) {
            String context = "after: " + lastCommand;
            assertTrue(label("contactViewQuery").isVisible(), context);
            assertEquals("Query: " + query, label("contactViewQuery").getText(), context);
            assertEquals("Query: " + query, label("contactViewQuery").getTooltip().getText(), context);
            assertEquals("Shown: " + shown + " of " + total + " contacts · Matching: " + matching
                    + " · Retained: " + retained, label("contactViewCounts").getText(), context);
        }

        @Override
        public void close() {
            window.getPrimaryStage().close();
        }
    }

    private static class FailingStorage extends JsonTuitionBookStorage {
        private boolean failSaving;

        FailingStorage(Path filePath) {
            super(filePath);
        }

        @Override
        public void saveTuitionBook(ReadOnlyTuitionBook tuitionBook) throws IOException {
            if (failSaving) {
                throw new IOException("Synthetic save failure");
            }
            super.saveTuitionBook(tuitionBook);
        }
    }
}
