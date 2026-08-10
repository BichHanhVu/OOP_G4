package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonFileUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static <T> List<T> readListFromFile(String filePath, Class<T> clazz) {
        File file = new File(filePath);

        try {
            if (!file.exists()) {
                createFileIfNotExist(file);
                return new ArrayList<>();
            }

            if (file.length() == 0) {
                return new ArrayList<>();
            }

            CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(ArrayList.class, clazz);

            List<T> data = objectMapper.readValue(file, listType);
            return data != null ? data : new ArrayList<>();

        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file JSON " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static <T> void writeListToFile(String filePath, List<T> data) {
        File file = new File(filePath);

        try {
            createFileIfNotExist(file);

            objectMapper.writeValue(file, data != null ? data : new ArrayList<>());

        } catch (IOException e) {
            throw new RuntimeException("Không thể ghi", e);
        }
    }


    private static void createFileIfNotExist(File file) throws IOException {
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
    }
}