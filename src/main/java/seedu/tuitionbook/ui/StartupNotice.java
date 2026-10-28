package seedu.tuitionbook.ui;

import java.nio.file.Path;
import java.util.List;

import seedu.tuitionbook.storage.GuardianLinkRepair;

/**
 * Describes a contact-data outcome that the UI must explain when the application starts.
 */
public record StartupNotice(Type type, List<GuardianLinkRepair> guardianLinkRepairs,
                            Path malformedFileBackupPath, String loadFailureGuidance,
                            String loadFailureDetails) {

    /** The kinds of startup feedback shown to the user. */
    public enum Type {
        MISSING_DATA_FILE,
        LOAD_FAILED_BACKED_UP,
        LOAD_FAILED_BACKUP_FAILED,
        GUARDIAN_LINKS_REPAIRED
    }

    /** Validates that the repair diagnostics match the notice type. */
    public StartupNotice {
        if (type == null) {
            throw new NullPointerException("Notice type cannot be null.");
        }
        guardianLinkRepairs = List.copyOf(guardianLinkRepairs);
        boolean isLoadFailure = type == Type.LOAD_FAILED_BACKED_UP || type == Type.LOAD_FAILED_BACKUP_FAILED;
        if ((type == Type.LOAD_FAILED_BACKED_UP && malformedFileBackupPath == null)
                || (type != Type.LOAD_FAILED_BACKED_UP && malformedFileBackupPath != null)
                || (type == Type.GUARDIAN_LINKS_REPAIRED && guardianLinkRepairs.isEmpty())
                || (type != Type.GUARDIAN_LINKS_REPAIRED && !guardianLinkRepairs.isEmpty())
                || (!isLoadFailure && loadFailureGuidance != null)
                || (!isLoadFailure && loadFailureDetails != null)
                || (isLoadFailure && loadFailureGuidance != null && loadFailureGuidance.isBlank())
                || (isLoadFailure && loadFailureDetails != null && loadFailureDetails.isBlank())
                || (loadFailureDetails != null && loadFailureGuidance == null)) {
            throw new IllegalArgumentException("Notice details do not match the notice type.");
        }
    }

    /** Creates a notice without expanded load-failure details. */
    public StartupNotice(Type type, List<GuardianLinkRepair> guardianLinkRepairs,
                         Path malformedFileBackupPath, String loadFailureGuidance) {
        this(type, guardianLinkRepairs, malformedFileBackupPath, loadFailureGuidance, null);
    }

    /** Returns the number of guardian links repaired during loading. */
    public int repairedGuardianLinkCount() {
        return guardianLinkRepairs.size();
    }

    /** Returns a notice for a missing contact-data file. */
    public static StartupNotice missingDataFile() {
        return new StartupNotice(Type.MISSING_DATA_FILE, List.of(), null, null, null);
    }

    /** Returns a notice for a contact-data file that could not be loaded. */
    public static StartupNotice loadFailedBackedUp(Path backupPath) {
        return loadFailedBackedUp(backupPath, null);
    }

    /** Returns a notice for a contact-data file that could not be loaded, with cause-specific guidance. */
    public static StartupNotice loadFailedBackedUp(Path backupPath, String guidance) {
        return loadFailedBackedUp(backupPath, guidance, null);
    }

    /** Returns a notice for a contact-data file that could not be loaded, with expandable details. */
    public static StartupNotice loadFailedBackedUp(Path backupPath, String guidance, String details) {
        return new StartupNotice(Type.LOAD_FAILED_BACKED_UP, List.of(), backupPath, guidance, details);
    }

    /** Returns a notice for a contact-data file that could not be preserved. */
    public static StartupNotice loadFailedBackupFailed() {
        return loadFailedBackupFailed(null);
    }

    /** Returns a notice for an unpreserved contact-data file, with cause-specific guidance. */
    public static StartupNotice loadFailedBackupFailed(String guidance) {
        return loadFailedBackupFailed(guidance, null);
    }

    /** Returns a notice for an unpreserved contact-data file, with expandable details. */
    public static StartupNotice loadFailedBackupFailed(String guidance, String details) {
        return new StartupNotice(Type.LOAD_FAILED_BACKUP_FAILED, List.of(), null, guidance, details);
    }

    /** Returns a notice for invalid guardian links removed while loading. */
    public static StartupNotice guardianLinksRepaired(List<GuardianLinkRepair> guardianLinkRepairs) {
        return new StartupNotice(Type.GUARDIAN_LINKS_REPAIRED, guardianLinkRepairs, null, null, null);
    }
}
