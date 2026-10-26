package seedu.tuitionbook.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.testutil.PersonBuilder;

/** Tests stable membership and live values independently of command-level view resets. */
public class WorkingSetTest {
    @Test
    public void viewSnapshot_copiesMembershipAndRemainsUnmodifiable() {
        List<UUID> source = new ArrayList<>(List.of(UUID.randomUUID()));
        PersonListViewState snapshot = new PersonListViewState(source, person -> true, "list");
        source.clear();
        assertEquals(1, snapshot.personIds().size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.personIds().clear());
    }

    @Test
    public void addPerson_reorderedWorkingSet_appendsWithoutReorderingOrAddingHiddenMatches() {
        Person alex = new PersonBuilder().withName("Alex Student").withTags("Active").build();
        Person john = new PersonBuilder().withName("John Student").withTags("Active").build();
        Person hidden = new PersonBuilder().withName("Hidden Student").withTags().build();
        Model model = new ModelManager();
        model.addPerson(alex);
        model.addPerson(john);
        model.addPerson(hidden);
        model.restorePersonListViewState(new PersonListViewState(List.of(john.getId(), alex.getId()),
                person -> !person.getTags().isEmpty(), "list t/Active"));
        Person editedHidden = new PersonBuilder(hidden).withTags("Active").build();
        model.setPerson(hidden, editedHidden);
        Person eve = new PersonBuilder().withName("Eve Student").withTags("Active").build();
        model.addPerson(eve);
        assertEquals(List.of(john, alex, eve), model.getFilteredPersonList());
        model.updateFilteredPersonList(model.getPersonListQueryPredicate(),
                model.getPersonListViewState().refreshCommand());
        assertEquals(List.of(alex, john, editedHidden, eve), model.getFilteredPersonList());
    }

    @Test
    public void setPerson_stopsMatching_keepsCompleteIndexMappingAndLiveValues() {
        Person alex = new PersonBuilder().withName("Alex Student").withTags("Active").build();
        Person john = new PersonBuilder().withName("John Student").withTags("Active").build();
        Model model = new ModelManager();
        model.addPerson(alex);
        model.addPerson(john);
        model.updateFilteredPersonList(person -> !person.getTags().isEmpty(), "list t/Active");

        Person editedAlex = new PersonBuilder(alex).withTags().build();
        model.setPerson(alex, editedAlex);

        assertEquals(List.of(editedAlex, john), model.getFilteredPersonList());
        assertFalse(model.getPersonListQueryPredicate().test(editedAlex));
    }

    @Test
    public void setPerson_hiddenContactStartsMatching_doesNotInsertUntilExplicitRefresh() {
        Person hidden = new PersonBuilder().withName("Hidden Student").withTags().build();
        Person visible = new PersonBuilder().withName("Visible Student").withTags("Active").build();
        Model model = new ModelManager();
        model.addPerson(hidden);
        model.addPerson(visible);
        model.updateFilteredPersonList(person -> !person.getTags().isEmpty(), "list t/Active");

        Person editedHidden = new PersonBuilder(hidden).withTags("Active").build();
        model.setPerson(hidden, editedHidden);
        assertEquals(List.of(visible), model.getFilteredPersonList());

        model.updateFilteredPersonList(model.getPersonListQueryPredicate(),
                model.getPersonListViewState().refreshCommand());
        assertEquals(List.of(editedHidden, visible), model.getFilteredPersonList());
    }
}
