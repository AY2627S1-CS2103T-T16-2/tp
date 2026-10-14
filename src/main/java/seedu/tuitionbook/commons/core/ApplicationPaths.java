package seedu.tuitionbook.commons.core;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;

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

    /**
     * Lexically resolves an application-relative path inside the application home directory.
     * This containment check normalizes path elements but does not resolve symbolic links.
     */
    public static Path resolve(String first, String... more) {
        return resolve(APPLICATION_HOME, first, more);
    }

    /** Lexically resolves an application-relative path inside the supplied application home directory. */
    static Path resolve(Path applicationHome, String first, String... more) {
        requireNonNull(applicationHome);
        Path normalizedHome = applicationHome.toAbsolutePath().normalize();
        Path relativePath = Path.of(first, more);
        if (relativePath.isAbsolute()) {
            throw new IllegalArgumentException("Application path must be relative: " + relativePath);
        }

        Path resolvedPath = normalizedHome.resolve(relativePath).normalize();
        if (!resolvedPath.startsWith(normalizedHome)) {
            throw new IllegalArgumentException("Application path must remain inside application home: " + relativePath);
        }
        return resolvedPath;
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
            // An absolute regular-file path cannot be a filesystem root, so it always has a parent directory.
            return codeSourcePath.getParent();
        }
        return developmentFallback.toAbsolutePath().normalize();
    }

    private static Path determineApplicationHome() {
        return determineApplicationHome(ApplicationPaths.class.getProtectionDomain().getCodeSource(), DEVELOPMENT_HOME);
    }

    /** Resolves the application home from runtime code-source information. */
    static Path determineApplicationHome(CodeSource codeSource, Path developmentFallback) {
        requireNonNull(developmentFallback);
        if (codeSource == null || codeSource.getLocation() == null) {
            return developmentFallback.toAbsolutePath().normalize();
        }
        try {
            URI codeSourceLocation = codeSource.getLocation().toURI();
            return resolveApplicationHome(codeSourceLocation, developmentFallback);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to resolve the TuitionBook application location", e);
        }
    }
}
