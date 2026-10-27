package seedu.tuitionbook.ui;

import static java.util.Objects.requireNonNull;
import static seedu.tuitionbook.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.tuitionbook.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.tuitionbook.model.Model.SHOW_ALL_PERSONS_REFRESH_COMMAND;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.animation.AnimationTimer;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import seedu.tuitionbook.commons.core.LogsCenter;
import seedu.tuitionbook.model.PersonListViewState;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    /**
     * Number of JavaFX pulses for which a scroll request is retried while the list is laid out.
     * A longer bounded window keeps scrolling reliable for large lists on slower platforms.
     */
    private static final int SCROLL_RETRY_PULSE_COUNT = 60;
    /** Number of consecutive pulses for which the target must remain visible before stopping. */
    private static final int REQUIRED_STABLE_VISIBLE_PULSE_COUNT = 3;
    /** Tolerance for fractional layout differences at the viewport boundary. */
    private static final double VIEWPORT_TOLERANCE = 2.0;
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);
    private final Map<UUID, Person> personsById = new HashMap<>();
    private final ObservableList<Person> allPersons;
    private final Supplier<PersonListViewState> viewState;
    private AnimationTimer scrollAnimation;

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a {@code PersonListPanel} using the filtered persons for display and all persons for
     * relationship lookup.
     *
     * @param filteredPersons persons in the displayed working set
     * @param allPersons all persons available for relationship lookup
     */
    public PersonListPanel(ObservableList<Person> filteredPersons, ObservableList<Person> allPersons) {
        this(filteredPersons, allPersons, () -> new PersonListViewState(
                filteredPersons.stream().map(Person::getId).toList(),
                PREDICATE_SHOW_ALL_PERSONS, SHOW_ALL_PERSONS_REFRESH_COMMAND));
    }

    /** Creates a panel that can mark retained contacts against the originating query. */
    public PersonListPanel(ObservableList<Person> filteredPersons, ObservableList<Person> allPersons,
            Supplier<PersonListViewState> viewState) {
        super(FXML);
        requireAllNonNull(filteredPersons, allPersons);
        this.allPersons = allPersons;
        this.viewState = requireNonNull(viewState);
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

    /** Scrolls the identified displayed contact into the visible viewport. */
    void scrollToPerson(UUID personId) {
        requireNonNull(personId);
        stopScrollAnimation();
        if (!scrollToPersonOnce(personId)) {
            return;
        }

        scrollAnimation = new AnimationTimer() {
            private int remainingPulseCount = SCROLL_RETRY_PULSE_COUNT;
            private int consecutiveVisiblePulseCount;

            @Override
            public void handle(long now) {
                // Timers run before this pulse's layout, so the visibility check reflects the
                // settled layout of the previous pulse. Checking before issuing another scroll
                // ensures the stop decision is never based on a frame with a scroll still pending.
                if (isPersonFullyVisible(personId)) {
                    consecutiveVisiblePulseCount++;
                    if (consecutiveVisiblePulseCount >= REQUIRED_STABLE_VISIBLE_PULSE_COUNT) {
                        stopScrollAnimation();
                        return;
                    }
                } else {
                    consecutiveVisiblePulseCount = 0;
                    if (!scrollTowardsPerson(personId)) {
                        stopScrollAnimation();
                        return;
                    }
                }
                if (--remainingPulseCount == 0) {
                    stopScrollAnimation();
                }
            }
        };
        scrollAnimation.start();
    }

    private boolean scrollToPersonOnce(UUID personId) {
        int index = indexOfPerson(personId);
        if (index < 0) {
            return false;
        }
        personListView.scrollTo(index);
        return true;
    }

    /**
     * Moves the viewport one step closer to the identified contact and reports whether the contact
     * is still displayed.
     *
     * <p>{@code ListView#scrollTo(int)} only jumps to an estimated position when the target row has
     * not been laid out yet, and retrying it can settle with the row still outside the viewport.
     * Once rows are realised, this instead advances by exact pixel amounts towards the target until
     * {@code scrollTo} can position the realised row precisely.</p>
     *
     * <p>Package-private so deterministic UI tests can exercise each stepping branch.</p>
     */
    boolean scrollTowardsPerson(UUID personId) {
        int index = indexOfPerson(personId);
        if (index < 0) {
            return false;
        }
        if (!(personListView.lookup(".virtual-flow") instanceof VirtualFlow<?> virtualFlow)) {
            personListView.scrollTo(index);
            return true;
        }
        IndexedCell<?> firstVisibleCell = virtualFlow.getFirstVisibleCell();
        IndexedCell<?> lastVisibleCell = virtualFlow.getLastVisibleCell();
        if (lastVisibleCell != null && index > lastVisibleCell.getIndex()) {
            virtualFlow.scrollPixels(virtualFlow.getHeight());
        } else if (firstVisibleCell != null && index < firstVisibleCell.getIndex()) {
            virtualFlow.scrollPixels(-virtualFlow.getHeight());
        } else {
            personListView.scrollTo(index);
        }
        return true;
    }

    private int indexOfPerson(UUID personId) {
        for (int index = 0; index < personListView.getItems().size(); index++) {
            if (personListView.getItems().get(index).getId().equals(personId)) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Returns whether the identified contact's row currently counts as visible in the viewport.
     *
     * <p>Package-private so deterministic UI tests can verify partial-visibility handling.</p>
     *
     * @param personId id of the contact to look for
     * @return whether the contact's row is visible within the viewport
     */
    boolean isPersonFullyVisible(UUID personId) {
        Bounds listBounds = personListView.localToScene(personListView.getBoundsInLocal());
        for (Node node : personListView.lookupAll(".list-cell")) {
            if (!(node instanceof ListCell<?> cell)
                    || !(cell.getItem() instanceof Person person)
                    || !person.getId().equals(personId)) {
                continue;
            }
            Bounds cellBounds = cell.localToScene(cell.getBoundsInLocal());
            if (isCellWithinViewport(listBounds, cellBounds)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the cell lies fully inside the viewport, within {@link #VIEWPORT_TOLERANCE};
     * a cell taller than the viewport only needs to overlap it.
     *
     * @param viewportBounds viewport bounds in scene coordinates
     * @param cellBounds cell bounds in scene coordinates
     * @return whether the cell counts as visible within the viewport
     */
    static boolean isCellWithinViewport(Bounds viewportBounds, Bounds cellBounds) {
        boolean fitsHorizontally = cellBounds.getMinX() >= viewportBounds.getMinX() - VIEWPORT_TOLERANCE
                && cellBounds.getMaxX() <= viewportBounds.getMaxX() + VIEWPORT_TOLERANCE;
        boolean fitsVertically = cellBounds.getMinY() >= viewportBounds.getMinY() - VIEWPORT_TOLERANCE
                && cellBounds.getMaxY() <= viewportBounds.getMaxY() + VIEWPORT_TOLERANCE;
        boolean isTallerThanViewport = cellBounds.getHeight() > viewportBounds.getHeight();
        return fitsHorizontally && (fitsVertically
                || (isTallerThanViewport && viewportBounds.intersects(cellBounds)));
    }

    private void stopScrollAnimation() {
        if (scrollAnimation != null) {
            scrollAnimation.stop();
            scrollAnimation = null;
        }
    }

    /** Returns whether an automatic scroll retry is pending for deterministic UI tests. */
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
                PersonListViewState state = viewState.get();
                String notice = state.predicate().test(person) ? ""
                        : "No longer matches the query. Refresh with: " + state.refreshCommand();
                setGraphic(new PersonCard(person, getIndex() + 1, relationshipSummary, notice).getRoot());
            }
        }
    }

}
