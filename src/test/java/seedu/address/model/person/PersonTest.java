package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void normalizeNameForIdentity_surroundingAndRepeatedSpaces_collapsesSpaces() {
        assertEquals("AMY BEE", Person.normalizeNameForIdentity("  Amy   Bee  "));
    }

    @Test
    public void isSamePerson_caseEquivalentUnicodeNames_returnsTrue() {
        Person uppercaseSigma = new PersonBuilder().withName("ΟΣ").withPhone("91234567").build();
        Person lowercaseSigma = new PersonBuilder().withName("οσ").withPhone("91234567").build();
        assertTrue(uppercaseSigma.isSamePerson(lowercaseSigma));

        Person sharpS = new PersonBuilder().withName("Straße").withPhone("91234567").build();
        Person uppercaseDoubleS = new PersonBuilder().withName("STRASSE").withPhone("91234567").build();
        assertTrue(sharpS.isSamePerson(uppercaseDoubleS));
    }

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // exact matching identity fields, all non-identity attributes different -> returns true
        Person editedAlice = new PersonBuilder(ALICE).withId(UUID.randomUUID()).withRole(Role.GUARDIAN)
                .withEmail(VALID_EMAIL_BOB).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // guardian link differs, identity fields are the same -> returns true
        editedAlice = new PersonBuilder(ALICE).withGuardianId(UUID.randomUUID()).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // name differs only in case -> returns true
        editedAlice = new PersonBuilder(ALICE).withName("aLiCe PaUlInE").build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // name differs only in repeated and trailing spaces -> returns true
        editedAlice = new PersonBuilder(ALICE).withName("Alice  Pauline  ").build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // name differs only in Unicode composition -> returns true
        Person jose = new PersonBuilder().withName("Jos\u00e9 Tan").withPhone("+65 9123-4567").build();
        Person decomposedJose = new PersonBuilder(jose).withName("Jose\u0301 Tan").withPhone("+65 91234567").build();
        assertTrue(jose.isSamePerson(decomposedJose));

        // phone differs only in spaces or hyphens -> returns true
        editedAlice = new PersonBuilder(ALICE).withPhone("9435 1253").build();
        assertTrue(ALICE.isSamePerson(editedAlice));
        editedAlice = new PersonBuilder(ALICE).withPhone("9435-1253").build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // phone differs only in formatting while retaining a leading plus -> returns true
        editedAlice = new PersonBuilder(ALICE).withPhone("+9435 1253").build();
        Person unformattedPlusPhoneAlice = new PersonBuilder(ALICE).withPhone("+94351253").build();
        assertTrue(editedAlice.isSamePerson(unformattedPlusPhoneAlice));

        // phone differs by a leading plus -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone("+94351253").build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // same name but different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // same phone but different name -> returns false
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));

        // different role -> returns false
        editedAlice = new PersonBuilder(ALICE).withRole(Role.GUARDIAN).build();
        assertFalse(ALICE.equals(editedAlice));

        // different guardian link -> returns false
        editedAlice = new PersonBuilder(ALICE).withGuardianId(UUID.randomUUID()).build();
        assertFalse(ALICE.equals(editedAlice));

        // different id only -> returns true (id is excluded from equality)
        Person aliceWithOtherId = new PersonBuilder(ALICE).withId(UUID.randomUUID()).build();
        assertTrue(ALICE.equals(aliceWithOtherId));
    }

    @Test
    public void hashCode_equalPersons_haveSameHashCode() {
        Person aliceCopy = new PersonBuilder(ALICE).withId(UUID.randomUUID()).build();
        assertEquals(ALICE.hashCode(), aliceCopy.hashCode());
    }

    @Test
    public void getGuardianId_noGuardianLinked_returnsEmpty() {
        Person person = new PersonBuilder().build();
        assertTrue(person.getGuardianId().isEmpty());
    }

    @Test
    public void getOptionalContactFields_notProvided_returnsEmpty() {
        Person person = new PersonBuilder().withoutEmail().withoutAddress().build();

        assertTrue(person.getEmail().isEmpty());
        assertTrue(person.getAddress().isEmpty());
    }

    @Test
    public void equals_optionalContactFieldsAbsent_comparesSafely() {
        Person person = new PersonBuilder().withoutEmail().withoutAddress().build();
        Person copy = new PersonBuilder(person).build();

        assertEquals(person, copy);
        assertEquals(person.hashCode(), copy.hashCode());
    }

    @Test
    public void withGuardianId_linksGuardianAndKeepsOwnId() {
        Person guardian = new PersonBuilder().withName("Bernice Yu").withRole(Role.GUARDIAN).build();
        Person student = new PersonBuilder().build();

        Person linkedStudent = student.withGuardianId(guardian.getId());

        assertEquals(guardian.getId(), linkedStudent.getGuardianId().get());
        assertEquals(student.getId(), linkedStudent.getId());
        // original is unchanged (Person is immutable)
        assertTrue(student.getGuardianId().isEmpty());
    }

    @Test
    public void withGuardianId_null_throwsNullPointerException() {
        Person student = new PersonBuilder().build();
        assertThrows(NullPointerException.class, () -> student.withGuardianId(null));
    }

    @Test
    public void withGuardianId_onGuardian_throwsIllegalArgumentException() {
        Person guardian = new PersonBuilder().withRole(Role.GUARDIAN).build();
        Person otherGuardian = new PersonBuilder().withName("Bernice Yu").withRole(Role.GUARDIAN).build();
        assertThrows(IllegalArgumentException.class,
                Person.MESSAGE_ONLY_STUDENTS_CAN_HAVE_GUARDIAN, () -> guardian.withGuardianId(otherGuardian.getId()));
    }

    @Test
    public void withGuardianId_selfLink_throwsIllegalArgumentException() {
        Person student = new PersonBuilder().build();
        assertThrows(IllegalArgumentException.class,
                Person.MESSAGE_CANNOT_BE_OWN_GUARDIAN, () -> student.withGuardianId(student.getId()));
    }

    @Test
    public void clearGuardian_removesLinkAndKeepsOwnId() {
        Person guardian = new PersonBuilder().withName("Bernice Yu").withRole(Role.GUARDIAN).build();
        Person linkedStudent = new PersonBuilder().withGuardian(guardian).build();

        Person unlinkedStudent = linkedStudent.clearGuardian();

        assertTrue(unlinkedStudent.getGuardianId().isEmpty());
        assertEquals(linkedStudent.getId(), unlinkedStudent.getId());
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{role=" + ALICE.getRole() + ", name=" + ALICE.getName()
                + ", phone=" + ALICE.getPhone() + ", email=" + ALICE.getEmail().orElse(null) + ", address="
                + ALICE.getAddress().orElse(null) + ", tags=" + ALICE.getTags() + ", guardianId="
                + ALICE.getGuardianId().orElse(null) + "}";
        assertEquals(expected, ALICE.toString());
    }
}
