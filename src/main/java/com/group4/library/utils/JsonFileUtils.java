package com.group4.library.utils;

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

    public static <T> List<T> readList(String filePath, Class<T> clazz) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return new ArrayList<>();
        }

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
            System.err.println("Lỗi khi đọc file JSON (" + filePath + "): " + e.getMessage());
            return new ArrayList<>();
        }
    }


    public static <T> void writeList(String filePath, List<T> data) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Lỗi: Đường dẫn file JSON không được null hoặc rỗng!");
        }

        File file = new File(filePath);

        try {
            createFileIfNotExist(file);
            objectMapper.writeValue(file, data != null ? data : new ArrayList<>());
        } catch (IOException e) {
            throw new RuntimeException("Lỗi: Không thể ghi dữ liệu vào file JSON (" + filePath + ")", e);
        }
    }


    private static void createFileIfNotExist(File file) throws IOException {
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            boolean dirsCreated = file.getParentFile().mkdirs();
            if (!dirsCreated && !file.getParentFile().exists()) {
                throw new IOException("Không thể tạo thư mục chứa file: " + file.getParentFile().getAbsolutePath());
            }
        }

        if (!file.exists()) {
            boolean created = file.createNewFile();
            if (created) {
                objectMapper.writeValue(file, new ArrayList<>());
            }
        }
    }
}