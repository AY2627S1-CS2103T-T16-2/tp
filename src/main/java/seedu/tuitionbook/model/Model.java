package seedu.tuitionbook.model;

import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.tuitionbook.commons.core.GuiSettings;
import seedu.tuitionbook.model.person.Person;

/**
 * The API of the Model component.
 */
public interface Model {
    /** Refresh command for the full, unfiltered working set. */
    String SHOW_ALL_PERSONS_REFRESH_COMMAND = "list";

    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces TuitionBook data and resets the working set to the full, unfiltered book.
     */
    void setTuitionBook(ReadOnlyTuitionBook tuitionBook);

    /** Returns the TuitionBook */
    ReadOnlyTuitionBook getTuitionBook();

    /**
     * Returns true if a person with the same identity as {@code person} exists in TuitionBook.
     */
    boolean hasPerson(Person person);

    /**
     * Deletes the given person. If the person is a guardian, clears the guardian links of
     * all students linked to that guardian.
     * The person must exist in TuitionBook.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in TuitionBook.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in TuitionBook.
     * The person identity of {@code editedPerson} must not be the same as another existing person in TuitionBook.
     */
    void setPerson(Person target, Person editedPerson);

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Returns the originating query predicate. Retained displayed contacts may fail this predicate;
     * use {@link #getFilteredPersonList()} to inspect actual displayed membership.
     */
    Predicate<Person> getPersonListQueryPredicate();

    /** Returns a snapshot of the displayed UUIDs, their order and the originating query. */
    PersonListViewState getPersonListViewState();

    /** Restores a working-set snapshot against the current contact data. */
    void restorePersonListViewState(PersonListViewState state);

    /**
     * Replaces the working set and its equivalent refresh command together.
     * Every query, including show-all, must supply its refresh command explicitly.
     */
    void updateFilteredPersonList(Predicate<Person> predicate, String refreshCommand);
}
