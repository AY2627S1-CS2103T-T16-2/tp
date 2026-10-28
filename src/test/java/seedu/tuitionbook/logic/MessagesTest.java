package seedu.tuitionbook.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.model.person.Role;
import seedu.tuitionbook.model.tag.Tag;
import seedu.tuitionbook.testutil.PersonBuilder;

public class MessagesTest {

    @Test
    public void getInvalidPersonDisplayedIndexMessage_emptyList_returnsEmptyListMessage() {
        assertEquals("The contact index provided is invalid. No contacts are shown in the current list. "
                        + "Use list to show all contacts.",
                Messages.getInvalidPersonDisplayedIndexMessage(0));
    }

    @Test
    public void getInvalidPersonDisplayedIndexMessage_oneContact_usesSingularWording() {
        assertEquals("The contact index provided is invalid. There is 1 contact in the current list. "
                        + "Use an index from 1 to 1.",
                Messages.getInvalidPersonDisplayedIndexMessage(1));
    }

    @Test
    public void getInvalidPersonDisplayedIndexMessage_multipleContacts_usesPluralWording() {
        assertEquals("The contact index provided is invalid. There are 2 contacts in the current list. "
                        + "Use an index from 1 to 2.",
                Messages.getInvalidPersonDisplayedIndexMessage(2));
    }

    @Test
    public void format_absentEmailAndAddress_displaysHyphens() {
        Person person = new PersonBuilder().withoutEmail().withoutAddress().withTags().build();

        assertEquals("Amy Bee; Role: STUDENT; Phone: 85355255; Email: -; Address: -; Tags: ", Messages.format(person));
    }

    @Test
    public void format_plainTag_displaysTagUnchanged() {
        Person person = new PersonBuilder().withoutEmail().withoutAddress().withTags("Sec 4").build();

        assertEquals("Amy Bee; Role: STUDENT; Phone: 85355255; Email: -; Address: -; Tags: [Sec 4]",
                Messages.format(person));
    }

    @Test
    public void format_tagContainingBrackets_distinguishableFromSeparateTags() {
        Person oneTag = new PersonBuilder().withTags("first][second").build();
        Person twoTags = new PersonBuilder().withTags("first", "second").build();

        assertTrue(Messages.format(oneTag).endsWith("Tags: [first\\]\\[second]"));
        assertNotEquals(Messages.format(oneTag), Messages.format(twoTags));
    }

    @Test
    public void formatTag_backslashesAndBrackets_escaped() {
        assertEquals("[plain]", Messages.formatTag(new Tag("plain")));
        assertEquals("[C:\\\\Math\\\\]", Messages.formatTag(new Tag("C:\\Math\\")));
        assertEquals("[\\[Math\\]]", Messages.formatTag(new Tag("[Math]")));
    }

    @Test
    public void format_guardian_displaysGuardianRole() {
        Person person = new PersonBuilder().withRole(Role.GUARDIAN).withoutEmail().withoutAddress()
                .withTags().build();

        assertEquals("Amy Bee; Role: GUARDIAN; Phone: 85355255; Email: -; Address: -; Tags: ",
                Messages.format(person));
    }
}
