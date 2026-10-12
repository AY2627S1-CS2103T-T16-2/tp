package seedu.tuitionbook.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.testutil.PersonBuilder;

public class MessagesTest {

    @Test
    public void getInvalidPersonDisplayedIndexMessage_emptyList_returnsEmptyListMessage() {
        assertEquals("The person index provided is invalid. No contacts are shown in the current list.",
                Messages.getInvalidPersonDisplayedIndexMessage(0));
    }

    @Test
    public void getInvalidPersonDisplayedIndexMessage_oneContact_usesSingularWording() {
        assertEquals("The person index provided is invalid. There is 1 contact in the current list. "
                        + "Use an index from 1 to 1.",
                Messages.getInvalidPersonDisplayedIndexMessage(1));
    }

    @Test
    public void getInvalidPersonDisplayedIndexMessage_multipleContacts_usesPluralWording() {
        assertEquals("The person index provided is invalid. There are 2 contacts in the current list. "
                        + "Use an index from 1 to 2.",
                Messages.getInvalidPersonDisplayedIndexMessage(2));
    }

    @Test
    public void format_absentEmailAndAddress_displaysHyphens() {
        Person person = new PersonBuilder().withoutEmail().withoutAddress().withTags().build();

        assertEquals("Amy Bee; Role: STUDENT; Phone: 85355255; Email: -; Address: -; Tags: ", Messages.format(person));
    }

    @Test
    public void format_guardian_displaysGuardianRole() {
        Person person = new PersonBuilder().withRole(Role.GUARDIAN).withoutEmail().withoutAddress()
                .withTags().build();

        assertEquals("Amy Bee; Role: GUARDIAN; Phone: 85355255; Email: -; Address: -; Tags: ",
                Messages.format(person));
    }
}
