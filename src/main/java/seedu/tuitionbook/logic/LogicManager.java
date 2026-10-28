package seedu.tuitionbook.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.tuitionbook.commons.core.GuiSettings;
import seedu.tuitionbook.commons.core.LogsCenter;
import seedu.tuitionbook.logic.commands.Command;
import seedu.tuitionbook.logic.commands.CommandResult;
import seedu.tuitionbook.logic.commands.exceptions.CommandException;
import seedu.tuitionbook.logic.parser.TuitionBookParser;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;
import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.PersonListViewState;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.storage.SaveBlockedException;
import seedu.tuitionbook.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String MESSAGE_SAVE_FAILED = "Could not save your changes, so the command was undone. ";

    public static final String FILE_OPS_ERROR_MESSAGE = MESSAGE_SAVE_FAILED
            + "Check that the data folder can be written to and has free space, then try again.";

    public static final String FILE_OPS_BLOCKED_ERROR_FORMAT = MESSAGE_SAVE_FAILED + "%s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT = MESSAGE_SAVE_FAILED
            + "TuitionBook does not have permission to save to %s. Check that you can save files in that folder, "
            + "then try again.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final TuitionBookParser tuitionBookParser;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        tuitionBookParser = new TuitionBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        CommandResult commandResult;
        Command command = tuitionBookParser.parseCommand(commandText);
        TuitionBook originalTuitionBook = null;
        PersonListViewState originalView = null;
        if (command.canMutate()) {
            originalTuitionBook = new TuitionBook(model.getTuitionBook());
            originalView = model.getPersonListViewState();
        }
        commandResult = command.execute(model, storage);

        if (command.requiresSave()) {
            try {
                storage.saveTuitionBook(model.getTuitionBook());
            } catch (AccessDeniedException e) {
                restoreModel(originalTuitionBook, originalView);
                throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
            } catch (SaveBlockedException sbe) {
                restoreModel(originalTuitionBook, originalView);
                throw new CommandException(String.format(FILE_OPS_BLOCKED_ERROR_FORMAT, sbe.getMessage()), sbe);
            } catch (IOException ioe) {
                logger.warning("Could not save data: " + ioe);
                restoreModel(originalTuitionBook, originalView);
                throw new CommandException(FILE_OPS_ERROR_MESSAGE, ioe);
            }
        }

        return command.canMutate() ? addViewFeedback(commandResult) : commandResult;
    }

    private void restoreModel(TuitionBook tuitionBook, PersonListViewState view) {
        model.setTuitionBook(tuitionBook);
        model.restorePersonListViewState(view);
    }

    /** Adds guidance after any required save completes, so a failed save never reports success. */
    private CommandResult addViewFeedback(CommandResult result) {
        PersonListViewState view = model.getPersonListViewState();
        long retainedCount = model.getFilteredPersonList().stream().filter(view.predicate().negate()).count();
        if (retainedCount > 0) {
            result = result.withAdditionalFeedback(retainedCount + " displayed contact(s) no longer match the query "
                    + "and remain visible. Refresh with: " + view.refreshCommand());
        }
        return result;
    }

    @Override
    public PersonListViewState getPersonListViewState() {
        return model.getPersonListViewState();
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return model.getTuitionBook().getPersonList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
