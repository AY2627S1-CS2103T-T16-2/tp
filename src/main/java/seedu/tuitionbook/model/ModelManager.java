package seedu.tuitionbook.model;

import static java.util.Objects.requireNonNull;
import static seedu.tuitionbook.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.tuitionbook.commons.core.GuiSettings;
import seedu.tuitionbook.commons.core.LogsCenter;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;

/**
 * Represents the in-memory model of TuitionBook data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final TuitionBook tuitionBook;
    private final UserPrefs userPrefs;
    private final ObservableList<Person> filteredPersons = FXCollections.observableArrayList();
    private final ObservableList<Person> unmodifiableFilteredPersons =
            FXCollections.unmodifiableObservableList(filteredPersons);
    private final List<UUID> displayedPersonIds = new ArrayList<>();
    private String refreshCommand = "list";
    private Predicate<Person> filteredPersonListPredicate = PREDICATE_SHOW_ALL_PERSONS;

    /**
     * Initializes a ModelManager with the given tuitionBook and userPrefs.
     */
    public ModelManager(ReadOnlyTuitionBook tuitionBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(tuitionBook, userPrefs);

        logger.fine("Initializing with TuitionBook: " + tuitionBook + " and user prefs " + userPrefs);

        this.tuitionBook = new TuitionBook(tuitionBook);
        this.userPrefs = new UserPrefs(userPrefs);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS, "list");
    }

    public ModelManager() {
        this(new TuitionBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== TuitionBook ================================================================================

    @Override
    public void setTuitionBook(ReadOnlyTuitionBook tuitionBook) {
        this.tuitionBook.resetData(tuitionBook);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS, "list");
    }

    @Override
    public ReadOnlyTuitionBook getTuitionBook() {
        return tuitionBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return tuitionBook.hasPerson(person);
    }

    @Override
    public void deletePerson(Person target) {
        requireNonNull(target);

        // Apply every relationship update to a copy so the model changes only after the
        // complete deletion transaction has succeeded.
        TuitionBook updatedTuitionBook = new TuitionBook(tuitionBook);
        List<Person> linkedStudents = tuitionBook.getPersonList().stream()
                .filter(person -> person.getRole() == Role.STUDENT)
                .filter(person -> person.getGuardianId().filter(target.getId()::equals).isPresent())
                .toList();
        for (Person student : linkedStudents) {
            updatedTuitionBook.setPerson(student, student.clearGuardian());
        }
        updatedTuitionBook.removePerson(target);
        tuitionBook.resetData(updatedTuitionBook);
        refreshDisplayedPersons();
    }

    @Override
    public void addPerson(Person person) {
        tuitionBook.addPerson(person);
        if (filteredPersonListPredicate.test(person)) {
            displayedPersonIds.add(person.getId());
        }
        refreshDisplayedPersons();
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        tuitionBook.setPerson(target, editedPerson);
        int displayedIndex = displayedPersonIds.indexOf(target.getId());
        if (displayedIndex >= 0) {
            displayedPersonIds.set(displayedIndex, editedPerson.getId());
        }
        refreshDisplayedPersons();
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns the unmodifiable working set, whose UUIDs resolve to current contact values.
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return unmodifiableFilteredPersons;
    }

    @Override
    public Predicate<Person> getPersonListQueryPredicate() {
        return filteredPersonListPredicate;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate, String refreshCommand) {
        requireAllNonNull(predicate, refreshCommand);
        List<UUID> matchingIds = tuitionBook.getPersonList().stream().filter(predicate).map(Person::getId).toList();
        filteredPersonListPredicate = predicate;
        this.refreshCommand = refreshCommand;
        displayedPersonIds.clear();
        displayedPersonIds.addAll(matchingIds);
        refreshDisplayedPersons();
    }

    @Override
    public PersonListViewState getPersonListViewState() {
        return new PersonListViewState(displayedPersonIds, filteredPersonListPredicate, refreshCommand);
    }

    @Override
    public void restorePersonListViewState(PersonListViewState state) {
        requireNonNull(state);
        displayedPersonIds.clear();
        displayedPersonIds.addAll(state.personIds());
        filteredPersonListPredicate = state.predicate();
        refreshCommand = state.refreshCommand();
        refreshDisplayedPersons();
    }

    /** Resolves surviving UUIDs to live values without re-evaluating query membership or order. */
    private void refreshDisplayedPersons() {
        Map<UUID, Person> personsById = tuitionBook.getPersonList().stream()
                .collect(Collectors.toMap(Person::getId, Function.identity()));
        displayedPersonIds.removeIf(id -> !personsById.containsKey(id));
        filteredPersons.setAll(displayedPersonIds.stream().map(personsById::get).toList());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return tuitionBook.equals(otherModelManager.tuitionBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredPersons.equals(otherModelManager.filteredPersons);
    }

}
