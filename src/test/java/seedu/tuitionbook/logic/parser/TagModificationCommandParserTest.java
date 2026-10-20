package seedu.tuitionbook.logic.parser;

import static seedu.tuitionbook.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.tuitionbook.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.tuitionbook.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.tuitionbook.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.logic.commands.TagCommand;
import seedu.tuitionbook.logic.commands.UntagCommand;
import seedu.tuitionbook.model.tag.Tag;

public class TagModificationCommandParserTest {
    private final TagCommandParser tagParser = new TagCommandParser();
    private final UntagCommandParser untagParser = new UntagCommandParser();

    @Test
    public void parse_quotedTags_preservesEmbeddedPrefixesAndTrimsEdges() {
        String input = " 1 t/\"  study t/weekly r/student  \" t/\"Sec  4\" t/Math t/\"Math\"";
        Set<Tag> tags = Set.of(new Tag("study t/weekly r/student"), new Tag("Sec  4"), new Tag("Math"));
        assertParseSuccess(tagParser, input, new TagCommand(INDEX_FIRST_PERSON, tags));
        assertParseSuccess(untagParser, input, new UntagCommand(INDEX_FIRST_PERSON, tags));
    }

    @Test
    public void parse_escapedTags_decodesQuotedValuesOnly() {
        String input = " 1 t/\" \\\"Sec 4\\\" \" t/\"C:\\\\Math\\\\\" t/\"C:\\Math\" t/C:\\\\Math";
        Set<Tag> tags = Set.of(new Tag("\"Sec 4\""), new Tag("C:\\Math\\"),
                new Tag("C:\\Math"), new Tag("C:\\\\Math"));
        assertParseSuccess(tagParser, input, new TagCommand(INDEX_FIRST_PERSON, tags));
        assertParseSuccess(untagParser, input, new UntagCommand(INDEX_FIRST_PERSON, tags));
    }

    @Test
    public void parse_malformedQuotedTag_rejectsWholeCommand() {
        for (String input : List.of("1 t/Math t/\"", "1 t/\"Math\\\"", "1 t/\"Math\\")) {
            assertParseFailure(tagParser, input, ArgumentTokenizer.MESSAGE_UNCLOSED_QUOTE);
            assertParseFailure(untagParser, input, ArgumentTokenizer.MESSAGE_UNCLOSED_QUOTE);
        }
        for (String input : List.of("1 t/\"Math\" extra", "1 t/\"Math\"t/Science",
                "1 t/\"Math\" p/90000001")) {
            assertParseFailure(tagParser, input, ArgumentTokenizer.MESSAGE_TEXT_AFTER_QUOTE);
            assertParseFailure(untagParser, input, ArgumentTokenizer.MESSAGE_TEXT_AFTER_QUOTE);
        }
    }

    @Test
    public void parse_onlyTagPrefixRecognised_preservesOtherUnquotedPrefixes() {
        String input = " 1 t/meet at a/c room p/next week n/alias r/student t/weekly";
        Set<Tag> tags = Set.of(new Tag("meet at a/c room p/next week n/alias r/student"), new Tag("weekly"));
        assertParseSuccess(tagParser, input, new TagCommand(INDEX_FIRST_PERSON, tags));
        assertParseSuccess(untagParser, input, new UntagCommand(INDEX_FIRST_PERSON, tags));
    }

    @Test
    public void parse_repeatedTagsAndSurroundingWhitespace_success() {
        Set<Tag> tags = Set.of(new Tag("Sec  4"), new Tag("Math/Science"));
        String input = " 1 t/  Sec  4  t/Math/Science t/Sec  4 ";
        assertParseSuccess(tagParser, input, new TagCommand(INDEX_FIRST_PERSON, tags));
        assertParseSuccess(untagParser, input, new UntagCommand(INDEX_FIRST_PERSON, tags));
    }

    @Test
    public void parse_missingTagsOrInvalidIndex_reportsUsage() {
        for (String input : List.of("", "1", "1 Math", "0 t/Math", "-1 t/Math", "one t/Math",
                "1 2 t/Math", "2147483648 t/Math", "t/Math 1")) {
            assertParseFailure(tagParser, input,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagCommand.MESSAGE_USAGE));
            assertParseFailure(untagParser, input,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, UntagCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_anyInvalidTag_rejectsWholeCommand() {
        for (String input : List.of("1 t/", "1 t/   ", "1 t/Math t/", "1 t/ t/Science",
                "1 t/Math t/bad\ttag", "1 t/caf\u00e9", "1 t/bad\u007ftag", "1 t/\"\"",
                "1 t/Math t/\"   \"", "1 t/Math t/\"bad\ttag\"")) {
            assertParseFailure(tagParser, input, Tag.MESSAGE_CONSTRAINTS);
            assertParseFailure(untagParser, input, Tag.MESSAGE_CONSTRAINTS);
        }
    }
}
