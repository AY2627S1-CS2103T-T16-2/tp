package seedu.tuitionbook;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.tuitionbook.logic.Logic;
import seedu.tuitionbook.logic.LogicManager;
import seedu.tuitionbook.logic.commands.CommandResult;
import seedu.tuitionbook.model.Model;
import seedu.tuitionbook.model.TuitionBook;
import seedu.tuitionbook.model.UserPrefs;
import seedu.tuitionbook.storage.JsonTuitionBookStorage;
import seedu.tuitionbook.storage.JsonUserPrefsStorage;
import seedu.tuitionbook.storage.Storage;
import seedu.tuitionbook.storage.StorageManager;
import seedu.tuitionbook.testutil.PersonBuilder;

public class MainAppTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    public void initModelManager_missingDataFile_startsEmpty() {
        Path dataFile = temporaryFolder.resolve("missing.json");
        Storage storage = createStorage(dataFile);

        Model model = new MainApp().initModelManager(storage, new UserPrefs());

        assertTrue(Files.notExists(dataFile));
        assertTrue(model.getTuitionBook().getPersonList().isEmpty());
    }

    @Test
    public void initModelManager_existingValidEmptyDataFile_staysEmpty() throws Exception {
        Path dataFile = temporaryFolder.resolve("empty.json");
        Storage storage = createStorage(dataFile);
        storage.saveTuitionBook(new TuitionBook());

        Model model = new MainApp().initModelManager(storage, new UserPrefs());

        assertTrue(Files.exists(dataFile));
        assertTrue(model.getTuitionBook().getPersonList().isEmpty());
    }

    @Test
    public void clearThenRestart_existingEmptyDataFileDoesNotRestoreSamples() throws Exception {
        Path dataFile = temporaryFolder.resolve("cleared.json");
        Storage storage = createStorage(dataFile);
        TuitionBook tuitionBook = new TuitionBook();
        tuitionBook.addPerson(new PersonBuilder().build());
        storage.saveTuitionBook(tuitionBook);
        MainApp mainApp = new MainApp();
        Model model = mainApp.initModelManager(storage, new UserPrefs());
        assertTrue(model.getTuitionBook().getPersonList().contains(tuitionBook.getPersonList().get(0)));
        Logic logic = new LogicManager(model, storage);

        logic.execute("clear");
        Model restartedModel = mainApp.initModelManager(storage, new UserPrefs());

        assertTrue(Files.exists(dataFile));
        assertTrue(restartedModel.getTuitionBook().getPersonList().isEmpty());
    }

    @Test
    public void documentedViewExamples_fromEmptyStartup_succeed() throws Exception {
        Storage storage = createStorage(temporaryFolder.resolve("examples.json"));
        Model model = new MainApp().initModelManager(storage, new UserPrefs());
        Logic logic = new LogicManager(model, storage);
        logic.execute("add r/student n/Alex Yeoh p/87438807");
        logic.execute("add r/guardian n/Bernice Yu p/99272758");
        logic.execute("link 1 2");
        logic.execute("add r/student n/Charlotte Oliveiro p/93210283");
        logic.execute("add r/guardian n/David Li p/91031282");

        assertViewContains(logic.execute("view 1"), "Alex Yeoh", "Guardian details:", "Bernice Yu");
        assertViewContains(logic.execute("view 2"), "Bernice Yu", "Linked students:", "Alex Yeoh");
        assertViewContains(logic.execute("view 3"), "Charlotte Oliveiro", "No guardian linked.");
        assertViewContains(logic.execute("view 4"), "David Li", "No students linked.");
        logic.execute("list r/student");
        assertViewContains(logic.execute("view 1"), "Alex Yeoh", "Guardian details:", "Bernice Yu");
    }

    private void assertViewContains(CommandResult result, String... expectedFragments) {
        String details = result.getDetailsToShow().orElseThrow();
        for (String expectedFragment : expectedFragments) {
            assertTrue(details.contains(expectedFragment), () ->
                    "Missing <" + expectedFragment + "> in:\n" + details);
        }
    }

    private Storage createStorage(Path dataFile) {
        JsonTuitionBookStorage tuitionBookStorage = new JsonTuitionBookStorage(dataFile);
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"));
        return new StorageManager(tuitionBookStorage, userPrefsStorage);
    }
}
