package seedu.tuitionbook.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.tuitionbook.commons.core.LogsCenter;
import seedu.tuitionbook.commons.exceptions.IllegalValueException;
import seedu.tuitionbook.model.person.Address;
import seedu.tuitionbook.model.person.Email;
import seedu.tuitionbook.model.person.Name;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Phone;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Missing \"%s\" value.";
    public static final String INVALID_ID_MESSAGE = "The \"id\" value is not in the expected format.";
    public static final String NULL_TAG_MESSAGE =
            "The \"tags\" list contains null. Remove it or replace it with a tag name.";

    private static final Logger logger = LogsCenter.getLogger(JsonAdaptedPerson.class);

    private final String id;
    private final String role;
    private final String guardianId;
    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("id") String id, @JsonProperty("role") String role,
            @JsonProperty("guardianId") String guardianId, @JsonProperty("name") String name,
            @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("tags") List<JsonAdaptedTag> tags) {
        this.id = id;
        this.role = role;
        this.guardianId = guardianId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    /**
     * Constructs a legacy, pre-role JSON person for tests and backwards-compatible loading.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String address, List<JsonAdaptedTag> tags) {
        this(null, null, null, name, phone, email, address, tags);
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        id = source.getId().toString();
        role = source.getRole().name();
        guardianId = source.getGuardianId().map(UUID::toString).orElse(null);
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().map(value -> value.value).orElse(null);
        address = source.getAddress().map(value -> value.value).orElse(null);
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        return toModelTypeWithResult().person();
    }

    /**
     * Returns the converted person and whether an invalid guardian link was removed.
     */
    PersonLoadResult toModelTypeWithResult() throws IllegalValueException {
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            if (tag == null) {
                throw new IllegalValueException(NULL_TAG_MESSAGE);
            }
            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "name"));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.getConstraintMessage(name));
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "phone"));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (email != null && !Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.getConstraintMessage(email));
        }
        final Email modelEmail = email == null ? null : new Email(email);

        if (address != null && !Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.getConstraintMessage(address));
        }
        final Address modelAddress = address == null ? null : new Address(address);

        UUID modelId;
        Role modelRole;
        UUID modelGuardianId = null;
        GuardianLinkRepair.Reason repairReason = null;
        if (isLegacyPerson()) {
            modelId = UUID.randomUUID();
            modelRole = Role.STUDENT;
        } else {
            modelId = parseId(id, "id");
            if (role == null) {
                throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "role"));
            }
            if (!Role.isValidRole(role)) {
                throw new IllegalValueException(Role.MESSAGE_CONSTRAINTS);
            }
            modelRole = Role.fromString(role);
            modelGuardianId = parseGuardianId();
            repairReason = guardianId != null && modelGuardianId == null
                    ? GuardianLinkRepair.Reason.MALFORMED_GUARDIAN_ID : null;
            if (modelGuardianId != null && (modelRole != Role.STUDENT || modelGuardianId.equals(modelId))) {
                logger.warning("Discarding invalid guardian link for contact " + modelName);
                repairReason = modelRole != Role.STUDENT
                        ? GuardianLinkRepair.Reason.NON_STUDENT_GUARDIAN_LINK
                        : GuardianLinkRepair.Reason.SELF_LINK;
                modelGuardianId = null;
            }
        }

        final Set<Tag> modelTags = new HashSet<>(personTags);
        Person person = new Person(modelId, modelRole, modelGuardianId, modelName, modelPhone, modelEmail,
                modelAddress, modelTags);
        GuardianLinkRepair repair = repairReason == null ? null
                : new GuardianLinkRepair(modelName.fullName, modelId, guardianId, repairReason);
        return new PersonLoadResult(person, repair);
    }

    /** A converted contact and its guardian-link repair status. */
    record PersonLoadResult(Person person, GuardianLinkRepair repair) {
    }

    private boolean isLegacyPerson() {
        return id == null && role == null && guardianId == null;
    }

    private UUID parseGuardianId() {
        if (guardianId == null) {
            return null;
        }
        try {
            return UUID.fromString(guardianId);
        } catch (IllegalArgumentException iae) {
            logger.warning("Discarding malformed guardian link id: " + guardianId);
            return null;
        }
    }

    private UUID parseId(String value, String fieldName) throws IllegalValueException {
        if (value == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, fieldName));
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException iae) {
            throw new IllegalValueException(INVALID_ID_MESSAGE);
        }
    }

}
