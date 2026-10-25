package seedu.tuitionbook.commons.exceptions;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/** Indicates that an application-managed path cannot be safely contained in the application home. */
public class StoragePathException extends IOException {

    private final Path rejectedPath;

    /** Creates a failure for {@code rejectedPath} with corrective guidance in {@code message}. */
    public StoragePathException(Path rejectedPath, String message) {
        super(message);
        this.rejectedPath = rejectedPath;
    }

    /** Creates a failure for {@code rejectedPath} caused by a filesystem resolution error. */
    public StoragePathException(Path rejectedPath, String message, IOException cause) {
        super(message, cause);
        this.rejectedPath = rejectedPath;
    }

    /** Returns the path that could not be safely resolved. */
    public Path getRejectedPath() {
        return rejectedPath;
    }

    /** Returns the first storage-path failure in {@code throwable}'s cause chain, if present. */
    public static Optional<StoragePathException> findIn(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof StoragePathException storagePathException) {
                return Optional.of(storagePathException);
            }
            current = current.getCause();
        }
        return Optional.empty();
    }
}
