package com.group4.library.repository.json;

import com.group4.library.model.Reader;
import com.group4.library.model.StudentReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrows;
class JsonReaderRepositoryTest {

    private JsonReaderRepository repository;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        repository = new JsonReaderRepository();
        File tempFile = new File(tempDir.toFile(), "readers-test.json");
        repository.setFilePathForTest(tempFile.getPath());
    }

    @Test
    void save_vaFindAll_traVeDungDuLieu() {
        Reader reader = new StudentReader("R001", "Nguyễn Văn A", "0912345678");

        repository.save(reader);
        List<Reader> all = repository.findAll();

        assertEquals(1, all.size());
        assertEquals("R001", all.get(0).getId());
    }

    @Test
    void findById_khongTonTai_traVeRong() {
        Optional<Reader> result = repository.findById("R999");

        assertTrue(result.isEmpty());
    }

    @Test
    void save_capNhatBanGhiCu_khongTaoTrung() {
        repository.save(new StudentReader("R001", "Nguyễn Văn A", "0912345678"));
        repository.save(new StudentReader("R001", "Nguyễn Văn A Sửa", "0999999999"));

        List<Reader> all = repository.findAll();

        assertEquals(1, all.size());
        assertEquals("Nguyễn Văn A Sửa", all.get(0).getName());
    }

    @Test
    void deleteById_xoaDungBanGhi() {
        repository.save(new StudentReader("R001", "Nguyễn Văn A", "0912345678"));

        repository.deleteById("R001");

        assertFalse(repository.existsById("R001"));
    }

    @Test
    void existsById_traVeDungKetQua() {
        repository.save(new StudentReader("R001", "Nguyễn Văn A", "0912345678"));

        assertTrue(repository.existsById("R001"));
        assertFalse(repository.existsById("R999"));
    }
    @Test
    void findAll_fileChuaTonTai_traVeRongVaTuTaoFile() {
        File file = new File(tempDir.toFile(), "chua-ton-tai.json");
        repository.setFilePathForTest(file.getPath());
        assertFalse(file.exists());

        List<Reader> all = repository.findAll();

        assertTrue(all.isEmpty());
        assertTrue(file.exists());
    }

    @Test
    void findAll_fileRong_traVeDanhSachRong() throws java.io.IOException {
        File file = new File(tempDir.toFile(), "rong.json");
        file.createNewFile();

        repository.setFilePathForTest(file.getPath());

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void findAll_fileSaiDinhDangJson_nemLoiRoRang() throws java.io.IOException {
        File file = new File(tempDir.toFile(), "sai-dinh-dang.json");
        java.nio.file.Files.writeString(file.toPath(), "{ khong phai mang JSON hop le ]");

        repository.setFilePathForTest(file.getPath());

        assertThrows(RuntimeException.class, () -> repository.findAll());
    }

    @Test
    void saveVaDoc_unicode_giuNguyenDauTiengViet() {
        repository.save(new StudentReader("R001", "Đặng Thị Ngọc Ánh", "0912345678"));

        JsonReaderRepository repositoryKhac = new JsonReaderRepository();
        repositoryKhac.setFilePathForTest(
                new File(tempDir.toFile(), "readers-test.json").getPath());

        Reader reader = repositoryKhac.findById("R001").orElseThrow();
        assertEquals("Đặng Thị Ngọc Ánh", reader.getName());
    }
}