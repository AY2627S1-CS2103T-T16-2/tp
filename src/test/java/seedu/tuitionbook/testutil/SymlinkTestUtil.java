package seedu.tuitionbook.testutil;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.junit.jupiter.api.Assumptions;

/** Creates symbolic links for tests, skipping only when the platform does not permit link creation. */
public final class SymlinkTestUtil {

    private SymlinkTestUtil() {
    }

    /** Creates {@code link}, aborting the test only when the platform rejects symbolic-link creation. */
    public static Path createSymbolicLink(Path link, Path target) throws IOException {
        try {
            return Files.createSymbolicLink(link, target);
        } catch (UnsupportedOperationException | AccessDeniedException exception) {
            Assumptions.assumeTrue(false, "Symbolic links are unavailable: " + exception.getMessage());
            throw exception;
        } catch (FileSystemException exception) {
            String reason = (String.valueOf(exception.getReason()) + " " + exception.getMessage())
                    .toLowerCase(Locale.ROOT);
            if (reason.contains("permission denied") || reason.contains("operation not permitted")
                    || reason.contains("privilege")) {
                Assumptions.assumeTrue(false, "Symbolic links are unavailable: " + exception.getMessage());
            }
            throw exception;
        }
    }
}
