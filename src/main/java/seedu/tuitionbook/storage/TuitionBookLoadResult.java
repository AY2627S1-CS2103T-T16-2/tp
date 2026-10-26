package seedu.tuitionbook.storage;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.tuitionbook.model.ReadOnlyTuitionBook;

/**
 * The contact data loaded from storage and diagnostics for invalid guardian links removed while loading.
 */
public record TuitionBookLoadResult(ReadOnlyTuitionBook tuitionBook, List<GuardianLinkRepair> guardianLinkRepairs) {

    /** Validates the loaded contact data and makes the diagnostics immutable. */
    public TuitionBookLoadResult {
        requireNonNull(tuitionBook);
        guardianLinkRepairs = List.copyOf(requireNonNull(guardianLinkRepairs));
    }

    /** Returns the number of guardian links removed while loading. */
    public int repairedGuardianLinkCount() {
        return guardianLinkRepairs.size();
    }
}
