package seedu.tuitionbook.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        String invalidTagName = "";
        assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
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

    @Test
    public void displayComparator_sortsCaseInsensitivelyWithDeterministicTieBreakAcrossLocales() {
        List<String[]> cases = List.of(
                new String[] {"zebra", "Alpha", "alpha", "10", "!tag"},
                new String[] {"Math", "math", "MATH"},
                new String[] {"beta", "Alpha", "2", "11"},
                new String[] {"-", "--", "-a", "-A"},
                new String[] {"i", "I", "item", "ITEM"},
                new String[] {",tag", ".tag", "_tag", "tag"},
                new String[] {"100", "20", "3", "Z"},
                new String[] {"school", "School", "SCHOOL", "science"},
                new String[] {"[tag]", "tag", "{tag}", "tag!"},
                new String[] {"z", "a", "A", "0", " " + "tag"});
        List<List<String>> expectedOrders = List.of(
                List.of("!tag", "10", "Alpha", "alpha", "zebra"),
                List.of("MATH", "Math", "math"),
                List.of("11", "2", "Alpha", "beta"),
                List.of("-", "--", "-A", "-a"),
                List.of("I", "i", "ITEM", "item"),
                List.of(",tag", ".tag", "_tag", "tag"),
                List.of("100", "20", "3", "Z"),
                List.of("SCHOOL", "School", "school", "science"),
                List.of("[tag]", "tag", "tag!", "{tag}"),
                List.of(" tag", "0", "A", "a", "z"));
        List<Locale> locales = List.of(Locale.ROOT, Locale.ENGLISH,
                Locale.forLanguageTag("tr"), Locale.forLanguageTag("az"));
        Locale originalLocale = Locale.getDefault();
        try {
            for (Locale locale : locales) {
                Locale.setDefault(locale);
                for (int index = 0; index < cases.size(); index++) {
                    assertEquals(expectedOrders.get(index), displayOrder(cases.get(index)),
                            "Unexpected order in locale " + locale + " for case " + index);
                }
            }
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    private static List<String> displayOrder(String... names) {
        List<Tag> tags = new ArrayList<>();
        for (String name : names) {
            tags.add(new Tag(name));
        }
        tags.sort(Tag.DISPLAY_COMPARATOR);
        List<String> result = new ArrayList<>();
        for (Tag tag : tags) {
            result.add(tag.tagName);
        }
        return result;
    }

}
