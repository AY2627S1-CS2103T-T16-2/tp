package seedu.tuitionbook.ui;

import static java.util.Objects.requireNonNull;
import static seedu.tuitionbook.commons.util.CollectionUtil.requireAllNonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.animation.AnimationTimer;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import seedu.tuitionbook.commons.core.LogsCenter;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    /**
     * Number of JavaFX pulses for which a scroll request is retried while the list is laid out.
     */
    private static final int SCROLL_RETRY_PULSE_COUNT = 10;
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);
    private final Map<UUID, Person> personsById = new HashMap<>();
    private final ObservableList<Person> allPersons;
    private AnimationTimer scrollAnimation;

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
        personListView.addEventFilter(ScrollEvent.ANY, unused -> stopScrollAnimation());
        personListView.addEventFilter(MouseEvent.MOUSE_PRESSED, unused -> stopScrollAnimation());
        personListView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (isListNavigationKey(event.getCode())) {
                stopScrollAnimation();
            }
        });
    }

    private boolean isListNavigationKey(KeyCode keyCode) {
        return keyCode == KeyCode.UP || keyCode == KeyCode.DOWN
                || keyCode == KeyCode.PAGE_UP || keyCode == KeyCode.PAGE_DOWN
                || keyCode == KeyCode.HOME || keyCode == KeyCode.END;
    }

    private void rebuildPersonLookup(ObservableList<Person> allPersons) {
        personsById.clear();
        allPersons.forEach(person -> personsById.put(person.getId(), person));
    }

    /**
     * Scrolls the identified displayed contact into the visible viewport.
     * Does nothing if the contact is not in the displayed list.
     */
    void scrollToPerson(UUID personId) {
        requireNonNull(personId);
        stopScrollAnimation();
        if (!scrollToPersonOnce(personId)) {
            return;
        }

        scrollAnimation = new AnimationTimer() {
            private int remainingPulseCount = SCROLL_RETRY_PULSE_COUNT;

            @Override
            public void handle(long now) {
                if (!scrollToPersonOnce(personId) || --remainingPulseCount == 0) {
                    stopScrollAnimation();
                }
            }
        };
        scrollAnimation.start();
    }

    private boolean scrollToPersonOnce(UUID personId) {
        for (int index = 0; index < personListView.getItems().size(); index++) {
            if (personListView.getItems().get(index).getId().equals(personId)) {
                personListView.scrollTo(index);
                return true;
            }
        }
        return false;
    }

    private void stopScrollAnimation() {
        if (scrollAnimation != null) {
            scrollAnimation.stop();
            scrollAnimation = null;
        }
    }

    /**
     * Returns whether an automatic scroll retry is pending.
     *
     * <p>This package-private accessor supports deterministic UI tests without relying on
     * platform-specific virtual-cell rendering.</p>
     */
    boolean hasPendingScrollAnimation() {
        return scrollAnimation != null;
    }

    /**
     * Returns the linked guardian's name and phone number, or {@code "-"} when the link cannot resolve to a guardian.
     *
     * @param person person whose guardian summary is required
     * @param personsById all persons indexed by id
     * @return linked guardian summary, or {@code "-"}
     */
    static String resolveGuardianSummary(Person person, Map<UUID, Person> personsById) {
        requireAllNonNull(person, personsById);
        if (person.getRole() != Role.STUDENT) {
            return "-";
        }
        return person.getGuardianId()
                .map(personsById::get)
                .filter(guardian -> guardian.getRole() == Role.GUARDIAN)
                .map(guardian -> guardian.getName().fullName + " (" + guardian.getPhone().value + ")")
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
                String relationshipSummary = person.getRole() == Role.STUDENT
                        ? resolveGuardianSummary(person, personsById)
                        : resolveLinkedStudentNames(person, allPersons);
                setGraphic(new PersonCard(person, getIndex() + 1, relationshipSummary).getRoot());
            }
        }
    }

}
