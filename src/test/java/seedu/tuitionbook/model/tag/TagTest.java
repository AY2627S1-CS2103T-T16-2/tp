package seedu.tuitionbook.model.tag;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        String invalidTagName = "";
        assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));

        // invalid tag names
        assertFalse(Tag.isValidTagName("")); // empty string
        assertFalse(Tag.isValidTagName(" ")); // whitespace only
        assertFalse(Tag.isValidTagName("-Math")); // leading hyphen
        assertFalse(Tag.isValidTagName("Math-")); // trailing hyphen
        assertFalse(Tag.isValidTagName("Math--Science")); // consecutive hyphens
        assertFalse(Tag.isValidTagName("-")); // hyphen without alphanumeric segments
        assertFalse(Tag.isValidTagName("Math Science")); // internal space
        assertFalse(Tag.isValidTagName("Math_Science")); // underscore
        assertFalse(Tag.isValidTagName("Math/Science")); // slash
        assertFalse(Tag.isValidTagName("Math.Science")); // other unsupported punctuation
        assertFalse(Tag.isValidTagName("数学")); // non-ASCII letters remain unsupported

        // valid tag names
        assertTrue(Tag.isValidTagName("Math"));
        assertTrue(Tag.isValidTagName("Sec4"));
        assertTrue(Tag.isValidTagName("referred-by-Mrs-Lim"));
        assertTrue(Tag.isValidTagName("Sec-4"));
        assertTrue(Tag.isValidTagName("A1-B2"));
        assertTrue(Tag.isValidTagName("A".repeat(1000))); // no tag-length limit
    }

}
