package seedu.tuitionbook.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public static final String MESSAGE_CONTACT_PREFIX = "Contact %d in the file: %s";
    public static final String MESSAGE_DUPLICATE_PERSON =
            "Contact %d in the file has the same name and phone number as contact %d.";
    public static final String MESSAGE_DUPLICATE_ID = "Contact %d in the file has the same \"id\" as contact %d. "
            + "Each contact needs a different id.";
    public static final String MESSAGE_NULL_PERSON = "Contact %d in the file is null instead of a contact. "
            + "Remove it from the \"persons\" list.";

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
        return new TuitionBook(toModelTypeWithResult().tuitionBook());
    }

    /**
     * Returns the loaded contacts and diagnostics for guardian links repaired during conversion.
     */
    TuitionBookLoadResult toModelTypeWithResult() throws IllegalValueException {
        List<Person> loadedPersons = new ArrayList<>();
        List<GuardianLinkRepair> parseRepairs = new ArrayList<>();
        Map<UUID, Integer> positionsById = new HashMap<>();
        for (int index = 0; index < persons.size(); index++) {
            int position = index + 1;
            JsonAdaptedPerson.PersonLoadResult personResult = convertContact(persons.get(index), position);
            Person person = personResult.person();
            parseRepairs.add(personResult.repair());
            // IDs must be unique before they can safely be used to resolve guardian links.
            Integer earlierPosition = positionsById.putIfAbsent(person.getId(), position);
            if (earlierPosition != null) {
                throw new IllegalValueException(String.format(MESSAGE_DUPLICATE_ID, position, earlierPosition));
            }
            loadedPersons.add(person);
        }

        // Build the complete lookup first because a student can precede its guardian in the file.
        Map<UUID, Person> personsById = new HashMap<>();
        for (Person person : loadedPersons) {
            personsById.put(person.getId(), person);
        }

        TuitionBook tuitionBook = new TuitionBook();
        List<GuardianLinkRepair> repairs = new ArrayList<>();
        for (int index = 0; index < loadedPersons.size(); index++) {
            Person person = loadedPersons.get(index);
            GuardianLinkRepair repair = parseRepairs.get(index);
            Person resolvedPerson = resolveGuardianLink(person, personsById);
            if (resolvedPerson != person) {
                GuardianLinkRepair.Reason reason = personsById.containsKey(person.getGuardianId().orElseThrow())
                        ? GuardianLinkRepair.Reason.STUDENT_GUARDIAN
                        : GuardianLinkRepair.Reason.MISSING_GUARDIAN;
                repair = new GuardianLinkRepair(person.getName().fullName, person.getId(),
                        person.getGuardianId().map(UUID::toString).orElse(null), reason);
            }
            if (repair != null) {
                repairs.add(repair);
            }
            // TuitionBook rejects manually introduced duplicate contact identities.
            if (tuitionBook.hasPerson(resolvedPerson)) {
                throw new IllegalValueException(String.format(MESSAGE_DUPLICATE_PERSON, index + 1,
                        findEarlierSamePersonPosition(loadedPersons, index)));
            }
            tuitionBook.addPerson(resolvedPerson);
        }
        return new TuitionBookLoadResult(tuitionBook, repairs);
    }

    /**
     * Converts the contact at the given 1-based {@code position}, naming that position in any error message.
     */
    private static JsonAdaptedPerson.PersonLoadResult convertContact(JsonAdaptedPerson jsonAdaptedPerson,
            int position) throws IllegalValueException {
        if (jsonAdaptedPerson == null) {
            throw new IllegalValueException(String.format(MESSAGE_NULL_PERSON, position));
        }
        try {
            return jsonAdaptedPerson.toModelTypeWithResult();
        } catch (IllegalValueException ive) {
            throw new IllegalValueException(String.format(MESSAGE_CONTACT_PREFIX, position, ive.getMessage()), ive);
        }
    }

    /**
     * Returns the 1-based position of the first contact before {@code index} with the same identity.
     */
    private static int findEarlierSamePersonPosition(List<Person> loadedPersons, int index) {
        Person duplicate = loadedPersons.get(index);
        return IntStream.range(0, index)
                .filter(earlierIndex -> loadedPersons.get(earlierIndex).isSamePerson(duplicate))
                .findFirst().orElseThrow() + 1;
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
