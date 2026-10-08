package seedu.tuitionbook.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.tuitionbook.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.tuitionbook.testutil.Assert.assertThrows;
import static seedu.tuitionbook.testutil.TypicalPersons.ALICE;
import static seedu.tuitionbook.testutil.TypicalPersons.getTypicalTuitionBook;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.exceptions.DuplicatePersonException;
import seedu.tuitionbook.testutil.PersonBuilder;

public class TuitionBookTest {

    private final TuitionBook tuitionBook = new TuitionBook();

    @Test
    public void constructor() {
        assertEquals(List.of(), tuitionBook.getPersonList());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> tuitionBook.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyTuitionBook_replacesData() {
        TuitionBook newData = getTypicalTuitionBook();
        tuitionBook.resetData(newData);
        assertEquals(newData, tuitionBook);
    }

    @Test
    public void resetData_withDuplicatePersons_throwsDuplicatePersonException() {
        // Two persons with the same identity fields
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        List<Person> newPersons = List.of(ALICE, editedAlice);
        TuitionBookStub newData = new TuitionBookStub(newPersons);

        assertThrows(DuplicatePersonException.class, () -> tuitionBook.resetData(newData));
    }

    @Test
    public void resetData_withNormalizedDuplicatePersons_throwsDuplicatePersonException() {
        Person duplicateAlice = new PersonBuilder(ALICE).withName("alice  pauline ")
                .withPhone("9435 1253").build();
        TuitionBookStub newData = new TuitionBookStub(List.of(ALICE, duplicateAlice));

        assertThrows(DuplicatePersonException.class, () -> tuitionBook.resetData(newData));
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> tuitionBook.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInTuitionBook_returnsFalse() {
        assertFalse(tuitionBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInTuitionBook_returnsTrue() {
        tuitionBook.addPerson(ALICE);
        assertTrue(tuitionBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personWithSameIdentityFieldsInTuitionBook_returnsTrue() {
        tuitionBook.addPerson(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        assertTrue(tuitionBook.hasPerson(editedAlice));
    }

    @Test
    public void getPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> tuitionBook.getPersonList().remove(0));
    }

    @Test
    public void toStringMethod() {
        String expected = TuitionBook.class.getCanonicalName() + "{persons=" + tuitionBook.getPersonList() + "}";
        assertEquals(expected, tuitionBook.toString());
    }

    /**
     * A stub ReadOnlyTuitionBook whose persons list can violate interface constraints.
     */
    private static class TuitionBookStub implements ReadOnlyTuitionBook {
        private final ObservableList<Person> persons = FXCollections.observableArrayList();

        TuitionBookStub(Collection<Person> persons) {
            this.persons.setAll(persons);
        }

        @Override
        public ObservableList<Person> getPersonList() {
            return persons;
        }
    }

}
