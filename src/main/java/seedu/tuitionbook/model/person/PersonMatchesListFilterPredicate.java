package seedu.tuitionbook.model.person;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Predicate;

import seedu.tuitionbook.commons.util.ToStringBuilder;

/**
 * Tests whether a person matches the optional role and every requested tag.
 * Tags are matched exactly, ignoring case; partial tag names do not match.
 */
public class PersonMatchesListFilterPredicate implements Predicate<Person> {
    private final Role role;
    private final List<String> tags;

    /**
     * Creates a filter with an optional role and an immutable copy of the requested tag names.
     *
     * @param role Required role, or null to match any role.
     * @param tags Requested tag names. An empty list imposes no tag restriction.
     * @throws NullPointerException if {@code tags} or any of its elements is null.
     */
    public PersonMatchesListFilterPredicate(Role role, List<String> tags) {
        this.role = role;
        this.tags = List.copyOf(requireNonNull(tags));
    }

    /** Returns the required role, or null when the query matches either role. */
    public Role getRole() {
        return role;
    }

    /** Returns the immutable requested tag names, preserving their internal whitespace and spelling. */
    public List<String> getTags() {
        return tags;
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
