package seedu.tuitionbook.ui;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.storage.GuardianLinkRepair;

/**
 * Tests validation of startup notice types and repair diagnostics.
 */
public class StartupNoticeTest {

    @Test
    public void constructor_nullType_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new StartupNotice(null, List.of(), null, null));
    }

    @Test
    public void constructor_repairNoticeWithoutRepairs_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> StartupNotice.guardianLinksRepaired(List.of()));
    }

    @Test
    public void constructor_nullRepairs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StartupNotice.guardianLinksRepaired(null));
    }

    @Test
    public void constructor_otherNoticeWithRepairs_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new StartupNotice(
                StartupNotice.Type.LOAD_FAILED_BACKUP_FAILED, List.of(repair()), null, null));
    }

    @Test
    public void constructor_successfulBackupWithoutPath_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new StartupNotice(
                StartupNotice.Type.LOAD_FAILED_BACKED_UP, List.of(), null, null));
    }

    @Test
    public void constructor_nonBackupNoticeWithPath_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new StartupNotice(
                StartupNotice.Type.MISSING_DATA_FILE, List.of(), Path.of("backup.json"), null));
    }

    @Test
    public void constructor_nonLoadNoticeWithGuidance_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new StartupNotice(
                StartupNotice.Type.MISSING_DATA_FILE, List.of(), null, "Check permissions."));
    }

    @Test
    public void constructor_blankGuidance_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> StartupNotice.loadFailedBackupFailed(" "));
    }

    @Test
    public void constructor_detailsWithoutGuidance_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new StartupNotice(
                StartupNotice.Type.LOAD_FAILED_BACKUP_FAILED, List.of(), null, null, "Phone constraints."));
    }

    @Test
    public void constructor_blankDetails_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> StartupNotice.loadFailedBackupFailed(
                "Invalid contact data.", " "));
    }

    private static GuardianLinkRepair repair() {
        return new GuardianLinkRepair("Sam", UUID.randomUUID(), null,
                GuardianLinkRepair.Reason.MISSING_GUARDIAN);
    }
}
