package seedu.tuitionbook.model;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.collections.ObservableList;
import seedu.tuitionbook.commons.util.ToStringBuilder;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.UniquePersonList;

/**
 * Wraps all data at the address-book level.
 * Duplicates are not allowed (by .isSamePerson comparison).
 */
public class TuitionBook implements ReadOnlyTuitionBook {

    private final UniquePersonList persons = new UniquePersonList();

    public TuitionBook() {}

    /**
     * Creates an TuitionBook using the Persons in the {@code toBeCopied}
     */
    public TuitionBook(ReadOnlyTuitionBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the person list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        this.persons.setPersons(persons);
    }

    /**
     * Resets the existing data of this {@code TuitionBook} with {@code newData}.
     */
    public void resetData(ReadOnlyTuitionBook newData) {
        requireNonNull(newData);

        setPersons(newData.getPersonList());
    }

    //// person-level operations

    /**
     * Returns true if a person with the same identity as {@code person} exists in TuitionBook.
     */
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return persons.contains(person);
    }

    /**
     * Adds a person to TuitionBook.
     * The person must not already exist in TuitionBook.
     */
    public void addPerson(Person p) {
        persons.add(p);
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in TuitionBook.
     * The person identity of {@code editedPerson} must not be the same as another existing person in TuitionBook.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);
    }

    /**
     * Removes {@code key} from this {@code TuitionBook}.
     * {@code key} must exist in TuitionBook.
     */
    public void removePerson(Person key) {
        persons.remove(key);
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .toString();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof TuitionBook otherTuitionBook)) {
            return false;
        }

        return persons.equals(otherTuitionBook.persons);
    }

    @Override
    public int hashCode() {
        return persons.hashCode();
    }
}
