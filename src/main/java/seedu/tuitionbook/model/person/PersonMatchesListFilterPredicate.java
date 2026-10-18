package seedu.tuitionbook.model.person;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Predicate;

import seedu.tuitionbook.commons.util.ToStringBuilder;

/** Tests whether a person matches the optional role and all requested tags. */
public class PersonMatchesListFilterPredicate implements Predicate<Person> {
    private final Role role;
    private final List<String> tags;

    public PersonMatchesListFilterPredicate(Role role, List<String> tags) {
        this.role = role;
        this.tags = List.copyOf(requireNonNull(tags));
    }

    @Override
    public boolean test(Person person) {
        if (role != null && person.getRole() != role) {
            return false;
        }
        return tags.stream().allMatch(requested -> person.getTags().stream()
                .anyMatch(actual -> actual.tagName.equalsIgnoreCase(requested)));
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PersonMatchesListFilterPredicate that)) {
            return false;
        }
        return role == that.role && tags.equals(that.tags);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("role", role).add("tags", tags).toString();
    }
}
