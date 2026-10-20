package seedu.tuitionbook.ui;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
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
    static final String OPEN_GUIDE_PENDING = "Opening the User Guide...";

    static final String COMMAND_SUMMARY = """
            add r/ROLE n/NAME p/PHONE [e/EMAIL] [a/ADDRESS] [t/TAG]...
              Add a student or guardian contact.

            list [r/ROLE] [t/TAG]...
              List contacts matching the role and all tags, ignoring case. Bare list resets filters.

            view INDEX [v/]
              View a contact and linked contact summaries. Add v/ for full linked contact details.

            edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]...
              Edit one or more details of a displayed contact.

            tag INDEX t/TAG [t/TAG]...
              Add tags to a displayed contact, keeping its existing tags.

            untag INDEX t/TAG [t/TAG]...
              Remove specified tags from a displayed contact, keeping its other tags.

            link STUDENT_INDEX GUARDIAN_INDEX
              Link a displayed student to a displayed guardian.

            unlink STUDENT_INDEX
              Remove a displayed student's guardian link.

            find KEYWORD [MORE_KEYWORDS]...
              Find contacts whose names contain any full keyword.

            delete INDEX
              Delete a displayed contact.

            clear confirm
              Delete every contact, including contacts hidden by the current filter.

            help
              Open or focus this Help window.

            reload
              Replace contacts from the data file and clear the filter. Retry a blocked load.

            exit
              Exit TuitionBook.

            Quoted values (add/edit names and tags; list tags)
              Use double quotes around values containing field prefixes, e.g. t/"call p/next week".
              Inside quoted tags, use \\" for a literal quote and \\\\ for a literal backslash.
              Surrounding whitespace is trimmed, including inside quotes.
            """.strip();

    private static final Logger logger = LogsCenter.getLogger(HelpWindow.class);
    private static final String FXML = "HelpWindow.fxml";

    private final DesktopAdapter desktopAdapter;
    private final Executor browserExecutor;
    private boolean isOpeningUserGuide;

    @FXML
    private Label browserStatus;

    @FXML
    private TextArea commandSummary;

    @FXML
    private Button copyButton;

    @FXML
    private Label helpMessage;

    @FXML
    private Button openUserGuideButton;

    /**
     * Creates a new HelpWindow.
     *
     * @param root Stage to use as the root of the HelpWindow.
     */
    public HelpWindow(Stage root) {
        this(root, new DesktopAdapter());
    }

    /**
     * Creates a new HelpWindow with the specified desktop integration.
     */
    HelpWindow(Stage root, DesktopAdapter desktopAdapter) {
        this(root, desktopAdapter, HelpWindow::runInBackground);
    }

    /**
     * Creates a new HelpWindow with the specified desktop integration and background executor.
     */
    HelpWindow(Stage root, DesktopAdapter desktopAdapter, Executor browserExecutor) {
        super(FXML, root);
        this.desktopAdapter = Objects.requireNonNull(desktopAdapter);
        this.browserExecutor = Objects.requireNonNull(browserExecutor);
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
        if (isOpeningUserGuide) {
            return;
        }

        isOpeningUserGuide = true;
        openUserGuideButton.setDisable(true);
        showBrowserStatus(OPEN_GUIDE_PENDING, false);
        try {
            browserExecutor.execute(this::openUserGuideInBackground);
        } catch (RuntimeException exception) {
            showBrowserFailure(exception);
        }
    }

    private void openUserGuideInBackground() {
        try {
            openInDefaultBrowser(USERGUIDE_URL, desktopAdapter);
            Platform.runLater(() -> finishOpeningUserGuide(OPEN_GUIDE_SUCCESS, false));
        } catch (IOException | RuntimeException exception) {
            logger.log(Level.WARNING, "Failed to open the User Guide at " + USERGUIDE_URL, exception);
            Platform.runLater(() -> finishOpeningUserGuide(OPEN_GUIDE_FAILURE, true));
        }
    }

    private void showBrowserFailure(RuntimeException exception) {
        logger.log(Level.WARNING, "Failed to start browser opening for " + USERGUIDE_URL, exception);
        finishOpeningUserGuide(OPEN_GUIDE_FAILURE, true);
    }

    private void finishOpeningUserGuide(String message, boolean isError) {
        isOpeningUserGuide = false;
        openUserGuideButton.setDisable(false);
        showBrowserStatus(message, isError);
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

    private static void openInDefaultBrowser(String url, DesktopAdapter desktopAdapter) throws IOException {
        if (!desktopAdapter.isDesktopSupported()) {
            throw new IOException("Desktop integration is not supported");
        }

        if (!desktopAdapter.isBrowseSupported()) {
            throw new IOException("Opening browser URLs is not supported");
        }
        desktopAdapter.browse(URI.create(url));
    }

    private static void runInBackground(Runnable action) {
        Thread thread = new Thread(action, "user-guide-opener");
        thread.setDaemon(true);
        thread.start();
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
}
