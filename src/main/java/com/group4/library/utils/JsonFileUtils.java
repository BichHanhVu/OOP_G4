// utils/JsonFileUtils.java
package com.group4.library.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonFileUtils {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public static <T> List<T> readList(String filePath, Class<T> clazz) {
        try {
            File file = new File(filePath);

            if (!file.exists()) {
                // Trường hợp 1: file chưa tồn tại -> tạo thư mục cha + file chứa []
                createEmptyFile(file);
                return new ArrayList<>();
            }

            if (file.length() == 0) {
                // Trường hợp 2: file tồn tại nhưng rỗng
                return new ArrayList<>();
            }

            // Trường hợp 3: file có dữ liệu
            CollectionType listType = mapper.getTypeFactory()
                    .constructCollectionType(List.class, clazz);
            return mapper.readValue(file, listType);
        } catch (IOException e) {
            throw new RuntimeException("Không đọc được file: " + filePath, e);
        }
    }

    public static <T> void writeList(String filePath, List<T> data) {
        try {
            File file = new File(filePath);
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
        } catch (IOException e) {
            throw new RuntimeException("Không ghi được file: " + filePath, e);
        }
    }

    private static void createEmptyFile(File file) throws IOException {
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, new ArrayList<>());
    }
}