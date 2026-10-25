package seedu.tuitionbook.storage;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.model.TuitionBook;

/**
 * Tests validation of the contact-data load result.
 */
public class TuitionBookLoadResultTest {

    @Test
    public void constructor_nullContacts_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TuitionBookLoadResult(null, List.of()));
    }

    @Test
    public void constructor_nullRepairs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TuitionBookLoadResult(new TuitionBook(), null));
    }
}
