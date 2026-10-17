package seedu.tuitionbook.ui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * Small boundary around the platform-specific {@link Desktop} integration.
 */
final class DesktopAdapter {

    private final BooleanSupplier desktopSupported;
    private final BooleanSupplier browseSupported;
    private final Browser browser;

    /** Creates an adapter for the current operating system desktop. */
    DesktopAdapter() {
        this(Desktop::isDesktopSupported,
                DesktopAdapter::isSystemBrowseSupported,
                DesktopAdapter::browseWithSystemDesktop);
    }

    /** Creates an adapter backed by the supplied platform operations. */
    DesktopAdapter(BooleanSupplier desktopSupported, BooleanSupplier browseSupported, Browser browser) {
        this.desktopSupported = Objects.requireNonNull(desktopSupported);
        this.browseSupported = Objects.requireNonNull(browseSupported);
        this.browser = Objects.requireNonNull(browser);
    }

    boolean isDesktopSupported() {
        return desktopSupported.getAsBoolean();
    }

    boolean isBrowseSupported() {
        return browseSupported.getAsBoolean();
    }

    void browse(URI uri) throws IOException {
        browser.browse(uri);
    }

    private static boolean isSystemBrowseSupported() {
        return Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
    }

    private static void browseWithSystemDesktop(URI uri) throws IOException {
        Desktop.getDesktop().browse(uri);
    }

    @FunctionalInterface
    interface Browser {
        void browse(URI uri) throws IOException;
    }
}
