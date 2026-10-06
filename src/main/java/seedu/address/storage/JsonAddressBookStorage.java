package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * A class to access AddressBook data stored as a JSON file on the hard disk.
 */
public class JsonAddressBookStorage {

    public static final String MESSAGE_WRITE_BLOCKED = "Saving is disabled because the existing data file at %s "
            + "could not be loaded. Correct the data file and restart or reload TuitionBook before saving.";

    private static final Logger logger = LogsCenter.getLogger(JsonAddressBookStorage.class);

    private Path filePath;
    private boolean isWriteBlocked;

    public JsonAddressBookStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getAddressBookFilePath() {
        return filePath;
    }

    /**
     * Returns AddressBook data as a {@link ReadOnlyAddressBook}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        return readAddressBook(filePath);
    }

    /**
     * Similar to {@link #readAddressBook()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);
        boolean isConfiguredFile = isConfiguredFilePath(filePath);

        try {
            Optional<JsonSerializableAddressBook> jsonAddressBook = JsonUtil.readJsonFile(
                    filePath, JsonSerializableAddressBook.class);
            if (jsonAddressBook.isEmpty()) {
                if (isConfiguredFile) {
                    isWriteBlocked = false;
                }
                return Optional.empty();
            }

            try {
                ReadOnlyAddressBook addressBook = jsonAddressBook.get().toModelType();
                if (isConfiguredFile) {
                    isWriteBlocked = false;
                }
                return Optional.of(addressBook);
            } catch (IllegalValueException ive) {
                logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
                throw new DataLoadingException(ive);
            }
        } catch (DataLoadingException dle) {
            if (isConfiguredFile) {
                isWriteBlocked = true;
            }
            throw dle;
        }
    }

    /**
     * Saves the given {@link ReadOnlyAddressBook} to the storage.
     * @param addressBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        saveAddressBook(addressBook, filePath);
    }

    /**
     * Similar to {@link #saveAddressBook(ReadOnlyAddressBook)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(filePath);

        if (isWriteBlocked && isConfiguredFilePath(filePath)) {
            throw new IOException(String.format(MESSAGE_WRITE_BLOCKED, this.filePath));
        }

        FileUtil.createIfMissing(filePath);
        JsonUtil.saveJsonFile(new JsonSerializableAddressBook(addressBook), filePath);
    }

    /**
     * Returns true if {@code filePath} identifies this storage instance's configured address-book file.
     */
    private boolean isConfiguredFilePath(Path filePath) {
        return this.filePath.toAbsolutePath().normalize().equals(filePath.toAbsolutePath().normalize());
    }

}
