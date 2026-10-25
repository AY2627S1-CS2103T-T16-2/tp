package seedu.tuitionbook.commons.exceptions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;

public class DataLoadingExceptionTest {

    @Test
    public void getUserFacingGuidance_accessDenied_explainsPermissionRecovery() {
        DataLoadingException exception = new DataLoadingException(
                new IOException("wrapper", new AccessDeniedException("contacts.json")));

        assertTrue(exception.getUserFacingGuidance().contains("permission"));
    }

    @Test
    public void getUserFacingGuidance_illegalValue_includesValidationMessage() {
        DataLoadingException exception = new DataLoadingException(new IllegalValueException("Invalid phone"));

        String guidance = exception.getUserFacingGuidance();
        assertTrue(guidance.contains("invalid contact data"));
        assertTrue(guidance.contains("Invalid phone"));
    }

    @Test
    public void getUserFacingGuidance_jsonProcessing_explainsEncodingOrSyntaxRecovery() {
        DataLoadingException exception = new DataLoadingException(new JsonParseException(null, "Invalid JSON"));

        String guidance = exception.getUserFacingGuidance();
        assertTrue(guidance.contains("UTF-8 JSON"));
        assertTrue(guidance.contains("encoding or JSON syntax"));
    }

    @Test
    public void getUserFacingGuidance_nullJsonRoot_explainsEncodingOrSyntaxRecovery() {
        DataLoadingException exception = new DataLoadingException(
                new IOException("JSON root value must not be null."));

        assertTrue(exception.getUserFacingGuidance().contains("UTF-8 JSON"));
    }

    @Test
    public void getUserFacingGuidance_otherIoFailure_explainsFileSystemRecovery() {
        DataLoadingException exception = new DataLoadingException(new IOException("Device unavailable"));

        assertTrue(exception.getUserFacingGuidance().contains("file-system error"));
    }

    @Test
    public void getUserFacingGuidance_otherFailure_explainsFileSystemRecovery() {
        DataLoadingException exception = new DataLoadingException(new Exception("Unexpected read failure"));

        assertTrue(exception.getUserFacingGuidance().contains("file-system error"));
    }
}
