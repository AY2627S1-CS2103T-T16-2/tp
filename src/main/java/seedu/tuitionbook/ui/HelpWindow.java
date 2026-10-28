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
            How to read this summary
              Replace words in CAPITALS with your own details, e.g. n/NAME becomes n/John Doe.
              Parts in [square brackets] are optional. Parts followed by ... can be repeated.
              INDEX is the number shown beside a contact in the current list.

            Typing commands
              Command words can be typed in any letter case: LIST works like list.
              Prefixes such as n/ and t/, and the word confirm, must be lowercase.
              Type one command at a time, then press Enter.
              help, exit and reload take nothing after the command word.
              Editing, tagging and linking keep each contact at the same number in the list.
              Deleting moves the contacts below up by one, so check the list before your next command.

            add r/ROLE n/NAME p/PHONE [e/EMAIL] [a/ADDRESS] [t/TAG]...
              Add a student or guardian.

            list [r/ROLE] [t/TAG]...
              Show everyone, or only contacts with this role and all of these tags.

            view INDEX [v/]
              Show a contact's details. Add v/ to also show their linked contacts' full details.

            edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]...
              Change a contact's details. Giving t/ replaces all of their tags.

            tag INDEX t/TAG [t/TAG]...
              Add tags to a contact, keeping their existing tags.

            untag INDEX t/TAG [t/TAG]...
              Remove these tags from a contact, keeping their other tags.

            link STUDENT_INDEX GUARDIAN_INDEX
              Link a student to their guardian.

            unlink STUDENT_INDEX
              Remove a student's guardian link.

            find KEYWORD [MORE_KEYWORDS]...
              Find contacts whose names contain any of the keywords, even as part of a name.

            delete INDEX
              Delete a contact.

            clear confirm
              Delete every contact, including any hidden by the current filter.

            help
              Open this Help window.

            reload
              Load your contacts from the data file again, replacing the ones in the app.

            exit
              Close TuitionBook.

            Using double quotes
              If a name, address or tag contains text like a/ or s/, wrap the whole value in double quotes:
              n/"Muhammad s/o Ali", a/"20 Main Road, c/o Rina a/p Kaur" or t/"call p/next week".
              This works in add, edit, list, tag and untag. The quotes are not saved.
              Inside quotes, type \\" for a double quote and \\\\ for a backslash.
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
