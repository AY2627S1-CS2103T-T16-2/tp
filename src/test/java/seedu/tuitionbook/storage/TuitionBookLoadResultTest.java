package seedu.tuitionbook.storage;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.model.TuitionBook;

/**
 * Tests validation of the contact-data load result.
 */
public class TuitionBookLoadResultTest {

    @Test
    public void constructor_nullContacts_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TuitionBookLoadResult(null, 0));
    }

    @Test
    public void constructor_negativeRepairCount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new TuitionBookLoadResult(new TuitionBook(), -1));
    }
}
