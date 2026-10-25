package seedu.tuitionbook.commons.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.tuitionbook.commons.exceptions.StoragePathException;
import seedu.tuitionbook.testutil.SymlinkTestUtil;

public class ApplicationPathGuardTest {

    @TempDir
    public Path testFolder;

    @Test
    public void verify_missingPathInsideHome_allowed() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("home"));
        ApplicationPathGuard guard = new ApplicationPathGuard(applicationHome);

        assertDoesNotThrow(() -> guard.verify(applicationHome.resolve("missing/data/tuitionbook.json")));
    }

    @Test
    public void verify_parentSymlinkOutsideHome_rejected() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("home"));
        Path outside = Files.createDirectory(testFolder.resolve("outside"));
        SymlinkTestUtil.createSymbolicLink(applicationHome.resolve("data"), outside);

        assertThrows(StoragePathException.class, () -> new ApplicationPathGuard(applicationHome)
                .verify(applicationHome.resolve("data/file.json")));
    }

    @Test
    public void verify_parentSymlinkInsideHome_allowed() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("home"));
        Path internalData = Files.createDirectory(applicationHome.resolve("internal-data"));
        SymlinkTestUtil.createSymbolicLink(applicationHome.resolve("data"), internalData);

        assertDoesNotThrow(() -> new ApplicationPathGuard(applicationHome)
                .verify(applicationHome.resolve("data/file.json")));
    }

    @Test
    public void verify_danglingAndLoopingLinks_rejected() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("home"));
        Path dangling = applicationHome.resolve("dangling");
        Path loop = applicationHome.resolve("loop");
        SymlinkTestUtil.createSymbolicLink(dangling, applicationHome.resolve("missing"));
        SymlinkTestUtil.createSymbolicLink(loop, loop);
        ApplicationPathGuard guard = new ApplicationPathGuard(applicationHome);

        assertThrows(StoragePathException.class, () -> guard.verify(dangling.resolve("file.json")));
        assertThrows(StoragePathException.class, () -> guard.verify(loop.resolve("file.json")));
    }

    @Test
    public void verify_similarTextualPrefix_rejected() throws Exception {
        Path applicationHome = Files.createDirectory(testFolder.resolve("home"));
        Path similarSibling = Files.createDirectory(testFolder.resolve("home-backup"));

        assertThrows(StoragePathException.class, () -> new ApplicationPathGuard(applicationHome)
                .verify(similarSibling.resolve("file.json")));
    }

    @Test
    public void verify_applicationHomeAlias_resolvesConsistently() throws Exception {
        Path realHome = Files.createDirectory(testFolder.resolve("real-home"));
        Path homeAlias = testFolder.resolve("home-alias");
        SymlinkTestUtil.createSymbolicLink(homeAlias, realHome);

        assertDoesNotThrow(() -> new ApplicationPathGuard(homeAlias).verify(homeAlias.resolve("missing.json")));
    }
}
