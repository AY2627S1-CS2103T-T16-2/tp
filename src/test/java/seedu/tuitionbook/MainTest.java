package seedu.tuitionbook;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import seedu.tuitionbook.commons.exceptions.StoragePathException;

public class MainTest {

    @Test
    public void run_safePreflight_launchesJavaFxWithoutRequestingExit() {
        String[] args = {"first", "second"};
        AtomicReference<String[]> launchedArgs = new AtomicReference<>();
        AtomicBoolean exitRequested = new AtomicBoolean();

        Main.run(args, () -> { }, launchedArgs::set, exitCode -> exitRequested.set(true), System.err);

        assertArrayEquals(args, launchedArgs.get());
        assertFalse(exitRequested.get());
    }

    @Test
    public void run_unsafePreflight_reportsRefusalAndDoesNotLaunchJavaFx() {
        AtomicBoolean launched = new AtomicBoolean();
        AtomicInteger exitCode = new AtomicInteger(-1);
        ByteArrayOutputStream errorBytes = new ByteArrayOutputStream();
        StoragePathException unsafePath = new StoragePathException(
                Path.of("unsafe"), "Correct or remove the unsafe link.");

        Main.run(new String[0], () -> {
            throw unsafePath;
        }, args -> launched.set(true), exitCode::set, new PrintStream(errorBytes, true, StandardCharsets.UTF_8));

        assertFalse(launched.get());
        assertEquals(1, exitCode.get());
        String errorMessage = errorBytes.toString(StandardCharsets.UTF_8);
        assertTrue(errorMessage.contains("TuitionBook could not start."));
        assertTrue(errorMessage.contains(unsafePath.getMessage()));
    }
}
