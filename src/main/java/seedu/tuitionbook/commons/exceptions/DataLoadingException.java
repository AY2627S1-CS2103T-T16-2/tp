package seedu.tuitionbook.commons.exceptions;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Represents an error during loading of data from a file.
 */
public class DataLoadingException extends Exception {
    private static final int MAX_GUIDANCE_DETAIL_LENGTH = 200;
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
            return "The file contains invalid contact data: " + detail
                    + (detail.endsWith(".") ? "" : ".");
        }
        if (hasCause(JsonProcessingException.class)) {
            return "The file is not valid UTF-8 JSON. Check its encoding or JSON syntax before trying again.";
        }
        if (hasCause(IOException.class)) {
            return "TuitionBook could not read the data file because of a file-system error. "
                    + "Check the file and its containing folder before trying again.";
        }
        return "TuitionBook could not read the data file. Check the file before trying again.";
    }

    private boolean hasCause(Class<? extends Throwable> type) {
        for (Throwable current = getCause(); current != null; current = current.getCause()) {
            if (type.isInstance(current)) {
                return true;
            }
        }
        return false;
    }

    private String causeMessage(Class<? extends Throwable> type) {
        for (Throwable current = getCause(); current != null; current = current.getCause()) {
            if (type.isInstance(current)) {
                String message = current.getMessage();
                if (message == null || message.isBlank()) {
                    return "the contact fields are invalid";
                }
                String singleLineMessage = message.replaceAll("\\R", " ").trim();
                return singleLineMessage.length() > MAX_GUIDANCE_DETAIL_LENGTH
                        ? singleLineMessage.substring(0, MAX_GUIDANCE_DETAIL_LENGTH) + "..."
                        : singleLineMessage;
            }
        }
        return "the contact fields are invalid";
    }

}
