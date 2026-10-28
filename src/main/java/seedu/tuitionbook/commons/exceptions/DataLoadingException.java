package seedu.tuitionbook.commons.exceptions;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Represents an error during loading of data from a file.
 */
public class DataLoadingException extends Exception {
    private static final int MAX_GUIDANCE_DETAIL_LENGTH = 1_000;
    private static final String INVALID_CONTACT_GUIDANCE = "The file contains invalid contact data. "
            + "Correct the affected contact field in the preserved file before trying again.";
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
            return INVALID_CONTACT_GUIDANCE;
        }
        if (hasCause(CharacterCodingException.class) || hasCause(JsonProcessingException.class)
                || hasCauseWithMessage(IOException.class, "JSON root value must not be null.")) {
            return "The data file is not valid UTF-8 JSON. Correct the encoding or JSON syntax in the preserved "
                    + "file before trying again.";
        }
        if (hasCause(IOException.class)) {
            return "The data file could not be read because of a file-system error. Check access to the preserved "
                    + "file before trying again.";
        }
        return "The data file could not be read because of a file-system error. Check access to the preserved "
                + "file before trying again.";
    }

    /**
     * Returns a bounded user-facing validation explanation when invalid contact data caused the load failure.
     * The concise guidance remains separate so a long explanation can be placed in expandable recovery details.
     */
    public Optional<String> getUserFacingDetails() {
        if (!hasCause(IllegalValueException.class)) {
            return Optional.empty();
        }
        return Optional.of(causeMessage(IllegalValueException.class));
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
            return "One or more contact fields do not satisfy their validation rules.";
        }
        String singleLineMessage = message.replaceAll("\\R", " ").trim();
        return singleLineMessage.length() > MAX_GUIDANCE_DETAIL_LENGTH
                ? "The validation explanation is too long to display safely. Review the affected contact field "
                        + "against the documented constraints."
                : singleLineMessage;
    }

}
