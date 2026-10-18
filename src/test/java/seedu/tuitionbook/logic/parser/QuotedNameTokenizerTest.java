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

public class QuotedNameTokenizerTest {
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
        assertThrows(ParseException.class, ArgumentTokenizer.MESSAGE_UNCLOSED_QUOTE, () ->
                tokenize(" n/\"Rina a/p Kaur p/91234567"));
    }

    @Test
    public void tokenize_textAfterQuote_throwsHelpfulError() {
        for (String suffix : List.of(" extra", "p/91234567", " \"Kaur\"", " x/unknown")) {
            assertThrows(ParseException.class, ArgumentTokenizer.MESSAGE_TEXT_AFTER_QUOTE, () ->
                    tokenize(" n/\"Rina\"" + suffix));
        }
    }

    private ArgumentMultimap tokenize(String input) throws ParseException {
        return ArgumentTokenizer.tokenizeWithQuotedValue(input, PREFIX_NAME,
                PREFIX_NAME, PREFIX_PHONE, PREFIX_ADDRESS, PREFIX_TAG);
    }
}
