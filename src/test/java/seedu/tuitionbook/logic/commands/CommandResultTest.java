package seedu.tuitionbook.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class CommandResultTest {
    @Test
    public void equals() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns true
        assertTrue(commandResult.equals(new CommandResult("feedback")));
        assertTrue(commandResult.equals(new CommandResult("feedback", false, false)));

        // same object -> returns true
        assertTrue(commandResult.equals(commandResult));

        // null -> returns false
        assertFalse(commandResult.equals(null));

        // different types -> returns false
        assertFalse(commandResult.equals(0.5f));

        // different feedbackToUser value -> returns false
        assertFalse(commandResult.equals(new CommandResult("different")));

        // different showHelp value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", true, false)));

        // different exit value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", false, true)));

        // different detailsToShow value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", "details")));

        // different personIdToScrollTo value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", UUID.randomUUID())));
    }

    @Test
    public void hashcode() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns same hashcode
        assertEquals(commandResult.hashCode(), new CommandResult("feedback").hashCode());

        // different feedbackToUser value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("different").hashCode());

        // different showHelp value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", true, false).hashCode());

        // different exit value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", false, true).hashCode());

        // different detailsToShow value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", "details").hashCode());

        // different personIdToScrollTo value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", UUID.randomUUID()).hashCode());
    }

    @Test
    public void getDetailsToShow() {
        assertTrue(new CommandResult("feedback", "details").getDetailsToShow().isPresent());
        assertEquals("details", new CommandResult("feedback", "details").getDetailsToShow().orElseThrow());
        assertTrue(new CommandResult("feedback").getDetailsToShow().isEmpty());
    }

    @Test
    public void getPersonIdToScrollTo() {
        UUID personId = UUID.randomUUID();

        assertEquals(personId, new CommandResult("feedback", personId).getPersonIdToScrollTo().orElseThrow());
        assertTrue(new CommandResult("feedback").getPersonIdToScrollTo().isEmpty());
    }

    @Test
    public void toStringMethod() {
        CommandResult commandResult = new CommandResult("feedback");
        String expected = CommandResult.class.getCanonicalName() + "{feedbackToUser="
                + commandResult.getFeedbackToUser() + ", showHelp=" + commandResult.isShowHelp()
                + ", exit=" + commandResult.isExit()
                + ", detailsToShow=null, personIdToScrollTo=null}";
        assertEquals(expected, commandResult.toString());
    }
}
