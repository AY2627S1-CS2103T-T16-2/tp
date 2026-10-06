package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.person.Role;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";
    public static final String MESSAGE_DUPLICATE_ID = "Persons list contains duplicate id(s).";

    private static final Logger logger = LogsCenter.getLogger(JsonSerializableAddressBook.class);

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons) {
        this.persons.addAll(persons);
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        List<Person> loadedPersons = new ArrayList<>();
        Set<UUID> loadedIds = new HashSet<>();
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            Person person = jsonAdaptedPerson.toModelType();
            if (!loadedIds.add(person.getId())) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_ID);
            }
            loadedPersons.add(person);
        }

        Map<UUID, Person> personsById = new HashMap<>();
        for (Person person : loadedPersons) {
            personsById.put(person.getId(), person);
        }

        AddressBook addressBook = new AddressBook();
        for (Person person : loadedPersons) {
            Person resolvedPerson = resolveGuardianLink(person, personsById);
            if (addressBook.hasPerson(resolvedPerson)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            addressBook.addPerson(resolvedPerson);
        }
        return addressBook;
    }

    private Person resolveGuardianLink(Person person, Map<UUID, Person> personsById) {
        if (person.getRole() != Role.STUDENT || person.getGuardianId().isEmpty()) {
            return person;
        }

        Person guardian = personsById.get(person.getGuardianId().get());
        if (guardian == null || guardian.getRole() != Role.GUARDIAN) {
            logger.warning("Discarding unresolved guardian link for student " + person.getName());
            return person.clearGuardian();
        }
        return person;
    }

}
