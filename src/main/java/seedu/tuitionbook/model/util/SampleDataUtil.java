package seedu.tuitionbook.model.util;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import seedu.tuitionbook.model.ReadOnlyTuitionBook;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.person.Address;
import seedu.tuitionbook.model.person.Email;
import seedu.tuitionbook.model.person.Name;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Phone;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.model.tag.Tag;

/**
 * Contains utility methods for populating {@code TuitionBook} with sample data.
 */
public class SampleDataUtil {
    /**
     * Fixed id for the sample guardian so that repeated sample-data generations produce
     * equal data; the guardian link participates in {@code Person#equals}.
     */
    private static final UUID SAMPLE_GUARDIAN_ID = UUID.fromString("7e57ab1e-0000-4000-8000-000000000001");

    public static Person[] getSamplePersons() {
        Person bernice = new Person(SAMPLE_GUARDIAN_ID, Role.GUARDIAN, null, new Name("Bernice Yu"),
                new Phone("99272758"), new Email("berniceyu@example.com"),
                new Address("Blk 30 Lorong 3 Serangoon Gardens, #07-18"),
                getTagSet("colleagues", "friends"));
        Person alex = new Person(Role.STUDENT, new Name("Alex Yeoh"), new Phone("87438807"),
                new Email("alexyeoh@example.com"),
                new Address("Blk 30 Geylang Street 29, #06-40"),
                getTagSet("friends"))
                .withGuardianId(bernice.getId());
        return new Person[] {
            alex,
            bernice,
            new Person(Role.STUDENT, new Name("Charlotte Oliveiro"), new Phone("93210283"),
                new Email("charlotte@example.com"),
                new Address("Blk 11 Ang Mo Kio Street 74, #11-04"),
                getTagSet("neighbours")),
            new Person(Role.GUARDIAN, new Name("David Li"), new Phone("91031282"),
                new Email("lidavid@example.com"),
                new Address("Blk 436 Serangoon Gardens Street 26, #16-43"),
                getTagSet("family")),
            new Person(Role.STUDENT, new Name("Irfan Ibrahim"), new Phone("92492021"),
                new Email("irfan@example.com"),
                new Address("Blk 47 Tampines Street 20, #17-35"),
                getTagSet("classmates")),
            new Person(Role.GUARDIAN, new Name("Roy Balakrishnan"), new Phone("92624417"),
                new Email("royb@example.com"),
                new Address("Blk 45 Aljunied Street 85, #11-31"),
                getTagSet("colleagues"))
        };
    }

    public static ReadOnlyTuitionBook getSampleTuitionBook() {
        TuitionBook sampleAb = new TuitionBook();
        for (Person samplePerson : getSamplePersons()) {
            sampleAb.addPerson(samplePerson);
        }
        return sampleAb;
    }

    /**
     * Returns a tag set containing the list of strings given.
     */
    public static Set<Tag> getTagSet(String... strings) {
        return Arrays.stream(strings)
                .map(Tag::new)
                .collect(Collectors.toSet());
    }

}
