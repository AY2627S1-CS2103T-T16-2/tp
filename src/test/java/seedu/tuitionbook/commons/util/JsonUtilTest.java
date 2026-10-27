package seedu.tuitionbook.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.tuitionbook.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.core.JsonParseException;

import seedu.tuitionbook.commons.exceptions.DataLoadingException;
import seedu.tuitionbook.testutil.SerializableTestClass;
import seedu.tuitionbook.testutil.TestUtil;

/**
 * Tests JSON Read and Write
 */
public class JsonUtilTest {

    private static final Path SERIALIZATION_FILE = TestUtil.getFilePathInSandboxFolder("serialize.json");

    @TempDir
    public Path temporaryFolder;

    @Test
    public void readJsonFile_missingFile_returnsEmpty() throws Exception {
        assertTrue(JsonUtil.readJsonFile(temporaryFolder.resolve("missing.json"),
                SerializableTestClass.class).isEmpty());
    }

    @Test
    public void readJsonFile_unreadableAsFile_throwsLoadingException() {
        assertThrows(DataLoadingException.class, () ->
                JsonUtil.readJsonFile(temporaryFolder, SerializableTestClass.class));
    }


    @Test
    public void serializeObjectToJsonFile_noExceptionThrown() throws IOException {
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.serializeObjectToJsonFile(SERIALIZATION_FILE, serializableTestClass);

        assertEquals(FileUtil.readFromFile(SERIALIZATION_FILE), SerializableTestClass.JSON_STRING_REPRESENTATION);
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = JsonUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertEquals(serializableTestClass.getName(), SerializableTestClass.getNameTestValue());
        assertEquals(serializableTestClass.getListOfLocalDateTimes(), SerializableTestClass.getListTestValues());
        assertEquals(serializableTestClass.getMapOfIntegerToString(), SerializableTestClass.getHashMapTestValues());
    }

    @Test
    public void deserializeObjectFromJsonFile_shortJsonWithoutBom_noExceptionThrown() throws IOException {
        Path filePath = temporaryFolder.resolve("short.json");
        Files.writeString(filePath, "{}");

        assertNotNull(JsonUtil.deserializeObjectFromJsonFile(filePath, SerializableTestClass.class));
    }

    @Test
    public void deserializeObjectFromJsonFile_nearBomPrefixes_throwsIoException() throws IOException {
        Path differentSecondByte = temporaryFolder.resolve("different-second-byte.json");
        Path differentThirdByte = temporaryFolder.resolve("different-third-byte.json");
        Files.write(differentSecondByte, new byte[]{(byte) 0xEF, 0, 0});
        Files.write(differentThirdByte, new byte[]{(byte) 0xEF, (byte) 0xBB, 0});

        assertThrows(IOException.class, () ->
                JsonUtil.deserializeObjectFromJsonFile(differentSecondByte, SerializableTestClass.class));
        assertThrows(IOException.class, () ->
                JsonUtil.deserializeObjectFromJsonFile(differentThirdByte, SerializableTestClass.class));
    }

    @Test
    public void fromJsonString_validJson_returnsObject() throws IOException {
        SerializableTestClass result = JsonUtil.fromJsonString(
                SerializableTestClass.JSON_STRING_REPRESENTATION, SerializableTestClass.class);

        assertEquals(SerializableTestClass.getNameTestValue(), result.getName());
        assertEquals(SerializableTestClass.getListTestValues(), result.getListOfLocalDateTimes());
        assertEquals(SerializableTestClass.getHashMapTestValues(), result.getMapOfIntegerToString());
    }

    @Test
    public void fromJsonString_trailingJson_throwsIoException() {
        String jsonWithTrailingObject = SerializableTestClass.JSON_STRING_REPRESENTATION + " {}";

        assertThrows(IOException.class, () ->
                JsonUtil.fromJsonString(jsonWithTrailingObject, SerializableTestClass.class));
    }

    @Test
    public void fromJsonString_trailingJsonValue_throwsJsonParseException() {
        for (String trailingValue : new String[] {" {}", " []", " 1"}) {
            assertThrows(JsonParseException.class, () -> JsonUtil.fromJsonString(
                    SerializableTestClass.JSON_STRING_REPRESENTATION + trailingValue, SerializableTestClass.class));
        }
    }

    //TODO: @Test jsonUtil_writeThenReadObjectToJson_correctObject()
}
