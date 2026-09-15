package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class RemarkTest {
    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void value_emptyAndUnrestrictedText_accepted() {
        assertEquals("", new Remark("").value);
        String text = "  Call @ 5:00 — 你好!  ";
        assertEquals(text, new Remark(text).toString());
    }

    @Test
    public void equalsAndHashCode() {
        Remark remark = new Remark("note");
        assertEquals(remark, new Remark("note"));
        assertEquals(remark.hashCode(), new Remark("note").hashCode());
        assertNotEquals(remark, new Remark("other"));
        assertNotEquals(remark, null);
        assertNotEquals(remark, "note");
    }

    @Test
    public void person_remarkAffectsEqualityButNotIdentity() {
        Person original = new PersonBuilder().build();
        Person changed = new PersonBuilder(original).withRemark("note").build();
        assertFalse(original.equals(changed));
        assertTrue(original.isSamePerson(changed));
        assertEquals(changed, new PersonBuilder(changed).build());
    }
}
