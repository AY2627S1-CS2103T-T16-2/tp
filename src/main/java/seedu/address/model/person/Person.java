package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    // Identity fields
    private final UUID id;
    private final Role role;
    private final Name name;
    private final Phone phone;
    private final Email email;

    // Data fields
    private final Address address;
    private final Set<Tag> tags = new HashSet<>();
    private final UUID guardianId;

    /**
     * Every field must be present and not null, except {@code guardianId} which may be null
     * to represent a person with no linked guardian.
     */
    public Person(UUID id, Role role, UUID guardianId, Name name, Phone phone, Email email, Address address,
                  Set<Tag> tags) {
        requireAllNonNull(id, role, name, phone, email, address, tags);
        this.id = id;
        this.role = role;
        this.guardianId = guardianId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.tags.addAll(tags);
    }

    /**
     * Convenience constructor for a new person with a freshly generated id and no linked guardian.
     */
    public Person(Role role, Name name, Phone phone, Email email, Address address, Set<Tag> tags) {
        this(UUID.randomUUID(), role, null, name, phone, email, address, tags);
    }

    /**
     * Transitional constructor that defaults the role to {@code Role.STUDENT}.
     * Kept so that callers that do not handle roles yet continue to compile;
     * to be removed once all callers supply a role explicitly.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags) {
        this(Role.STUDENT, name, phone, email, address, tags);
    }

    public UUID getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    /**
     * Returns the id of this person's linked guardian, or an empty optional if no guardian is linked.
     */
    public Optional<UUID> getGuardianId() {
        return Optional.ofNullable(guardianId);
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns a copy of this person linked to the guardian with the given id.
     * The copy keeps this person's own id.
     */
    public Person withGuardian(UUID guardianId) {
        requireNonNull(guardianId);
        return new Person(id, role, guardianId, name, phone, email, address, tags);
    }

    /**
     * Returns a copy of this person with no linked guardian.
     * The copy keeps this person's own id.
     */
    public Person clearGuardian() {
        return new Person(id, role, null, name, phone, email, address, tags);
    }

    /**
     * Returns true if both persons have the same name.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getName().equals(getName());
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     * The generated {@code id} is deliberately excluded, so two persons with
     * identical user-visible fields are considered equal.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return role.equals(otherPerson.role)
                && Objects.equals(guardianId, otherPerson.guardianId)
                && name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && address.equals(otherPerson.address)
                && tags.equals(otherPerson.tags);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(role, guardianId, name, phone, email, address, tags);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("role", role)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("tags", tags)
                .add("guardianId", guardianId)
                .toString();
    }

}
