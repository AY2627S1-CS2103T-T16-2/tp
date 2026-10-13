package seedu.tuitionbook.ui;

/**
 * Describes a contact-data outcome that the UI must explain when the application starts.
 */
public record StartupNotice(Type type, int repairedGuardianLinkCount) {

    /** The kinds of startup feedback shown to the user. */
    public enum Type {
        MISSING_DATA_FILE,
        LOAD_FAILED,
        GUARDIAN_LINKS_REPAIRED
    }

    /** Validates that the repair count matches the notice type. */
    public StartupNotice {
        if (type == null) {
            throw new NullPointerException("Notice type cannot be null.");
        }
        if (repairedGuardianLinkCount < 0
                || (type == Type.GUARDIAN_LINKS_REPAIRED && repairedGuardianLinkCount == 0)
                || (type != Type.GUARDIAN_LINKS_REPAIRED && repairedGuardianLinkCount != 0)) {
            throw new IllegalArgumentException("Repair count does not match the notice type.");
        }
    }

    /** Returns a notice for a missing contact-data file. */
    public static StartupNotice missingDataFile() {
        return new StartupNotice(Type.MISSING_DATA_FILE, 0);
    }

    /** Returns a notice for a contact-data file that could not be loaded. */
    public static StartupNotice loadFailed() {
        return new StartupNotice(Type.LOAD_FAILED, 0);
    }

    /** Returns a notice for invalid guardian links removed while loading. */
    public static StartupNotice guardianLinksRepaired(int repairedGuardianLinkCount) {
        return new StartupNotice(Type.GUARDIAN_LINKS_REPAIRED, repairedGuardianLinkCount);
    }
}
