package seedu.tuitionbook.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.tuitionbook.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.tuitionbook.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.logic.parser.exceptions.ParseException;

public class QuotedValueTokenizerTest {
    @Test
    public void tokenize_unknownPrefixOutsideQuotes_rejectsRatherThanAbsorbsData() {
        for (String prefix : List.of("ee/", "N/", "unknown/", "x/")) {
            String expected = "Unknown field " + prefix + ". Check the command format, "
                    + "or put the whole value in double quotes if it contains " + prefix + ".";
            assertThrows(ParseException.class, expected, () -> tokenize(" a/12 Road " + prefix + "value"));
        }
    }

    @Test
    public void tokenize_unknownPrefixesInsideQuotedValues_remainLiteral() throws Exception {
        ArgumentMultimap result = tokenize(" n/\"Rina x/o Tan\" t/\"Advanced Math/Science ee/literal\"");
        assertEquals("Rina x/o Tan", result.getValue(PREFIX_NAME).orElseThrow());
        assertEquals(List.of("Advanced Math/Science ee/literal"), result.getAllValues(PREFIX_TAG));
        assertEquals(List.of("Math/Science"), tokenize(" t/Math/Science").getAllValues(PREFIX_TAG));
    }

    @Test
    public void tokenize_nonPrefixSlashesAndPunctuation_remainLiteral() throws Exception {
        for (String value : List.of("Math/Science", "See https://example.com", "Use 1/2", "C++/Java",
                "a-path/file", "Unit\u3000ee/literal")) {
            assertEquals(value, tokenize(" a/" + value).getValue(PREFIX_ADDRESS).orElseThrow());
        }
        assertThrows(ParseException.class, String.format(ArgumentTokenizer.MESSAGE_UNKNOWN_PREFIX, "ee/"), () ->
                tokenize("ee/unknown"));
    }

    @Test
    public void tokenize_quotedAddress_protectsKnownAndUnknownPrefixesAndDecodesEscapes() throws Exception {
        ArgumentMultimap result = ArgumentTokenizer.tokenizeWithQuotedValues(
                " a/\"20 Main Road, c/o Rina a/p Kaur \\\"Unit A\\\" C:\\\\Road\\\\\" p/123",
                List.of(PREFIX_ADDRESS), PREFIX_PHONE);
        assertEquals("20 Main Road, c/o Rina a/p Kaur \"Unit A\" C:\\Road\\",
                result.getValue(PREFIX_ADDRESS).orElseThrow());
        assertEquals("123", result.getValue(PREFIX_PHONE).orElseThrow());
    }

    @Test
    public void tokenize_quotedName_protectsPrefixesAndResumesAfterQuote() throws Exception {
        for (String input : List.of(
                " n/\"Rina a/p Kaur n/alias p/test\" p/91234567",
                " p/91234567 n/\"Rina a/p Kaur n/alias p/test\"",
                " p/91234567 n/  \" Rina a/p Kaur n/alias p/test \"  t/Math t/Science")) {
            ArgumentMultimap result = tokenize(input);
            assertEquals("Rina a/p Kaur n/alias p/test", result.getValue(PREFIX_NAME).orElseThrow());
            assertEquals("91234567", result.getValue(PREFIX_PHONE).orElseThrow());
            assertFalse(result.getValue(PREFIX_ADDRESS).isPresent());
        }
        ArgumentMultimap result = tokenize(" 2 n/\"Rina s/o Kaur\" a/123 Road t/Math t/Science");
        assertEquals("2", result.getPreamble());
        assertEquals("Rina s/o Kaur", result.getValue(PREFIX_NAME).orElseThrow());
        assertEquals("123 Road", result.getValue(PREFIX_ADDRESS).orElseThrow());
        assertEquals(List.of("Math", "Science"), result.getAllValues(PREFIX_TAG));
    }

    @Test
    public void tokenize_quotedTags_protectsPrefixesAndSupportsMultipleValues() throws Exception {
        ArgumentMultimap result = tokenize(
                " n/Person t/\"meet at a/c room\" t/\"call p/92345678\" p/91234567");

        assertEquals("Person", result.getValue(PREFIX_NAME).orElseThrow());
        assertEquals("91234567", result.getValue(PREFIX_PHONE).orElseThrow());
        assertFalse(result.getValue(PREFIX_ADDRESS).isPresent());
        assertEquals(List.of("meet at a/c room", "call p/92345678"), result.getAllValues(PREFIX_TAG));
    }

    @Test
    public void tokenize_unquotedNameAndOtherQuotes_preservesExistingInterpretation() throws Exception {
        ArgumentMultimap result = tokenize(" n/Rina a/p Kaur");
        assertEquals("Rina", result.getValue(PREFIX_NAME).orElseThrow());
        assertEquals("p Kaur", result.getValue(PREFIX_ADDRESS).orElseThrow());
        result = tokenize(" n/O'Brien a/\"Block A\"");
        assertEquals("O'Brien", result.getValue(PREFIX_NAME).orElseThrow());
        assertEquals("\"Block A\"", result.getValue(PREFIX_ADDRESS).orElseThrow());
        assertEquals("preamble", tokenize("preamble").getPreamble());
    }

