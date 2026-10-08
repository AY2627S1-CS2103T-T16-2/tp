package seedu.tuitionbook.testutil;

import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.person.Person;

/**
 * A utility class to help with building TuitionBook objects.
 * Example usage: <br>
 *     {@code TuitionBook ab = new TuitionBookBuilder().withPerson("John", "Doe").build();}
 */
public class TuitionBookBuilder {

    private TuitionBook tuitionBook;

    public TuitionBookBuilder() {
        tuitionBook = new TuitionBook();
    }

    public TuitionBookBuilder(TuitionBook tuitionBook) {
        this.tuitionBook = tuitionBook;
    }

    /**
     * Adds a new {@code Person} to the {@code TuitionBook} that we are building.
     */
    public TuitionBookBuilder withPerson(Person person) {
        tuitionBook.addPerson(person);
        return this;
    }

    public TuitionBook build() {
        return tuitionBook;
    }
}
