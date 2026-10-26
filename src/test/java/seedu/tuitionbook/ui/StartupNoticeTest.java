package seedu.tuitionbook.ui;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/**
 * Tests validation of startup notice types and repair counts.
 */
public class StartupNoticeTest {

    @Test
    public void constructor_nullType_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new StartupNotice(null, 0, null, null));
    }

    @Test
    public void constructor_repairNoticeWithoutRepairs_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> StartupNotice.guardianLinksRepaired(0));
    }

    @Test
    public void constructor_negativeRepairCount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> StartupNotice.guardianLinksRepaired(-1));
    }

    @Test
    public void constructor_otherNoticeWithRepairCount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new StartupNotice(
                StartupNotice.Type.LOAD_FAILED_BACKUP_FAILED, 1, null, null));
    }

    @Test
    public void constructor_successfulBackupWithoutPath_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new StartupNotice(
                StartupNotice.Type.LOAD_FAILED_BACKED_UP, 0, null, null));
    }

    @Test
    public void constructor_nonBackupNoticeWithPath_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new StartupNotice(
                StartupNotice.Type.MISSING_DATA_FILE, 0, Path.of("backup.json"), null));
    }

    @Test
    public void constructor_nonLoadNoticeWithGuidance_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new StartupNotice(
                StartupNotice.Type.MISSING_DATA_FILE, 0, null, "Check permissions."));
    }

    @Test
    public void constructor_blankGuidance_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> StartupNotice.loadFailedBackupFailed(" "));
    }
}