    @Test
    public void tokenize_emptyAndRepeatedNames_preservesValuesForParserValidation() throws Exception {
        ArgumentMultimap result = tokenize(" n/\"\" n/Other");
        assertEquals(List.of("", "Other"), result.getAllValues(PREFIX_NAME));
        assertThrows(ParseException.class, () -> result.verifyNoDuplicatePrefixesFor(PREFIX_NAME));
    }

    @Test
    public void tokenize_emptyUnquotedName_preservesEmptyValueForParserValidation() throws Exception {
        for (String input : List.of(" n/", " n/   ", " n/\t")) {
            assertEquals(List.of(""), tokenize(input).getAllValues(PREFIX_NAME));
        }
    }

    @Test
    public void tokenize_unclosedQuote_throwsHelpfulError() {
        for (String input : List.of(" n/\"Rina a/p Kaur p/91234567", " t/\"meet at a/c room")) {
            assertThrows(ParseException.class, ArgumentTokenizer.MESSAGE_UNCLOSED_QUOTE, () -> tokenize(input));
        }
    }

    @Test
    public void tokenize_singleOpeningQuoteWithSurroundingSpaces_throwsHelpfulError() {
        for (Prefix prefix : List.of(PREFIX_NAME, PREFIX_TAG)) {
            for (String leading : List.of("", "   ")) {
                for (String trailing : List.of("", "   ")) {
                    for (String value : List.of("\"", "\"Math")) {
                        String input = " " + prefix.getPrefix() + leading + value + trailing;
                        assertThrows(ParseException.class, ArgumentTokenizer.MESSAGE_UNCLOSED_QUOTE, () ->
                                tokenize(input));
                    }
                }
            }
        }
    }

    @Test
    public void tokenize_quotedValueWithSurroundingSpaces_ignoresSurroundingSpaces() throws Exception {
        for (Prefix prefix : List.of(PREFIX_NAME, PREFIX_TAG)) {
            ArgumentMultimap result = tokenize(" " + prefix.getPrefix() + "   \"  Math  Class  \"   ");
            assertEquals(List.of("Math  Class"), result.getAllValues(prefix));
        }
    }

    @Test
    public void tokenize_escapedQuotes_protectsEmbeddedPrefixesAndResumesAfterClosingQuote() throws Exception {
        ArgumentMultimap result = tokenize(
                " t/  \" \\\"meet at a/c room\\\" t/weekly \"  p/91234567 t/\"\\\"Sec 4\\\"\"");
        assertEquals(List.of("\"meet at a/c room\" t/weekly", "\"Sec 4\""),
                result.getAllValues(PREFIX_TAG));
        assertEquals("91234567", result.getValue(PREFIX_PHONE).orElseThrow());
        assertFalse(result.getValue(PREFIX_ADDRESS).isPresent());
    }

    @Test
    public void tokenize_backslashes_decodesOnlySupportedEscapesInsideQuotedValues() throws Exception {
        ArgumentMultimap result = tokenize(
                " t/\"C:\\\\Math\\\\\" t/\"C:\\Math\\Science\" t/C:\\\\Math\\\"notes");
        assertEquals(List.of("C:\\Math\\", "C:\\Math\\Science", "C:\\\\Math\\\"notes"),
                result.getAllValues(PREFIX_TAG));
    }

    @Test
    public void tokenize_escapedNameQuotes_decodesBeforeDomainValidation() throws Exception {
        assertEquals("Rina \"alias\"", tokenize(" n/\"Rina \\\"alias\\\"\"")
                .getValue(PREFIX_NAME).orElseThrow());
    }

    @Test
    public void tokenize_escapedClosingQuoteWithoutRealClosingQuote_throwsHelpfulError() {
        for (String input : List.of(" t/\"Math\\\"", " t/\"Math\\", " n/\"Rina\\\"")) {
            assertThrows(ParseException.class, ArgumentTokenizer.MESSAGE_UNCLOSED_QUOTE, () -> tokenize(input));
        }
    }

    @Test
    public void tokenize_textAfterEscapedQuotedValue_throwsHelpfulError() {
        assertThrows(ParseException.class, ArgumentTokenizer.MESSAGE_TEXT_AFTER_QUOTE, () ->
                tokenize(" t/\"\\\"Math\\\"\" extra"));
    }

    @Test
    public void tokenize_textAfterQuote_throwsHelpfulError() {
        for (String suffix : List.of(" extra", "p/91234567", " \"Kaur\"")) {
            assertThrows(ParseException.class, ArgumentTokenizer.MESSAGE_TEXT_AFTER_QUOTE, () ->
                    tokenize(" n/\"Rina\"" + suffix));
            assertThrows(ParseException.class, ArgumentTokenizer.MESSAGE_TEXT_AFTER_QUOTE, () ->
                    tokenize(" t/\"Sec 4\"" + suffix));
        }
    }

    @Test
    public void tokenize_unknownPrefixAfterQuotedValue_reportsUnknownPrefix() {
        for (String input : List.of(" n/\"Rina\" x/unknown", " t/\"Sec 4\" x/unknown")) {
            assertThrows(ParseException.class, String.format(ArgumentTokenizer.MESSAGE_UNKNOWN_PREFIX, "x/"), () ->
                    tokenize(input));
        }
    }

    private ArgumentMultimap tokenize(String input) throws ParseException {
        return ArgumentTokenizer.tokenizeWithQuotedValues(input, List.of(PREFIX_NAME, PREFIX_TAG),
                PREFIX_PHONE, PREFIX_ADDRESS);
    }
}
