package seedu.tuitionbook.model;

import static java.util.Objects.requireNonNull;
import static seedu.tuitionbook.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
    private final FilteredList<Person> filteredPersons;

    /**
     * Initializes a ModelManager with the given tuitionBook and userPrefs.
     */
    public ModelManager(ReadOnlyTuitionBook tuitionBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(tuitionBook, userPrefs);

        logger.fine("Initializing with TuitionBook: " + tuitionBook + " and user prefs " + userPrefs);

        this.tuitionBook = new TuitionBook(tuitionBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.tuitionBook.getPersonList());
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
    }

    @Override
    public void addPerson(Person person) {
        tuitionBook.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        tuitionBook.setPerson(target, editedPerson);
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code tuitionBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return filteredPersons;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
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
