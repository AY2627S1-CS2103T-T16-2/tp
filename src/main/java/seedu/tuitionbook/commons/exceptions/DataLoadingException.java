package seedu.tuitionbook.commons.exceptions;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * Represents an error during loading of data from a file.
 */
public class DataLoadingException extends Exception {
    private static final int MAX_GUIDANCE_DETAIL_LENGTH = 320;
    private final Path malformedFileBackupPath;

    /**
     * Creates a loading exception without a malformed-file backup path.
     *
     * @param cause loading failure
     */
    public DataLoadingException(Exception cause) {
        super(cause);
        malformedFileBackupPath = null;
    }

    /**
     * Creates an exception that records where an unreadable data file was preserved.
     *
     * @param cause loading failure
     * @param malformedFileBackupPath backup path, or {@code null} when preservation failed
     */
    public DataLoadingException(Exception cause, Path malformedFileBackupPath) {
        super(cause);
        this.malformedFileBackupPath = malformedFileBackupPath;
    }

    /** Returns the backup created while recovering from this loading failure, if any. */
    public Optional<Path> getMalformedFileBackupPath() {
        return Optional.ofNullable(malformedFileBackupPath);
    }

    /** Returns bounded, actionable guidance without exposing implementation details or file paths. */
    public String getUserFacingGuidance() {
        if (hasCause(AccessDeniedException.class)) {
            return "TuitionBook did not have permission to read the data file. "
                    + "Restore read permission on the preserved file before trying again.";
        }
        if (hasCause(IllegalValueException.class)) {
            String detail = causeMessage(IllegalValueException.class);
            return "The file contains invalid contact data. " + detail
                    + (detail.endsWith(".") ? "" : ".");
        }
        if (hasCause(UnrecognizedPropertyException.class)) {
            return "The data file contains a field TuitionBook does not recognise. Check that every field name uses "
                    + "the spelling and letter case shown in the User Guide before trying again.";
        }
        if (hasCause(CharacterCodingException.class) || hasCause(JsonProcessingException.class)
                || hasCauseWithMessage(IOException.class, "JSON root value must not be null.")) {
            return "The data file is not in a format TuitionBook can read. Check the preserved file for mistakes "
                    + "such as a missing comma, quote or bracket, and save it as UTF-8 text before trying again.";
        }
        if (hasCause(IOException.class)) {
            return "The data file could not be read because of a file-system error. Check access to the preserved "
                    + "file before trying again.";
        }
        return "The data file could not be read because of a file-system error. Check access to the preserved "
                + "file before trying again.";
    }

    private boolean hasCause(Class<? extends Throwable> type) {
        for (Throwable current = getCause(); current != null; current = current.getCause()) {
            if (type.isInstance(current)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCauseWithMessage(Class<? extends Throwable> type, String message) {
        for (Throwable current = getCause(); current != null; current = current.getCause()) {
            if (type.isInstance(current) && message.equals(current.getMessage())) {
                return true;
            }
        }
        return false;
    }

    private String causeMessage(Class<? extends Throwable> type) {
        Throwable current = getCause();
        while (!type.isInstance(current)) {
            current = current.getCause();
        }
        String message = current.getMessage();
        if (message == null || message.isBlank()) {
            return "Check each contact's values";
        }
        String singleLineMessage = message.replaceAll("\\R", " ").trim();
        // Count code points, as names do, so supplementary characters neither shorten the bound nor get split.
        return singleLineMessage.codePointCount(0, singleLineMessage.length()) > MAX_GUIDANCE_DETAIL_LENGTH
                ? singleLineMessage.substring(0, singleLineMessage.offsetByCodePoints(0, MAX_GUIDANCE_DETAIL_LENGTH))
                        + "..."
                : singleLineMessage;
    }

}
