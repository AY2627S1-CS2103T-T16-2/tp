package seedu.tuitionbook.commons.core;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Resolves files managed by TuitionBook relative to the application home directory. */
public final class ApplicationPaths {

    private static final Path DEVELOPMENT_HOME = Path.of("").toAbsolutePath().normalize();
    private static final Path APPLICATION_HOME = determineApplicationHome();

    private ApplicationPaths() {
    }

    /** Returns the directory that contains application-managed files. */
    public static Path getApplicationHome() {
        return APPLICATION_HOME;
    }

    /** Resolves a path inside the application home directory. */
    public static Path resolve(String first, String... more) {
        return APPLICATION_HOME.resolve(Path.of(first, more));
    }

    /**
     * Resolves the application home for a code-source location.
     * A packaged application uses the directory containing its JAR. A class-directory launch uses the supplied
     * development fallback.
     */
    static Path resolveApplicationHome(URI codeSourceLocation, Path developmentFallback) {
        requireNonNull(codeSourceLocation);
        requireNonNull(developmentFallback);

        Path codeSourcePath = Path.of(codeSourceLocation).toAbsolutePath().normalize();
        if (Files.isRegularFile(codeSourcePath)) {
            Path jarDirectory = codeSourcePath.getParent();
            if (jarDirectory == null) {
                throw new IllegalStateException("Executable JAR does not have a parent directory: " + codeSourcePath);
            }
            return jarDirectory;
        }
        return developmentFallback.toAbsolutePath().normalize();
    }

    private static Path determineApplicationHome() {
        if (ApplicationPaths.class.getProtectionDomain().getCodeSource() == null) {
            return DEVELOPMENT_HOME;
        }
        try {
            URI codeSourceLocation = ApplicationPaths.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            return resolveApplicationHome(codeSourceLocation, DEVELOPMENT_HOME);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to resolve the TuitionBook application location", e);
        }
    }
}
