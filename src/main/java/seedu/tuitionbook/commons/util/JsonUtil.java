package seedu.tuitionbook.commons.util;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import seedu.tuitionbook.commons.core.LogsCenter;
import seedu.tuitionbook.commons.exceptions.DataLoadingException;

/**
 * Converts a Java object instance to JSON and vice versa
 */
public class JsonUtil {

    private static final byte[] UTF_8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private static final Logger logger = LogsCenter.getLogger(JsonUtil.class);

    private static ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true)
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    static <T> void serializeObjectToJsonFile(Path jsonFile, T objectToSerialize) throws IOException {
        FileUtil.writeToFile(jsonFile, toJsonString(objectToSerialize));
    }

    static <T> T deserializeObjectFromJsonFile(Path jsonFile, Class<T> classOfObjectToDeserialize)
            throws IOException {
        byte[] contents = Files.readAllBytes(jsonFile);
        int jsonStart = hasUtf8Bom(contents) ? UTF_8_BOM.length : 0;
        String json = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(contents, jsonStart, contents.length - jsonStart))
                .toString();
        return fromJsonString(json, classOfObjectToDeserialize);
    }

    private static boolean hasUtf8Bom(byte[] contents) {
        return contents.length >= UTF_8_BOM.length
                && contents[0] == UTF_8_BOM[0]
                && contents[1] == UTF_8_BOM[1]
                && contents[2] == UTF_8_BOM[2];
    }

    /**
     * Returns the JSON object from the given file or {@code Optional.empty()} object if the file is not found.
     * If any values are missing from the file, default values will be used, as long as the file is a valid JSON file.
     *
     * @param filePath cannot be null.
     * @param classOfObjectToDeserialize JSON file has to correspond to the structure in the class given here.
     * @throws DataLoadingException if loading of the JSON file failed.
     */
    public static <T> Optional<T> readJsonFile(
            Path filePath, Class<T> classOfObjectToDeserialize) throws DataLoadingException {
        requireNonNull(filePath);

        T jsonFile;

        try {
            jsonFile = deserializeObjectFromJsonFile(filePath, classOfObjectToDeserialize);
        } catch (NoSuchFileException e) {
            return Optional.empty();
        } catch (IOException e) {
            logger.warning("Error reading from jsonFile file " + filePath + ": " + e);
            throw new DataLoadingException(e);
        }

        if (jsonFile == null) {
            throw new DataLoadingException(new IOException("JSON root value must not be null."));
        }

        return Optional.of(jsonFile);
    }

    /**
     * Saves the Json object to the specified file.
     * Overwrites existing file if it exists, creates a new file if it doesn't.
     * @param jsonFile cannot be null
     * @param filePath cannot be null
     * @throws IOException if there was an error during writing to the file
     */
    public static <T> void saveJsonFile(T jsonFile, Path filePath) throws IOException {
        requireNonNull(filePath);
        requireNonNull(jsonFile);

        serializeObjectToJsonFile(filePath, jsonFile);
    }


    /**
     * Converts a given string representation of JSON data to an instance of a class
     * @param <T> The generic type to create an instance of
     * @return The instance of T with the specified values in the JSON string
     */
    public static <T> T fromJsonString(String json, Class<T> instanceClass) throws IOException {
        try (JsonParser parser = objectMapper.getFactory().createParser(json)) {
            return fromJsonParser(parser, instanceClass);
        }
    }

    private static <T> T fromJsonParser(JsonParser parser, Class<T> instanceClass) throws IOException {
        T result = objectMapper.readValue(parser, instanceClass);
        if (parser.nextToken() != null) {
            throw new JsonParseException(parser, "Unexpected trailing JSON content.");
        }
        return result;
    }

    /**
     * Converts a given instance of a class into its JSON data string representation
     * @param instance The T object to be converted into the JSON string
     * @param <T> The generic type to create an instance of
     * @return JSON data representation of the given class instance, in string
     */
    public static <T> String toJsonString(T instance) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(instance);
    }

}
