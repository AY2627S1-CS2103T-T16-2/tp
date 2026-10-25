package seedu.tuitionbook.commons.core;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import seedu.tuitionbook.commons.exceptions.StoragePathException;

/** Revalidates physical containment of application-managed paths before filesystem access. */
public final class ApplicationPathGuard {

    private final Path applicationHome;

    public ApplicationPathGuard(Path applicationHome) {
        this.applicationHome = requireNonNull(applicationHome).toAbsolutePath().normalize();
    }

    /**
     * Verifies that {@code path} is lexically and physically contained in the application home.
     * Existing symbolic links are resolved, while missing suffixes are resolved beneath their deepest existing
     * ancestor. Dangling, looping, or otherwise unresolvable links are rejected.
     */
    public void verify(Path path) throws StoragePathException {
        requireNonNull(path);
        Path candidate = path.toAbsolutePath().normalize();
        if (!candidate.startsWith(applicationHome)) {
            throw rejectedOutside(candidate, applicationHome);
        }

        Path realHome;
        try {
            realHome = applicationHome.toRealPath();
        } catch (IOException exception) {
            throw rejectedUnresolvable(applicationHome, applicationHome, exception);
        }

        Path lexicalPath = applicationHome;
        Path resolvedPath = realHome;
        for (Path component : applicationHome.relativize(candidate)) {
            lexicalPath = lexicalPath.resolve(component);
            try {
                Files.readAttributes(lexicalPath, BasicFileAttributes.class, NOFOLLOW_LINKS);
            } catch (NoSuchFileException exception) {
                // The candidate was normalized and every existing ancestor was verified inside realHome, so resolving
                // a missing suffix beneath the verified ancestor cannot escape realHome.
                resolvedPath = resolvedPath.resolve(component).normalize();
                continue;
            } catch (IOException exception) {
                throw rejectedUnresolvable(candidate, applicationHome, exception);
            }

            try {
                resolvedPath = lexicalPath.toRealPath();
            } catch (IOException exception) {
                throw rejectedUnresolvable(candidate, applicationHome, exception);
            }

            if (!resolvedPath.startsWith(realHome)) {
                throw rejectedOutside(candidate, applicationHome);
            }
        }
    }

    private static StoragePathException rejectedOutside(Path path, Path applicationHome) {
        return new StoragePathException(path, "Refusing to access application-managed path " + path
                + " because it resolves outside the application folder " + applicationHome
                + ". Correct or remove the symbolic link, then retry or restart TuitionBook.");
    }

    private static StoragePathException rejectedUnresolvable(
            Path path, Path applicationHome, IOException cause) {
        return new StoragePathException(path, "Refusing to access application-managed path " + path
                + " because it cannot be safely resolved inside the application folder " + applicationHome
                + ". Correct or remove any dangling, looping, or inaccessible symbolic link, then retry or restart "
                + "TuitionBook.", cause);
    }
}
