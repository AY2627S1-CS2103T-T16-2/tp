package seedu.address.testutil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.tag.Tag;
import seedu.address.model.util.SampleDataUtil;

/**
 * A utility class to help with building Person objects.
 */
public class PersonBuilder {

    public static final String DEFAULT_NAME = "Amy Bee";
    public static final String DEFAULT_PHONE = "85355255";
    public static final String DEFAULT_EMAIL = "amy@gmail.com";
    public static final String DEFAULT_ADDRESS = "123, Jurong West Ave 6, #08-111";
    public static final Role DEFAULT_ROLE = Role.STUDENT;

    private UUID id;
    private Role role;
    private UUID guardianId;
    private Name name;
    private Phone phone;
    private Email email;
    private Address address;
    private Set<Tag> tags;

    /**
     * Creates a {@code PersonBuilder} with the default details.
     */
    public PersonBuilder() {
        id = UUID.randomUUID();
        role = DEFAULT_ROLE;
        guardianId = null;
        name = new Name(DEFAULT_NAME);
        phone = new Phone(DEFAULT_PHONE);
        email = new Email(DEFAULT_EMAIL);
        address = new Address(DEFAULT_ADDRESS);
        tags = new HashSet<>();
    }

    /**
     * Initializes the PersonBuilder with the data of {@code personToCopy}.
     */
    public PersonBuilder(Person personToCopy) {
        id = personToCopy.getId();
        role = personToCopy.getRole();
        guardianId = personToCopy.getGuardianId().orElse(null);
        name = personToCopy.getName();
        phone = personToCopy.getPhone();
        email = personToCopy.getEmail().orElse(null);
        address = personToCopy.getAddress().orElse(null);
        tags = new HashSet<>(personToCopy.getTags());
    }

    /**
     * Sets the {@code Name} of the {@code Person} that we are building.
     */
    public PersonBuilder withName(String name) {
        this.name = new Name(name);
        return this;
    }

    /**
     * Parses the {@code tags} into a {@code Set<Tag>} and sets it to the {@code Person} that we are building.
     */
    public PersonBuilder withTags(String ... tags) {
        this.tags = SampleDataUtil.getTagSet(tags);
        return this;
    }

    /**
     * Sets the {@code Address} of the {@code Person} that we are building.
     */
    public PersonBuilder withAddress(String address) {
        this.address = new Address(address);
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code Person} that we are building.
     */
    public PersonBuilder withPhone(String phone) {
        this.phone = new Phone(phone);
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code Person} that we are building.
     */
    public PersonBuilder withEmail(String email) {
        this.email = new Email(email);
        return this;
    }

    /**
     * Sets the email of the {@code Person} that we are building to be absent.
     */
    public PersonBuilder withoutEmail() {
        this.email = null;
        return this;
    }

    /**
     * Sets the address of the {@code Person} that we are building to be absent.
     */
    public PersonBuilder withoutAddress() {
        this.address = null;
        return this;
    }

    /**
     * Sets the id of the {@code Person} that we are building.
     */
    public PersonBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the {@code Role} of the {@code Person} that we are building.
     */
    public PersonBuilder withRole(Role role) {
        this.role = role;
        return this;
    }

    /**
     * Sets the guardian link of the {@code Person} that we are building to the given guardian's id.
     */
    public PersonBuilder withGuardian(Person guardian) {
        this.guardianId = guardian.getId();
        return this;
    }

    /**
     * Sets the guardian link of the {@code Person} that we are building to the given guardian id.
     */
    public PersonBuilder withGuardianId(UUID guardianId) {
        this.guardianId = guardianId;
        return this;
    }

    public Person build() {
        return new Person(id, role, guardianId, name, phone, email, address, tags);
    }

}
