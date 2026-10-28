package seedu.tuitionbook.storage;

import java.io.IOException;

/**
 * Signals that saving was refused to protect a data file that could not be loaded.
 * Its message is written for users and can be shown as is.
 */
public class SaveBlockedException extends IOException {
    public SaveBlockedException(String message) {
        super(message);
    }
}
