package seedu.tuitionbook.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ApplicationPathsTest {

    @TempDir
    public Path testFolder;

    @Test
    public void resolveApplicationHome_jarLocation_usesContainingDirectory() throws Exception {
        Path jarDirectory = Files.createDirectories(testFolder.resolve("JAR home 测试 100% ready"));
        Path jarFile = Files.createFile(jarDirectory.resolve("tuition book 100%.jar"));
        Path unrelatedLaunchDirectory = Files.createDirectories(testFolder.resolve("launch directory"));

        Path result = ApplicationPaths.resolveApplicationHome(jarFile.toUri(), unrelatedLaunchDirectory);

        assertEquals(jarDirectory.toAbsolutePath().normalize(), result);
    }

    @Test
    public void resolveApplicationHome_classDirectory_usesExplicitDevelopmentFallback() throws Exception {
        Path classDirectory = Files.createDirectories(testFolder.resolve("classes"));
        Path developmentFallback = testFolder.resolve("development checkout");

        Path result = ApplicationPaths.resolveApplicationHome(classDirectory.toUri(), developmentFallback);

        assertEquals(developmentFallback.toAbsolutePath().normalize(), result);
    }
}
