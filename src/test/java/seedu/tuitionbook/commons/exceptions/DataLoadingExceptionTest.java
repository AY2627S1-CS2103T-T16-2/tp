package seedu.tuitionbook.commons.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.file.AccessDeniedException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;

import seedu.tuitionbook.model.person.Email;
import seedu.tuitionbook.model.person.Name;

public class DataLoadingExceptionTest {

    @Test
    public void getUserFacingGuidance_accessDenied_mentionsPermission() {
        DataLoadingException exception = new DataLoadingException(
                new IOException("wrapper", new AccessDeniedException("contacts.json")));

        String guidance = exception.getUserFacingGuidance();

        assertTrue(guidance.contains("permission"));
        assertTrue(guidance.contains("read permission"));
        assertFalse(guidance.contains("contacts.json"));
    }

    @Test
    public void getUserFacingGuidance_invalidContactValue_mentionsContactData() {
        String guidance = new DataLoadingException(new IllegalValueException("Invalid phone"))
                .getUserFacingGuidance();

        assertTrue(guidance.contains("invalid contact data"));
        assertTrue(guidance.contains("Invalid phone"));
    }

    @Test
    public void getUserFacingGuidance_nestedInvalidContactValue_mentionsContactData() {
        Exception wrappedCause = new IOException("wrapper", new IllegalValueException("Invalid phone"));
        String guidance = new DataLoadingException(wrappedCause).getUserFacingGuidance();

        assertTrue(guidance.contains("invalid contact data"));
        assertTrue(guidance.contains("Invalid phone"));
    }

    @Test
    public void getUserFacingGuidance_jsonFailure_mentionsEncodingAndSyntax() {
        String guidance = new DataLoadingException(new JsonParseException(null, "Invalid JSON"))
                .getUserFacingGuidance();

        assertTrue(guidance.contains("not in a format TuitionBook can read"));
        assertTrue(guidance.contains("missing comma, quote or bracket"));
    }

    @Test
    public void getUserFacingGuidance_invalidUtf8_mentionsEncodingAndSyntax() {
        String guidance = new DataLoadingException(new CharacterCodingException())
                .getUserFacingGuidance();

        assertTrue(guidance.contains("not in a format TuitionBook can read"));
    }

    @Test
    public void getUserFacingGuidance_nullJsonRoot_mentionsEncodingAndSyntax() {
        String guidance = new DataLoadingException(new IOException("JSON root value must not be null."))
                .getUserFacingGuidance();

        assertTrue(guidance.contains("not in a format TuitionBook can read"));
    }

    @Test
    public void getUserFacingGuidance_otherIoFailure_mentionsFileSystem() {
        String guidance = new DataLoadingException(new IOException("disk failure"))
                .getUserFacingGuidance();

        assertTrue(guidance.contains("file-system error"));
        assertFalse(guidance.contains("disk failure"));
    }

    @Test
    public void getUserFacingGuidance_unknownFailure_mentionsFileSystem() {
        String guidance = new DataLoadingException(new Exception("Unexpected read failure"))
                .getUserFacingGuidance();

        assertTrue(guidance.contains("file-system error"));
    }

    @Test
    public void getUserFacingGuidance_unknownFailure_usesGenericGuidance() {
        String guidance = new DataLoadingException(new RuntimeException("unexpected failure"))
                .getUserFacingGuidance();

        assertEquals("The data file could not be read because of a file-system error. Check access to the preserved "
                + "file before trying again.", guidance);
    }

    @Test
    public void getUserFacingGuidance_blankInvalidValue_usesFallbackDetail() {
        String guidance = new DataLoadingException(new IllegalValueException("  "))
                .getUserFacingGuidance();

        assertTrue(guidance.endsWith("invalid contact data. Check each contact's values."));
    }

    @Test
    public void getUserFacingGuidance_longMultilineInvalidValue_isBoundedAndSingleLine() {
        String longMessage = "first line\n" + "x".repeat(400);
        String guidance = new DataLoadingException(new IllegalValueException(longMessage))
                .getUserFacingGuidance();

        assertTrue(guidance.startsWith("The file contains invalid contact data. first line x"));
        assertTrue(guidance.endsWith("..."));
        assertTrue(guidance.length() <= 400);
    }

    @Test
    public void getUserFacingGuidance_longestNamedContactEmailError_isNotTruncated() {
        String detail = "Contact 9999 in the file (\"" + "A".repeat(100) + "\"): "
                + Email.MESSAGE_INVALID_NAME_CHARACTERS;
        String guidance = new DataLoadingException(new IllegalValueException(detail)).getUserFacingGuidance();

        assertEquals("The file contains invalid contact data. " + detail, guidance);
    }

    @Test
    public void getUserFacingGuidance_longestSupplementaryNamedContactEmailError_isNotTruncated() {
        // U+20000 is a CJK letter outside the Basic Multilingual Plane, taking two UTF-16 units each.
        String longestName = "\uD840\uDC00".repeat(Name.MAX_LENGTH);
        assertTrue(Name.isValidName(longestName));
        String detail = "Contact 9999 in the file (\"" + longestName + "\"): "
                + Email.MESSAGE_INVALID_NAME_CHARACTERS;
        String guidance = new DataLoadingException(new IllegalValueException(detail)).getUserFacingGuidance();

        assertEquals("The file contains invalid contact data. " + detail, guidance);
    }

    @Test
    public void getUserFacingGuidance_longSupplementaryInvalidValue_truncatesWithoutSplittingCharacters() {
        String guidance = new DataLoadingException(new IllegalValueException("\uD840\uDC00".repeat(400)))
                .getUserFacingGuidance();

        assertEquals("The file contains invalid contact data. " + "\uD840\uDC00".repeat(320) + "...", guidance);
    }
}
