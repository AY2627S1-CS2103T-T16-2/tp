package seedu.tuitionbook.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.tuitionbook.commons.util.AppUtil.checkArgument;
import static seedu.tuitionbook.commons.util.CollectionUtil.requireAllNonNull;

import java.text.Normalizer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import seedu.tuitionbook.commons.util.ToStringBuilder;
import seedu.tuitionbook.model.tag.Tag;

/**
 * Represents a Person in TuitionBook.
 * Guarantees: mandatory details are present and not null, field values are validated, immutable.
 */
public class Person {

    public static final String MESSAGE_ONLY_STUDENTS_CAN_HAVE_GUARDIAN =
            "Only a contact with the student role can be linked to a guardian.";
    public static final String MESSAGE_CANNOT_BE_OWN_GUARDIAN = "A contact cannot be linked to itself.";

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
     * Every mandatory field must be present and not null. {@code guardianId}, {@code email}, and
     * {@code address} may be null to represent an absent value.
     */
    public Person(UUID id, Role role, UUID guardianId, Name name, Phone phone, Email email, Address address,
                  Set<Tag> tags) {
        requireAllNonNull(id, role, name, phone, tags);
        checkArgument(guardianId == null || role == Role.STUDENT, MESSAGE_ONLY_STUDENTS_CAN_HAVE_GUARDIAN);
        checkArgument(guardianId == null || !guardianId.equals(id), MESSAGE_CANNOT_BE_OWN_GUARDIAN);
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

    /**
     * Returns this person's email, or an empty optional if no email was provided.
     */
    public Optional<Email> getEmail() {
        return Optional.ofNullable(email);
    }

    /**
     * Returns this person's address, or an empty optional if no address was provided.
     */
    public Optional<Address> getAddress() {
        return Optional.ofNullable(address);
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
     * Only a student can be linked, and never to itself; whether the id belongs to an
     * actual guardian cannot be verified here and is checked where both contacts are available.
     */
    public Person withGuardianId(UUID guardianId) {
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
     * Returns true if both persons have the same normalized name and phone number.
     * Names are compared case-insensitively after trimming surrounding spaces, collapsing consecutive spaces, and
     * applying Unicode NFC normalization. Phone numbers are compared after removing spaces and hyphens while retaining
     * a leading {@code +}; country-code equivalence is not applied. All other fields are ignored.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && normalizeNameForIdentity(otherPerson.getName().fullName)
                        .equals(normalizeNameForIdentity(getName().fullName))
                && normalizePhoneForIdentity(otherPerson.getPhone().value)
                        .equals(normalizePhoneForIdentity(getPhone().value));
    }

    /**
     * Returns a locale-independent identity representation of {@code name}.
     */
    static String normalizeNameForIdentity(String name) {
        String trimmedName = name.trim();
        StringBuilder collapsedName = new StringBuilder(trimmedName.length());
        boolean previousCharacterWasSpace = false;

        for (int i = 0; i < trimmedName.length(); i++) {
            char currentCharacter = trimmedName.charAt(i);
            if (currentCharacter != ' ' || !previousCharacterWasSpace) {
                collapsedName.append(currentCharacter);
            }
            previousCharacterWasSpace = currentCharacter == ' ';
        }

        String normalizedName = Normalizer.normalize(collapsedName, Normalizer.Form.NFC);
        String uppercaseName = normalizedName.toUpperCase(Locale.ROOT);
        return Normalizer.normalize(uppercaseName, Normalizer.Form.NFC);
    }

    /**
     * Returns the identity representation of {@code phone}, retaining a leading {@code +} while removing spaces and
     * hyphens.
     */
    private static String normalizePhoneForIdentity(String phone) {
        StringBuilder normalizedPhone = new StringBuilder(phone.length());
        for (int i = 0; i < phone.length(); i++) {
            char currentCharacter = phone.charAt(i);
            if (currentCharacter != ' ' && currentCharacter != '-') {
                normalizedPhone.append(currentCharacter);
            }
        }
        return normalizedPhone.toString();
    }

    /**
     * Returns true if both persons have the same user-visible fields:
     * role, name, phone, email, address, tags and guardian link.
     * This defines a stronger notion of equality between two persons.
     * The generated {@code id} is deliberately excluded: it is an internal identifier never
     * shown to the user, so rebuilt or edited copies of a person remain equal to the original.
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
                && Objects.equals(email, otherPerson.email)
                && Objects.equals(address, otherPerson.address)
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
