package seedu.address.model.person;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s {@code Role} matches the given role.
 */
public class PersonHasRolePredicate implements Predicate<Person> {
    private final Role role;

    public PersonHasRolePredicate(Role role) {
        this.role = role;
    }

    @Override
    public boolean test(Person person) {
        return person.getRole() == role;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof PersonHasRolePredicate otherPersonHasRolePredicate)) {
            return false;
        }

        return role == otherPersonHasRolePredicate.role;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("role", role).toString();
    }
}
