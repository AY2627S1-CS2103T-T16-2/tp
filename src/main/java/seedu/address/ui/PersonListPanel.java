package seedu.address.ui;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;
import seedu.address.model.person.Role;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);
    private final Map<UUID, Person> personsById = new HashMap<>();

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a {@code PersonListPanel} using the filtered persons for display and all persons for relationship lookup.
     *
     * @param filteredPersons persons selected by the active filter
     * @param allPersons all persons available for relationship lookup
     */
    public PersonListPanel(ObservableList<Person> filteredPersons, ObservableList<Person> allPersons) {
        super(FXML);
        requireAllNonNull(filteredPersons, allPersons);
        personListView.setItems(filteredPersons);
        rebuildPersonLookup(allPersons);
        allPersons.addListener((ListChangeListener<Person>) unused -> {
            rebuildPersonLookup(allPersons);
            personListView.refresh();
        });
        personListView.setCellFactory(listView -> new PersonListViewCell());
    }

    private void rebuildPersonLookup(ObservableList<Person> allPersons) {
        personsById.clear();
        allPersons.forEach(person -> personsById.put(person.getId(), person));
    }

    /**
     * Returns the linked guardian's name, or {@code "-"} when the link cannot resolve to a guardian.
     *
     * @param person person whose guardian name is required
     * @param personsById all persons indexed by id
     */
    static String resolveGuardianName(Person person, Map<UUID, Person> personsById) {
        requireAllNonNull(person, personsById);
        if (person.getRole() != Role.STUDENT) {
            return "-";
        }
        return person.getGuardianId()
                .map(personsById::get)
                .filter(guardian -> guardian.getRole() == Role.GUARDIAN)
                .map(guardian -> guardian.getName().fullName)
                .orElse("-");
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                String guardianName = resolveGuardianName(person, personsById);
                setGraphic(new PersonCard(person, getIndex() + 1, guardianName).getRoot());
            }
        }
    }

}
