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

    /** Returns recovery guidance suitable for displaying without exposing exception internals. */
    public String getUserFacingGuidance() {
        Throwable rootCause = getRootCause();
        if (rootCause instanceof AccessDeniedException) {
            return "TuitionBook did not have permission to read the data file. Restore read permission on the "
                    + "preserved file before trying again.";
        }
        if (rootCause instanceof IllegalValueException) {
            return "The data file contains invalid contact data: " + rootCause.getMessage()
                    + " Correct the reported value in the preserved file before trying again.";
        }
        if (rootCause instanceof JsonProcessingException
                || (rootCause instanceof IOException && "JSON root value must not be null.".equals(
                        rootCause.getMessage()))) {
            return "The data file is not valid UTF-8 JSON. Correct the encoding or JSON syntax in the preserved "
                    + "file before trying again.";
        }
        return "The data file could not be read because of a file-system error. Check access to the preserved file "
                + "before trying again.";
    }

    private Throwable getRootCause() {
        Throwable rootCause = this;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }

}
