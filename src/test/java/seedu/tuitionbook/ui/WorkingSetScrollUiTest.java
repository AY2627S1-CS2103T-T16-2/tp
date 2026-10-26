package seedu.tuitionbook.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.stage.Stage;
import seedu.tuitionbook.commons.core.index.Index;
import seedu.tuitionbook.logic.commands.EditCommand;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.UserPrefs;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Phone;
import seedu.tuitionbook.testutil.PersonBuilder;

/** Exercises edits near the bottom of a real virtualized contact list. */
@EnabledIfEnvironmentVariable(named = "RUN_JAVAFX_TESTS", matches = "true")
public class WorkingSetScrollUiTest {
    @BeforeAll
    public static void startToolkit() {
        JavaFxTestUtil.initialize();
    }

    @Test
    public void execute_editLowInLongList_preservesScrollPosition() throws Exception {
        Fixture fixture = JavaFxTestUtil.callOnFxThread(() -> {
            TuitionBook book = new TuitionBook();
            for (int index = 0; index < 120; index++) {
                String name = "Student " + (char) ('A' + index / 26) + (char) ('A' + index % 26);
                book.addPerson(new PersonBuilder().withName(name).withPhone(Integer.toString(90000000 + index))
                        .withTags("Active").build());
            }
            ModelManager model = new ModelManager(book, new UserPrefs());
            PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList(),
                    model.getTuitionBook().getPersonList(), model::getPersonListViewState);
            Stage stage = new Stage();
            stage.setScene(new Scene(panel.getRoot(), 900, 600));
            stage.show();
            @SuppressWarnings("unchecked")
            ListView<Person> list = (ListView<Person>) panel.getRoot().lookup("#personListView");
            return new Fixture(stage, model, list);
        });
        try {
            JavaFxTestUtil.runOnFxThread(() -> fixture.list().scrollTo(90));
            assertTrue(JavaFxTestUtil.waitForFxCondition(() -> verticalScrollBar(fixture.list()).getValue() > 0.5));
            double before = JavaFxTestUtil.callOnFxThread(() -> verticalScrollBar(fixture.list()).getValue());
            JavaFxTestUtil.runOnFxThread(() -> {
                EditCommand.EditPersonDescriptor descriptor = new EditCommand.EditPersonDescriptor();
                descriptor.setPhone(new Phone("98888888"));
                new EditCommand(Index.fromOneBased(91), descriptor).execute(fixture.model());
            });
            AtomicInteger pulses = new AtomicInteger();
            assertTrue(JavaFxTestUtil.waitForFxCondition(() -> pulses.incrementAndGet() >= 3));
            JavaFxTestUtil.runOnFxThread(() -> {
                assertEquals(before, verticalScrollBar(fixture.list()).getValue(), 0.00001);
                assertEquals("98888888", fixture.list().getItems().get(90).getPhone().value);
            });
        } finally {
            JavaFxTestUtil.runOnFxThread(fixture.stage()::close);
        }
    }

    private static ScrollBar verticalScrollBar(ListView<Person> list) {
        return list.lookupAll(".scroll-bar").stream().filter(ScrollBar.class::isInstance)
                .map(ScrollBar.class::cast).filter(bar -> bar.getOrientation() == Orientation.VERTICAL)
                .findFirst().orElseThrow();
    }

    private record Fixture(Stage stage, ModelManager model, ListView<Person> list) {
    }
}
