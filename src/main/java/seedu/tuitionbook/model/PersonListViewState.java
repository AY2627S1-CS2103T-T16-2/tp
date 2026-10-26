package seedu.tuitionbook.model;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import seedu.tuitionbook.model.person.Person;

/** Immutable snapshot of working-set membership, order and originating query; never persisted. */
public record PersonListViewState(List<UUID> personIds, Predicate<Person> predicate, String refreshCommand) {
    /** Copies the ordered IDs so later mutations cannot change a rollback snapshot. */
    public PersonListViewState {
        personIds = List.copyOf(personIds);
        requireNonNull(predicate);
        requireNonNull(refreshCommand);
    }
}
