package seedu.tuitionbook.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;

public class SampleDataUtilTest {

    @Test
    public void getSamplePersons_containsBothRoles() {
        Person[] samplePersons = SampleDataUtil.getSamplePersons();
        assertTrue(Arrays.stream(samplePersons).anyMatch(person -> person.getRole() == Role.STUDENT));
        assertTrue(Arrays.stream(samplePersons).anyMatch(person -> person.getRole() == Role.GUARDIAN));
    }

    @Test
    public void getSamplePersons_hasOneLinkedPairResolvingToSampleGuardian() {
        Person[] samplePersons = SampleDataUtil.getSamplePersons();
        List<Person> linkedPersons = Arrays.stream(samplePersons)
                .filter(person -> person.getGuardianId().isPresent())
                .toList();
        assertEquals(1, linkedPersons.size());

        UUID linkedGuardianId = linkedPersons.get(0).getGuardianId().get();
        Person linkedGuardian = Arrays.stream(samplePersons)
                .filter(person -> person.getId().equals(linkedGuardianId))
                .findFirst()
                .orElseThrow();
        assertEquals(Role.GUARDIAN, linkedGuardian.getRole());
    }

    @Test
    public void getSampleTuitionBook_calledTwice_producesEqualData() {
        assertEquals(SampleDataUtil.getSampleTuitionBook().getPersonList(),
                SampleDataUtil.getSampleTuitionBook().getPersonList());
    }

    @Test
    public void getSampleTuitionBook_containsAllSamplePersons() {
        assertEquals(SampleDataUtil.getSamplePersons().length,
                SampleDataUtil.getSampleTuitionBook().getPersonList().size());
    }
}
