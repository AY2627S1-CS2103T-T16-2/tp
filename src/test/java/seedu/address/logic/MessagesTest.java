package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class MessagesTest {

    @Test
    public void format_absentEmailAndAddress_displaysHyphens() {
        Person person = new PersonBuilder().withoutEmail().withoutAddress().withTags().build();

        assertEquals("Amy Bee; Phone: 85355255; Email: -; Address: -; Tags: ", Messages.format(person));
    }
}
