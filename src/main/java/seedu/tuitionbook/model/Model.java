package seedu.tuitionbook.model;

import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.tuitionbook.commons.core.GuiSettings;
import seedu.tuitionbook.model.person.Person;

/**
 * The API of the Model component.
 */
public interface Model {
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
     * Replaces TuitionBook data with the data in {@code tuitionBook}.
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
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);
}
