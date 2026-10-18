package seedu.tuitionbook.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.testutil.PersonBuilder;

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

    @Test
    public void resolveLinkedStudentNames_guardianWithoutLinkedStudents_returnsPlaceholder() {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withRole(Role.GUARDIAN).build();
        Person unlinkedStudent = new PersonBuilder().withName("Una Student").build();

        assertEquals("-", PersonListPanel.resolveLinkedStudentNames(guardian,
                List.of(guardian, unlinkedStudent)));
    }

    @Test
    public void resolveLinkedStudentNames_guardianWithOneLinkedStudent_returnsStudentName() {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withRole(Role.GUARDIAN).build();
        Person linkedStudent = new PersonBuilder().withName("Sam Student").withGuardian(guardian).build();

        assertEquals("Sam Student", PersonListPanel.resolveLinkedStudentNames(guardian,
                List.of(guardian, linkedStudent)));
    }

    @Test
    public void resolveLinkedStudentNames_multipleContacts_returnsLinkedStudentsInContactListOrder() {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withRole(Role.GUARDIAN).build();
        Person otherGuardian = new PersonBuilder().withName("Gina Guardian").withRole(Role.GUARDIAN).build();
        Person secondLinkedStudent = new PersonBuilder().withName("Zara Student").withGuardian(guardian).build();
        Person unrelatedStudent = new PersonBuilder().withName("Una Student").withGuardian(otherGuardian).build();
        Person firstLinkedStudent = new PersonBuilder().withName("Alex Student").withGuardian(guardian).build();

        assertEquals("Zara Student, Alex Student", PersonListPanel.resolveLinkedStudentNames(guardian,
                List.of(secondLinkedStudent, unrelatedStudent, firstLinkedStudent, guardian, otherGuardian)));
    }

    @Test
    public void resolveLinkedStudentNames_studentContact_returnsPlaceholder() {
        Person student = new PersonBuilder().withName("Sam Student").build();

        assertEquals("-", PersonListPanel.resolveLinkedStudentNames(student, List.of(student)));
    }
}
