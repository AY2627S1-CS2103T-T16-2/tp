package seedu.tuitionbook.storage;

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

import seedu.tuitionbook.commons.core.LogsCenter;
import seedu.tuitionbook.commons.exceptions.IllegalValueException;
import seedu.tuitionbook.model.ReadOnlyTuitionBook;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;

/**
 * An Immutable TuitionBook that is serializable to JSON format.
 */
@JsonRootName(value = "tuitionbook")
class JsonSerializableTuitionBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";
    public static final String MESSAGE_DUPLICATE_ID = "Persons list contains duplicate id(s).";

    private static final Logger logger = LogsCenter.getLogger(JsonSerializableTuitionBook.class);

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableTuitionBook} with the given persons.
     */
    @JsonCreator
    public JsonSerializableTuitionBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons) {
        this.persons.addAll(persons);
    }

    /**
     * Converts a given {@code ReadOnlyTuitionBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableTuitionBook}.
     */
    public JsonSerializableTuitionBook(ReadOnlyTuitionBook source) {
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
    }

    /**
     * Converts this TuitionBook into the model's {@code TuitionBook} object.
     * First converts and validates every serialized contact so that all contact ids are available.
     * It then resolves guardian links against that complete set of contacts before adding contacts
     * to TuitionBook, which enforces the normalized name-and-phone uniqueness invariant.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public TuitionBook toModelType() throws IllegalValueException {
        List<Person> loadedPersons = new ArrayList<>();
        Set<UUID> loadedIds = new HashSet<>();
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            Person person = jsonAdaptedPerson.toModelType();
            // IDs must be unique before they can safely be used to resolve guardian links.
            if (!loadedIds.add(person.getId())) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_ID);
            }
            loadedPersons.add(person);
        }

        // Build the complete lookup first because a student can precede its guardian in the file.
        Map<UUID, Person> personsById = new HashMap<>();
        for (Person person : loadedPersons) {
            personsById.put(person.getId(), person);
        }

        TuitionBook tuitionBook = new TuitionBook();
        for (Person person : loadedPersons) {
            Person resolvedPerson = resolveGuardianLink(person, personsById);
            // TuitionBook rejects manually introduced duplicate contact identities.
            if (tuitionBook.hasPerson(resolvedPerson)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            tuitionBook.addPerson(resolvedPerson);
        }
        return tuitionBook;
    }

    /**
     * Returns {@code person} with a valid guardian link, or with its link removed when the
     * serialized link cannot refer to a guardian in this TuitionBook. Invalid links do not
     * prevent the otherwise valid contact from loading.
     *
     * @param person contact whose guardian link is being resolved
     * @param personsById all successfully converted contacts indexed by their unique ids
     * @return {@code person}, or an equivalent contact without an invalid guardian link
     */
    private Person resolveGuardianLink(Person person, Map<UUID, Person> personsById) {
        if (person.getRole() != Role.STUDENT || person.getGuardianId().isEmpty()) {
            return person;
        }

        Person guardian = personsById.get(person.getGuardianId().get());
        if (guardian == null || guardian.getRole() != Role.GUARDIAN) {
            // Retain the student: only the relationship data is invalid.
            logger.warning("Discarding unresolved guardian link for student " + person.getName());
            return person.clearGuardian();
        }
        return person;
    }

}
