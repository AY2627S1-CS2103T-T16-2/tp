package seedu.tuitionbook.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.testutil.PersonBuilder;

public class MessagesTest {

    @Test
    public void format_absentEmailAndAddress_displaysHyphens() {
        Person person = new PersonBuilder().withoutEmail().withoutAddress().withTags().build();

        assertEquals("Amy Bee; Phone: 85355255; Email: -; Address: -; Tags: ", Messages.format(person));
    }
}
