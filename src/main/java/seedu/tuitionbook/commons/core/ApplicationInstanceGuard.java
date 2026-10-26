package seedu.tuitionbook.commons.core;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;

/**
 * Holds an exclusive operating-system lock for one TuitionBook application home directory.
 */
public final class ApplicationInstanceGuard implements AutoCloseable {

    public static final String LOCK_FILE_NAME = ".tuitionbook.lock";

    private final FileChannel channel;
    private final FileLock lock;
    private boolean isClosed;

    private ApplicationInstanceGuard(FileChannel channel, FileLock lock) {
        this.channel = channel;
        this.lock = lock;
    }

    /**
     * Acquires the guard for {@code applicationHome} without waiting.
     *
     * @throws AlreadyRunningException if this or another process already holds the guard
     * @throws IOException if the lock file cannot be opened or locked
     */
    public static ApplicationInstanceGuard acquire(Path applicationHome)
            throws AlreadyRunningException, IOException {
        return acquire(applicationHome, lockFile -> FileChannel.open(lockFile, CREATE, WRITE));
    }

    /**
     * Acquires the guard using {@code channelOpener}.
     * The injectable opener keeps acquisition-error tests independent of platform file permissions.
     */
    static ApplicationInstanceGuard acquire(Path applicationHome, ChannelOpener channelOpener)
            throws AlreadyRunningException, IOException {
        requireNonNull(applicationHome);
        requireNonNull(channelOpener);
        Path lockFile = applicationHome.toAbsolutePath().normalize().resolve(LOCK_FILE_NAME);
        FileChannel channel = null;
        try {
            channel = channelOpener.open(lockFile);
            FileLock lock = channel.tryLock();
            if (lock == null) {
                AlreadyRunningException exception = new AlreadyRunningException(lockFile);
                closeAfterFailedAcquisition(channel, exception);
                throw exception;
            }
            return new ApplicationInstanceGuard(channel, lock);
        } catch (OverlappingFileLockException e) {
            AlreadyRunningException exception = new AlreadyRunningException(lockFile);
            exception.addSuppressed(e);
            closeAfterFailedAcquisition(channel, exception);
            throw exception;
        } catch (IOException e) {
            closeAfterFailedAcquisition(channel, e);
            throw e;
        } catch (SecurityException e) {
            IOException exception = new IOException("Access to the instance lock was denied: " + lockFile, e);
            closeAfterFailedAcquisition(channel, exception);
            throw exception;
        }
    }

    private static void closeAfterFailedAcquisition(FileChannel channel, Throwable failure) {
        if (channel == null) {
            return;
        }
        closeAfterFailedAcquisition(channel::close, failure);
    }

    static void closeAfterFailedAcquisition(IoAction closeAction, Throwable failure) {
        try {
            closeAction.run();
        } catch (IOException closeException) {
            failure.addSuppressed(closeException);
        }
    }

    /** Releases this guard. Calling this method more than once has no effect. */
    @Override
    public synchronized void close() throws IOException {
        if (isClosed) {
            return;
        }
        isClosed = true;

        closeResources(() -> {
            if (lock.isValid()) {
                lock.release();
            }
        }, channel::close);
    }

    static void closeResources(IoAction releaseLock, IoAction closeChannel) throws IOException {
        IOException releaseFailure = null;
        try {
            releaseLock.run();
        } catch (IOException e) {
            releaseFailure = e;
        }

        try {
            closeChannel.run();
        } catch (IOException e) {
            if (releaseFailure == null) {
                releaseFailure = e;
            } else {
                releaseFailure.addSuppressed(e);
            }
        }

        if (releaseFailure != null) {
            throw releaseFailure;
        }
    }

    @FunctionalInterface
    interface ChannelOpener {
        FileChannel open(Path lockFile) throws IOException;
    }

    @FunctionalInterface
    interface IoAction {
        void run() throws IOException;
    }

    /** Indicates that the application home is already guarded by a running instance. */
    public static final class AlreadyRunningException extends Exception {
        private final Path lockFile;

        private AlreadyRunningException(Path lockFile) {
            super("The application home is already in use: " + lockFile.getParent());
            this.lockFile = lockFile;
        }

        public Path getLockFile() {
            return lockFile;
        }
    }
}
