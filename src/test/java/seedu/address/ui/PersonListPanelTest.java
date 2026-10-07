package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.model.person.Role;
import seedu.address.testutil.PersonBuilder;

public class PersonListPanelTest {

    @Test
    public void resolveGuardianName_linkedStudent_returnsGuardianName() {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Sam Student").withGuardian(guardian).build();

        String result = PersonListPanel.resolveGuardianName(student, Map.of(guardian.getId(), guardian));

        assertEquals(guardian.getName().fullName, result);
    }

    @Test
    public void resolveGuardianName_unlinkedStudent_returnsPlaceholder() {
        Person student = new PersonBuilder().withName("Sam Student").build();

        assertEquals("-", PersonListPanel.resolveGuardianName(student, Map.of(student.getId(), student)));
    }

    @Test
    public void resolveGuardianName_guardianContact_returnsPlaceholder() {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withRole(Role.GUARDIAN).build();

        assertEquals("-", PersonListPanel.resolveGuardianName(guardian, Map.of(guardian.getId(), guardian)));
    }

    @Test
    public void resolveGuardianName_missingGuardian_returnsPlaceholder() {
        Person student = new PersonBuilder().withName("Sam Student").withGuardianId(UUID.randomUUID()).build();

        assertEquals("-", PersonListPanel.resolveGuardianName(student, Map.of(student.getId(), student)));
    }

    @Test
    public void resolveGuardianName_guardianIdRefersToStudent_returnsPlaceholder() {
        Person referencedStudent = new PersonBuilder().withName("Other Student").build();
        Person student = new PersonBuilder().withName("Sam Student").withGuardianId(referencedStudent.getId()).build();

        assertEquals("-", PersonListPanel.resolveGuardianName(student,
                Map.of(referencedStudent.getId(), referencedStudent)));
    }

    @Test
    public void resolveGuardianName_guardianExcludedFromDisplayedPersons_returnsGuardianName() {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Sam Student").withGuardian(guardian).build();
        List<Person> displayedPersons = List.of(student);

        assertFalse(displayedPersons.contains(guardian));
        assertEquals(guardian.getName().fullName,
                PersonListPanel.resolveGuardianName(student, Map.of(guardian.getId(), guardian)));
    }
}
