package seedu.tuitionbook.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void constructor_spacesAndPunctuation_preservesExactValue() {
        for (String value : List.of("Sec 4", "Math--Science", "-Math-", "Math/Science",
                "Mrs. Lim's class", "C++", "!@#$%^&*()_+={}[]:;\"'<>,.?/\\|~`", "Math  Science")) {
            assertEquals(value, new Tag(value).tagName);
        }
    }

    @Test
    public void constructor_surroundingSpaces_trimsValue() {
        assertEquals("Math", new Tag("  Math  ").tagName);
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        for (String invalidTagName : List.of("", "   ", " Math\t ")) {
            assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
        }
    }

    @Test
    public void constructor_longMultiSegmentTag_preservesExactValue() {
        String longMultiSegmentTag = "A-".repeat(5000) + "B";

        assertTrue(Tag.isValidTagName(longMultiSegmentTag));
        assertEquals(longMultiSegmentTag, new Tag(longMultiSegmentTag).tagName);
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));

        // invalid tag names
        assertFalse(Tag.isValidTagName("")); // empty string
        assertFalse(Tag.isValidTagName(" ")); // whitespace only
        assertFalse(Tag.isValidTagName("Math\tScience")); // internal tab
        assertFalse(Tag.isValidTagName("Math\nScience")); // newline
        assertFalse(Tag.isValidTagName("Math\rScience")); // carriage return
        assertFalse(Tag.isValidTagName("é")); // non-ASCII Latin letter
        assertFalse(Tag.isValidTagName("数学")); // non-ASCII letters
        assertFalse(Tag.isValidTagName("١")); // non-ASCII digit

        // valid tag names
        assertTrue(Tag.isValidTagName("Math"));
        assertTrue(Tag.isValidTagName("Sec4"));
        assertTrue(Tag.isValidTagName("referred-by-Mrs-Lim"));
        assertTrue(Tag.isValidTagName("Sec-4"));
        assertTrue(Tag.isValidTagName("A1-B2"));
        assertTrue(Tag.isValidTagName(" Math "));
        assertTrue(Tag.isValidTagName("A".repeat(1000))); // no tag-length limit
    }

    @Test
    public void isValidTagName_longInvalidTags_returnsFalse() {
        String manySegments = "A-".repeat(5000);

        assertFalse(Tag.isValidTagName(manySegments + "\tB")); // control character near the end
        assertFalse(Tag.isValidTagName(" ".repeat(10000))); // long blank tag
    }

    @Test
    public void isValidTagName_asciiBoundaries_acceptsOnlyPrintableCharacters() {
        for (char character = 0; character < 128; character++) {
            assertEquals(character >= ' ' && character <= '~', Tag.isValidTagName("A" + character),
                    "ASCII character " + (int) character);
        }
    }

}
