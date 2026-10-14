package seedu.tuitionbook.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.cert.Certificate;

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

    @Test
    public void applicationHomeAndResolve_areConsistent() {
        Path applicationHome = ApplicationPaths.getApplicationHome();

        assertEquals(applicationHome.resolve(Path.of("data", "tuitionbook.json")),
                ApplicationPaths.resolve("data", "tuitionbook.json"));
        assertEquals(applicationHome.toAbsolutePath().normalize(), applicationHome);
    }

    @Test
    public void resolve_commonApplicationPaths_returnsPathsInsideHome() {
        Path applicationHome = testFolder.resolve("TuitionBook home");

        assertEquals(applicationHome.toAbsolutePath().normalize().resolve("preferences.json"),
                ApplicationPaths.resolve(applicationHome, "preferences.json"));
        assertEquals(applicationHome.toAbsolutePath().normalize().resolve(Path.of("data", "tuitionbook.json")),
                ApplicationPaths.resolve(applicationHome, "data", "tuitionbook.json"));
    }

    @Test
    public void resolve_multiPartPathWithNormalization_staysInsideHome() {
        Path applicationHome = testFolder.resolve("home with spaces 测试 100%");

        Path result = ApplicationPaths.resolve(applicationHome,
                "nested folder %t", "temporary", "..", "contact 100%.json");

        assertEquals(applicationHome.toAbsolutePath().normalize()
                .resolve(Path.of("nested folder %t", "contact 100%.json")), result);
    }

    @Test
    public void resolve_parentTraversalOutsideHome_throwsIllegalArgumentException() {
        Path applicationHome = testFolder.resolve("home");

        assertThrows(IllegalArgumentException.class, () ->
                ApplicationPaths.resolve(applicationHome, "..", "outside.json"));
        assertThrows(IllegalArgumentException.class, () -> ApplicationPaths.resolve("..", "outside.json"));
    }

    @Test
    public void resolve_platformNativeAbsolutePath_throwsIllegalArgumentException() {
        Path absoluteOutsidePath = testFolder.resolve("outside.json").toAbsolutePath();

        assertThrows(IllegalArgumentException.class, () ->
                ApplicationPaths.resolve(testFolder.resolve("home"), absoluteOutsidePath.toString()));
        assertThrows(IllegalArgumentException.class, () ->
                ApplicationPaths.resolve(absoluteOutsidePath.toString()));
    }

    @Test
    public void resolve_siblingWithSharedTextualPrefix_throwsIllegalArgumentException() {
        Path applicationHome = testFolder.resolve("tuitionbook-home").toAbsolutePath().normalize();
        Path similarlyNamedSibling = testFolder.resolve("tuitionbook-home-backup").resolve("outside.json")
                .toAbsolutePath().normalize();
        Path relativePathToSibling = applicationHome.relativize(similarlyNamedSibling);

        assertThrows(IllegalArgumentException.class, () ->
                ApplicationPaths.resolve(applicationHome, relativePathToSibling.toString()));
    }

    @Test
    public void determineApplicationHome_missingCodeSource_usesDevelopmentFallback() {
        Path developmentFallback = testFolder.resolve("relative development checkout");

        Path result = ApplicationPaths.determineApplicationHome(null, developmentFallback);

        assertEquals(developmentFallback.toAbsolutePath().normalize(), result);
    }

    @Test
    public void determineApplicationHome_codeSourceWithoutLocation_usesDevelopmentFallback() {
        Path developmentFallback = testFolder.resolve("development checkout");
        CodeSource codeSourceWithoutLocation = new CodeSource(null, (Certificate[]) null);

        Path result = ApplicationPaths.determineApplicationHome(codeSourceWithoutLocation, developmentFallback);

        assertEquals(developmentFallback.toAbsolutePath().normalize(), result);
    }

    @Test
    public void determineApplicationHome_classDirectory_usesSuppliedDevelopmentFallback() throws Exception {
        Path classDirectory = Files.createDirectories(testFolder.resolve("runtime classes"));
        Path developmentFallback = testFolder.resolve("development checkout");
        CodeSource classCodeSource = new CodeSource(classDirectory.toUri().toURL(), (Certificate[]) null);

        Path result = ApplicationPaths.determineApplicationHome(classCodeSource, developmentFallback);

        assertEquals(developmentFallback.toAbsolutePath().normalize(), result);
    }

    @Test
    public void determineApplicationHome_invalidCodeSourceLocation_reportsCause() throws Exception {
        URL invalidLocation = createUrlWithUnescapedSpace();
        CodeSource invalidCodeSource = new CodeSource(invalidLocation, (Certificate[]) null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                ApplicationPaths.determineApplicationHome(invalidCodeSource, testFolder));

        assertInstanceOf(java.net.URISyntaxException.class, exception.getCause());
    }

    @SuppressWarnings("deprecation")
    private static URL createUrlWithUnescapedSpace() throws Exception {
        return new URL(null, "file:/invalid path", new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL url) {
                throw new UnsupportedOperationException("No connection is required for path resolution");
            }
        });
    }
}
