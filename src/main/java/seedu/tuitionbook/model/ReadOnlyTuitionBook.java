package seedu.tuitionbook.model;

import javafx.collections.ObservableList;
import seedu.tuitionbook.model.person.Person;

/**
 * Unmodifiable view of a TuitionBook
 */
public interface ReadOnlyTuitionBook {

    /**
     * Returns an unmodifiable view of the persons list.
     * This list will not contain any duplicate persons.
     */
    ObservableList<Person> getPersonList();

}
