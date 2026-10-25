package seedu.tuitionbook.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.ListChangeListener;
import seedu.tuitionbook.logic.parser.TuitionBookParser;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.person.NameContainsKeywordsPredicate;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.PersonHasRolePredicate;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.testutil.PersonBuilder;

/** Verifies query commands establish copyable refresh metadata without LogicManager or original input text. */
public class QueryRefreshCommandTest {
    @Test
    public void execute_directRoleCommand_recordsRefreshCommand() {
        ModelManager model = new ModelManager();
        new ListCommand(new PersonHasRolePredicate(Role.STUDENT)).execute(model);
        assertEquals("list r/student", model.getPersonListViewState().refreshCommand());
    }

    @Test
    public void execute_directFindCommand_recordsRefreshCommand() {
        ModelManager model = new ModelManager();
        new FindCommand(new NameContainsKeywordsPredicate(List.of("Alex", "John"))).execute(model);
        assertEquals("find Alex John", model.getPersonListViewState().refreshCommand());
    }

    @Test
    public void execute_parsedQueries_normalizesSyntaxAndPreservesCriteriaOnReplay() throws Exception {
        TuitionBookParser parser = new TuitionBookParser();
        for (String input : List.of(" LIST    r/STUDENT ", " FIND   Alex\tJohn ",
                "list t/\"study r/student t/weekly\" r/GUARDIAN t/\"\\\"Sec 4\\\"\" t/\"C:\\\\Math\\\\\"",
                "list t/\"Sec  4\" t/C++ t/Active t/Active", "find STRASSE GROẞ", "find \"Alex\" C:\\ r/student")) {
            ModelManager original = new ModelManager();
            parser.parseCommand(input).execute(original);
            String refresh = original.getPersonListViewState().refreshCommand();
            ModelManager replayed = new ModelManager();
            parser.parseCommand(refresh).execute(replayed);
            assertEquals(original.getFilteredPersonListPredicate(), replayed.getFilteredPersonListPredicate(), input);
            assertEquals(refresh, replayed.getPersonListViewState().refreshCommand(), input);
        }
        ModelManager model = new ModelManager();
        parser.parseCommand(" LIST   r/STUDENT ").execute(model);
        assertEquals("list r/student", model.getPersonListViewState().refreshCommand());
        parser.parseCommand(" FIND   Alex\tJohn ").execute(model);
        assertEquals("find Alex John", model.getPersonListViewState().refreshCommand());
    }

    @Test
    public void execute_bareList_replacesPreviousQueryAndRefreshMetadata() {
        ModelManager model = new ModelManager();
        new FindCommand(new NameContainsKeywordsPredicate(List.of("Alex"))).execute(model);
        new ListCommand().execute(model);
        assertEquals("list", model.getPersonListViewState().refreshCommand());
    }

    @Test
    public void execute_find_notifiesObserversAfterQueryMetadataIsEstablished() {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Alex Student").build());
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("Missing"));
        model.getFilteredPersonList().addListener((ListChangeListener<Person>) change -> {
            assertEquals(predicate, model.getPersonListViewState().predicate());
            assertEquals("find Missing", model.getPersonListViewState().refreshCommand());
        });
        new FindCommand(predicate).execute(model);
    }
}
