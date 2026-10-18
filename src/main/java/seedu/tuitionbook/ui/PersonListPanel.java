package seedu.tuitionbook.ui;

import static seedu.tuitionbook.commons.util.CollectionUtil.requireAllNonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.tuitionbook.commons.core.LogsCenter;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);
    private final Map<UUID, Person> personsById = new HashMap<>();
    private final ObservableList<Person> allPersons;

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a {@code PersonListPanel} using the filtered persons for display and all persons for
     * relationship lookup.
     *
     * @param filteredPersons persons selected by the active filter
     * @param allPersons all persons available for relationship lookup
     */
    public PersonListPanel(ObservableList<Person> filteredPersons, ObservableList<Person> allPersons) {
        super(FXML);
        requireAllNonNull(filteredPersons, allPersons);
        this.allPersons = allPersons;
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
     * Returns the names of students linked to the guardian in contact-list order, or {@code "-"} if there are none.
     *
     * @param person guardian whose linked student names are required
     * @param allPersons all persons in contact-list order
     * @return comma-separated linked student names, or {@code "-"}
     */
    static String resolveLinkedStudentNames(Person person, List<Person> allPersons) {
        requireAllNonNull(person, allPersons);
        if (person.getRole() != Role.GUARDIAN) {
            return "-";
        }
        String linkedStudentNames = allPersons.stream()
                .filter(candidate -> candidate.getRole() == Role.STUDENT)
                .filter(candidate -> candidate.getGuardianId().filter(person.getId()::equals).isPresent())
                .map(candidate -> candidate.getName().fullName)
                .collect(Collectors.joining(", "));
        return linkedStudentNames.isEmpty() ? "-" : linkedStudentNames;
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
                String relationshipNames = person.getRole() == Role.STUDENT
                        ? resolveGuardianName(person, personsById)
                        : resolveLinkedStudentNames(person, allPersons);
                setGraphic(new PersonCard(person, getIndex() + 1, relationshipNames).getRoot());
            }
        }
    }

}
