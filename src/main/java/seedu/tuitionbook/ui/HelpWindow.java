package seedu.tuitionbook.ui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;
import seedu.tuitionbook.commons.core.LogsCenter;

/**
 * Controller for a help page
 */
public class HelpWindow extends UiPart<Stage> {

    public static final String USERGUIDE_URL = "https://ay2627s1-cs2103t-t16-2.github.io/tp/UserGuide.html";
    public static final String HELP_MESSAGE = "Full User Guide: " + USERGUIDE_URL;
    static final String OPEN_GUIDE_SUCCESS = "Opened the User Guide in your default browser.";
    static final String OPEN_GUIDE_FAILURE = "Could not open the User Guide. "
            + "Copy the URL and open it in your browser.";

    static final String COMMAND_SUMMARY = """
            add r/ROLE n/NAME p/PHONE [e/EMAIL] [a/ADDRESS] [t/TAG]...
              Add a student or guardian contact.

            list [r/ROLE]
              List every contact, or only students or guardians.

            view INDEX
              View a contact together with their recorded relationships.

            edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]...
              Edit one or more details of a displayed contact.

            link STUDENT_INDEX GUARDIAN_INDEX
              Link a displayed student to a displayed guardian.

            unlink STUDENT_INDEX
              Remove a displayed student's guardian link.

            find KEYWORD [MORE_KEYWORDS]
              Find contacts whose names contain any full keyword.

            delete INDEX
              Delete a displayed contact.

            clear
              Delete every contact.

            help
              Open or focus this Help window.

            exit
              Exit TuitionBook.
            """.strip();

    private static final Logger logger = LogsCenter.getLogger(HelpWindow.class);
    private static final String FXML = "HelpWindow.fxml";

    private final UrlOpener urlOpener;

    @FXML
    private Label browserStatus;

    @FXML
    private TextArea commandSummary;

    @FXML
    private Button copyButton;

    @FXML
    private Label helpMessage;

    /**
     * Creates a new HelpWindow.
     *
     * @param root Stage to use as the root of the HelpWindow.
     */
    public HelpWindow(Stage root) {
        this(root, HelpWindow::openInDefaultBrowser);
    }

    /**
     * Creates a new HelpWindow with the specified URL-opening behavior.
     */
    HelpWindow(Stage root, UrlOpener urlOpener) {
        super(FXML, root);
        this.urlOpener = Objects.requireNonNull(urlOpener);
        helpMessage.setText(HELP_MESSAGE);
        commandSummary.setText(COMMAND_SUMMARY);
    }

    /**
     * Creates a new HelpWindow.
     */
    public HelpWindow() {
        this(new Stage());
    }

    /**
     * Shows the help window.
     * @throws IllegalStateException
     *     <ul>
     *         <li>
     *             if this method is called on a thread other than the JavaFX Application Thread.
     *         </li>
     *         <li>
     *             if this method is called during animation or layout processing.
     *         </li>
     *         <li>
     *             if this method is called on the primary stage.
     *         </li>
     *         <li>
     *             if {@code dialogStage} is already showing.
     *         </li>
     *     </ul>
     */
    public void show() {
        logger.fine("Showing help page about the application.");
        getRoot().show();
        getRoot().centerOnScreen();
    }

    /**
     * Returns true if the help window is currently being shown.
     */
    public boolean isShowing() {
        return getRoot().isShowing();
    }

    /**
     * Hides the help window.
     */
    public void hide() {
        getRoot().hide();
    }

    /**
     * Focuses on the help window.
     */
    public void focus() {
        getRoot().setIconified(false);
        getRoot().toFront();
        getRoot().requestFocus();
    }

    /**
     * Opens the User Guide in the system default browser.
     */
    @FXML
    private void openUserGuide() {
        try {
            urlOpener.open(USERGUIDE_URL);
            showBrowserStatus(OPEN_GUIDE_SUCCESS, false);
        } catch (IOException | RuntimeException exception) {
            logger.log(Level.WARNING, "Failed to open the User Guide at " + USERGUIDE_URL, exception);
            showBrowserStatus(OPEN_GUIDE_FAILURE, true);
        }
    }

    private void showBrowserStatus(String message, boolean isError) {
        browserStatus.setText(message);
        browserStatus.setManaged(true);
        browserStatus.setVisible(true);
        browserStatus.getStyleClass().remove("error");
        if (isError) {
            browserStatus.getStyleClass().add("error");
        }
    }

    private static void openInDefaultBrowser(String url) throws IOException {
        if (!Desktop.isDesktopSupported()) {
            throw new IOException("Desktop integration is not supported");
        }

        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            throw new IOException("Opening browser URLs is not supported");
        }
        desktop.browse(URI.create(url));
    }

    /**
     * Copies the URL to the user guide to the clipboard.
     */
    @FXML
    private void copyUrl() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent url = new ClipboardContent();
        url.putString(USERGUIDE_URL);
        clipboard.setContent(url);
    }

    @FunctionalInterface
    interface UrlOpener {
        void open(String url) throws IOException;
    }
}
