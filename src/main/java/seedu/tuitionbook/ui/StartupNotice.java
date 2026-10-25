package seedu.tuitionbook.ui;

import java.nio.file.Path;

/**
 * Describes a contact-data outcome that the UI must explain when the application starts.
 */
public record StartupNotice(Type type, int repairedGuardianLinkCount, Path malformedFileBackupPath,
                            String loadFailureGuidance) {

    /** The kinds of startup feedback shown to the user. */
    public enum Type {
        MISSING_DATA_FILE,
        LOAD_FAILED_BACKED_UP,
        LOAD_FAILED_BACKUP_FAILED,
        GUARDIAN_LINKS_REPAIRED
    }

    /** Validates that the repair count matches the notice type. */
    public StartupNotice {
        if (type == null) {
            throw new NullPointerException("Notice type cannot be null.");
        }
        if (repairedGuardianLinkCount < 0 || (type == Type.LOAD_FAILED_BACKED_UP && malformedFileBackupPath == null)
                || (type != Type.LOAD_FAILED_BACKED_UP && malformedFileBackupPath != null)
                || (type == Type.GUARDIAN_LINKS_REPAIRED && repairedGuardianLinkCount == 0)
                || (type != Type.GUARDIAN_LINKS_REPAIRED && repairedGuardianLinkCount != 0)
                || (loadFailureGuidance != null && loadFailureGuidance.isBlank())
                || (type != Type.LOAD_FAILED_BACKED_UP && type != Type.LOAD_FAILED_BACKUP_FAILED
                        && loadFailureGuidance != null)) {
            throw new IllegalArgumentException("Notice details do not match the notice type.");
        }
    }

    /** Returns a notice for a missing contact-data file. */
    public static StartupNotice missingDataFile() {
        return new StartupNotice(Type.MISSING_DATA_FILE, 0, null, null);
    }

    /** Returns a notice for a contact-data file that could not be loaded. */
    public static StartupNotice loadFailedBackedUp(Path backupPath) {
        return loadFailedBackedUp(backupPath, null);
    }

    /** Returns a notice for a failed load whose original file was preserved, with specific recovery guidance. */
    public static StartupNotice loadFailedBackedUp(Path backupPath, String guidance) {
        return new StartupNotice(Type.LOAD_FAILED_BACKED_UP, 0, backupPath, guidance);
    }

    /** Returns a notice for a contact-data file that could not be preserved. */
    public static StartupNotice loadFailedBackupFailed() {
        return loadFailedBackupFailed(null);
    }

    /** Returns a notice for a failed load and backup, with specific recovery guidance. */
    public static StartupNotice loadFailedBackupFailed(String guidance) {
        return new StartupNotice(Type.LOAD_FAILED_BACKUP_FAILED, 0, null, guidance);
    }

    /** Returns a notice for invalid guardian links removed while loading. */
    public static StartupNotice guardianLinksRepaired(int repairedGuardianLinkCount) {
        return new StartupNotice(Type.GUARDIAN_LINKS_REPAIRED, repairedGuardianLinkCount, null, null);
    }
}
