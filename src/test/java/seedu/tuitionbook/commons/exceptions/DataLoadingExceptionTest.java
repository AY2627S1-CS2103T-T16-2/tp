package seedu.tuitionbook.commons.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;

public class DataLoadingExceptionTest {

    @Test
    public void getUserFacingGuidance_accessDenied_mentionsPermission() {
        String guidance = new DataLoadingException(new AccessDeniedException("contacts.json"))
                .getUserFacingGuidance();

        assertTrue(guidance.contains("permission"));
        assertTrue(guidance.contains("read permission"));
        assertFalse(guidance.contains("contacts.json"));
    }

    @Test
    public void getUserFacingGuidance_invalidContactValue_mentionsContactData() {
        String guidance = new DataLoadingException(new IllegalValueException("invalid role"))
                .getUserFacingGuidance();

        assertTrue(guidance.contains("invalid contact data"));
        assertTrue(guidance.contains("invalid role"));
    }

    @Test
    public void getUserFacingGuidance_jsonFailure_mentionsEncodingAndSyntax() {
        String guidance = new DataLoadingException(new JsonParseException(null, "malformed JSON"))
                .getUserFacingGuidance();

        assertTrue(guidance.contains("UTF-8 JSON"));
        assertTrue(guidance.contains("encoding or JSON syntax"));
    }

    @Test
    public void getUserFacingGuidance_otherIoFailure_mentionsFileSystem() {
        String guidance = new DataLoadingException(new IOException("disk failure"))
                .getUserFacingGuidance();

        assertTrue(guidance.contains("file-system error"));
        assertFalse(guidance.contains("disk failure"));
    }

    @Test
    public void getUserFacingGuidance_unknownFailure_usesGenericGuidance() {
        String guidance = new DataLoadingException(new RuntimeException("unexpected failure"))
                .getUserFacingGuidance();

        assertEquals("TuitionBook could not read the data file. Check the file before trying again.", guidance);
    }

    @Test
    public void getUserFacingGuidance_blankInvalidValue_usesFallbackDetail() {
        String guidance = new DataLoadingException(new IllegalValueException("  "))
                .getUserFacingGuidance();

        assertTrue(guidance.endsWith("the contact fields are invalid."));
    }

    @Test
    public void getUserFacingGuidance_longMultilineInvalidValue_isBoundedAndSingleLine() {
        String longMessage = "first line\n" + "x".repeat(250);
        String guidance = new DataLoadingException(new IllegalValueException(longMessage))
                .getUserFacingGuidance();

        assertTrue(guidance.startsWith("The file contains invalid contact data: first line x"));
        assertTrue(guidance.endsWith("..."));
        assertTrue(guidance.length() <= 250);
    }
}
