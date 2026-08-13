package com.group4.library.repository.json;

import com.group4.library.model.Reader;
import com.group4.library.model.StudentReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JsonReaderRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void docFile_chuaTonTai_traVeRongVaTuTaoFile() throws IOException {
        Path filePath = tempDir.resolve("readers.json");
        JsonReaderRepository repo = new JsonReaderRepository(filePath.toString());

        List<Reader> result = repo.findAll();

        assertTrue(result.isEmpty());
        assertTrue(Files.exists(filePath));
        assertEquals("[ ]", Files.readString(filePath).replaceAll("\\s+", " ").trim());
    }

    @Test
    void docFile_rong_traVeDanhSachRong() throws IOException {
        Path filePath = tempDir.resolve("readers.json");
        Files.writeString(filePath, "");
        JsonReaderRepository repo = new JsonReaderRepository(filePath.toString());

        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    void ghiRoiDoc_layDungDuLieuVuaGhi() {
        Path filePath = tempDir.resolve("readers.json");
        JsonReaderRepository repo = new JsonReaderRepository(filePath.toString());

        Reader reader = new StudentReader("R001", "Nguyễn Văn A", "0912345678");
        repo.save(reader);

        Optional<Reader> found = repo.findById("r001"); // kiểm tra luôn không phân biệt hoa/thường
        assertTrue(found.isPresent());
        assertEquals("Nguyễn Văn A", found.get().getName());
    }
}
