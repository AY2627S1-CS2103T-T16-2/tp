package seedu.tuitionbook.testutil;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assumptions;

/** Creates symbolic links for tests, skipping only when the platform does not permit link creation. */
public final class SymlinkTestUtil {

    private SymlinkTestUtil() {
    }

    /** Creates {@code link}, aborting the test only when the platform rejects symbolic-link creation. */
    public static Path createSymbolicLink(Path link, Path target) throws IOException {
        try {
            return Files.createSymbolicLink(link, target);
        } catch (UnsupportedOperationException | FileSystemException exception) {
            Assumptions.assumeTrue(false, "Symbolic links are unavailable: " + exception.getMessage());
            throw exception;
        }
    }
}
