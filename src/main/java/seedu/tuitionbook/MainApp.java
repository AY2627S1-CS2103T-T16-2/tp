package seedu.tuitionbook;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;
import seedu.tuitionbook.commons.core.LogsCenter;
import seedu.tuitionbook.commons.exceptions.DataLoadingException;
import seedu.tuitionbook.commons.util.StringUtil;
import seedu.tuitionbook.logic.Logic;
import seedu.tuitionbook.logic.LogicManager;
import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.ModelManager;
import seedu.tuitionbook.model.ReadOnlyTuitionBook;
import seedu.tuitionbook.model.ReadOnlyUserPrefs;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.UserPrefs;
import seedu.tuitionbook.model.util.SampleDataUtil;
import seedu.tuitionbook.storage.JsonTuitionBookStorage;
import seedu.tuitionbook.storage.JsonUserPrefsStorage;
import seedu.tuitionbook.storage.Storage;
import seedu.tuitionbook.storage.StorageManager;
import seedu.tuitionbook.storage.TuitionBookLoadResult;
import seedu.tuitionbook.ui.StartupNotice;
import seedu.tuitionbook.ui.Ui;
import seedu.tuitionbook.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final String VERSION = "V0.5.1";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = Paths.get("preferences.json");
    private static final Path TUITION_BOOK_FILE_PATH = Paths.get("data", "addressbook.json");

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing TuitionBook ]===========================");
        super.init();

        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(USER_PREFS_FILE_PATH);
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        JsonTuitionBookStorage tuitionBookStorage = new JsonTuitionBookStorage(TUITION_BOOK_FILE_PATH);
        storage = new StorageManager(tuitionBookStorage, userPrefsStorage);

        StartupState startupState = loadStartupState(storage, userPrefs);
        model = startupState.model();

        logic = new LogicManager(model, storage);

        ui = new UiManager(logic, storage.getTuitionBookFilePath(), startupState.notice());
    }

    /**
     * Returns the model and any startup notice for {@code storage}'s TuitionBook and {@code userPrefs}. <br>
     * The data from the sample TuitionBook will be used instead if {@code storage}'s TuitionBook is not found,
     * or an empty TuitionBook will be used instead if errors occur when reading {@code storage}'s TuitionBook.
     */
    static StartupState loadStartupState(Storage storage, ReadOnlyUserPrefs userPrefs) {
        logger.info("Using data file : " + storage.getTuitionBookFilePath());

        Optional<TuitionBookLoadResult> tuitionBookOptional;
        ReadOnlyTuitionBook initialData;
        Optional<StartupNotice> notice = Optional.empty();
        try {
            tuitionBookOptional = storage.readTuitionBookWithResult();
            if (tuitionBookOptional.isEmpty()) {
                logger.info("Creating a new data file " + storage.getTuitionBookFilePath()
                        + " populated with a sample TuitionBook.");
                initialData = SampleDataUtil.getSampleTuitionBook();
                notice = Optional.of(StartupNotice.missingDataFile());
            } else {
                TuitionBookLoadResult result = tuitionBookOptional.get();
                initialData = result.tuitionBook();
                if (result.repairedGuardianLinkCount() > 0) {
                    notice = Optional.of(StartupNotice.guardianLinksRepaired(result.repairedGuardianLinkCount()));
                }
            }
        } catch (DataLoadingException e) {
            logger.warning("Data file at " + storage.getTuitionBookFilePath() + " could not be loaded."
                    + " Will be starting with an empty TuitionBook.");
            initialData = new TuitionBook();
            notice = Optional.of(StartupNotice.loadFailed());
        }

        return new StartupState(new ModelManager(initialData, userPrefs), notice);
    }

    /** The initialized model and any contact-data notice to show at startup. */
    record StartupState(Model model, Optional<StartupNotice> notice) {
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save preference file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting TuitionBook " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping TuitionBook ] =============================");
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}
