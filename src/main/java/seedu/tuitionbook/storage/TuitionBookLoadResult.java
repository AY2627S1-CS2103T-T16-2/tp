package seedu.tuitionbook.storage;

import static java.util.Objects.requireNonNull;

import seedu.tuitionbook.model.ReadOnlyTuitionBook;

/**
 * The contact data loaded from storage and the number of invalid guardian links removed while loading.
 */
public record TuitionBookLoadResult(ReadOnlyTuitionBook tuitionBook, int repairedGuardianLinkCount) {

    /** Validates the loaded contact data and repair count. */
    public TuitionBookLoadResult {
        requireNonNull(tuitionBook);
        if (repairedGuardianLinkCount < 0) {
            throw new IllegalArgumentException("Repair count cannot be negative.");
        }
    }
}
