package seedu.tuitionbook.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.tuitionbook.commons.core.GuiSettings;
import seedu.tuitionbook.commons.core.LogsCenter;
import seedu.tuitionbook.logic.commands.AddCommand;
import seedu.tuitionbook.logic.commands.Command;
import seedu.tuitionbook.logic.commands.CommandResult;
import seedu.tuitionbook.logic.commands.exceptions.CommandException;
import seedu.tuitionbook.logic.parser.TuitionBookParser;
import seedu.tuitionbook.logic.parser.exceptions.ParseException;
import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.PersonListViewState;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.person.Person;
import seedu.tuitionbook.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

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
            } catch (IOException ioe) {
                restoreModel(originalTuitionBook, originalView);
                throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
            }
        }

        return command.canMutate() ? addViewFeedback(command, commandResult) : commandResult;
    }

    private void restoreModel(TuitionBook tuitionBook, PersonListViewState view) {
        model.setTuitionBook(tuitionBook);
        model.restorePersonListViewState(view);
    }

    /** Adds guidance after any required save completes, so a failed save never reports success. */
    private CommandResult addViewFeedback(Command command, CommandResult result) {
        PersonListViewState view = model.getPersonListViewState();
        if (command instanceof AddCommand) {
            Person added = model.getTuitionBook().getPersonList().getLast();
            if (!view.personIds().contains(added.getId())) {
                result = result.withAdditionalFeedback("The new contact is saved but does not match this working set. "
                        + "Use list to locate it, or refresh with: " + view.refreshCommand());
            }
        }
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
