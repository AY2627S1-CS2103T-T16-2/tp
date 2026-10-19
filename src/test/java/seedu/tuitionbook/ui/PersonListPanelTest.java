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
    public void resolveGuardianSummary_linkedStudent_returnsGuardianNameAndPhone() {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("91234567")
                .withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Sam Student").withGuardian(guardian).build();

        String result = PersonListPanel.resolveGuardianSummary(student, Map.of(guardian.getId(), guardian));

        assertEquals("Grace Guardian (91234567)", result);
    }

    @Test
    public void resolveGuardianSummary_unlinkedStudent_returnsPlaceholder() {
        Person student = new PersonBuilder().withName("Sam Student").build();

        assertEquals("-", PersonListPanel.resolveGuardianSummary(student, Map.of(student.getId(), student)));
    }

    @Test
    public void resolveGuardianSummary_guardianContact_returnsPlaceholder() {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withRole(Role.GUARDIAN).build();

        assertEquals("-", PersonListPanel.resolveGuardianSummary(guardian, Map.of(guardian.getId(), guardian)));
    }

    @Test
    public void resolveGuardianSummary_missingGuardian_returnsPlaceholder() {
        Person student = new PersonBuilder().withName("Sam Student").withGuardianId(UUID.randomUUID()).build();

        assertEquals("-", PersonListPanel.resolveGuardianSummary(student, Map.of(student.getId(), student)));
    }

    @Test
    public void resolveGuardianSummary_guardianIdRefersToStudent_returnsPlaceholder() {
        Person referencedStudent = new PersonBuilder().withName("Other Student").build();
        Person student = new PersonBuilder().withName("Sam Student").withGuardianId(referencedStudent.getId()).build();

        assertEquals("-", PersonListPanel.resolveGuardianSummary(student,
                Map.of(referencedStudent.getId(), referencedStudent)));
    }

    @Test
    public void resolveGuardianSummary_guardianExcludedFromDisplayedPersons_returnsNameAndPhone() {
        Person guardian = new PersonBuilder().withName("Grace Guardian").withPhone("91234567")
                .withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().withName("Sam Student").withGuardian(guardian).build();
        List<Person> displayedPersons = List.of(student);

        assertFalse(displayedPersons.contains(guardian));
        assertEquals("Grace Guardian (91234567)",
                PersonListPanel.resolveGuardianSummary(student, Map.of(guardian.getId(), guardian)));
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
