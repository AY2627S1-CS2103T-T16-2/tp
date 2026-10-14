package seedu.tuitionbook.commons.exceptions;

import java.nio.file.Path;
import java.util.Optional;

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

}
