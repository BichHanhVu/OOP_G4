package com.group4.library.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonFileUtilsTest {

    static class Sample {
        public String id;
        public String name;

        public Sample() {}
        public Sample(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @TempDir
    Path tempDir;

    @Test
    void readList_fileKhongTonTai_traVeDanhSachRong() {
        File file = new File(tempDir.toFile(), "khong-ton-tai.json");

        List<Sample> result = JsonFileUtils.readList(file.getPath(), Sample.class);

        assertTrue(result.isEmpty());
    }

    @Test
    void readList_fileRong_traVeDanhSachRong() throws IOException {
        File file = new File(tempDir.toFile(), "rong.json");
        Files.createFile(file.toPath());

        List<Sample> result = JsonFileUtils.readList(file.getPath(), Sample.class);

        assertTrue(result.isEmpty());
    }

    @Test
    void readList_jsonHong_nemRuntimeException() throws IOException {
        File file = new File(tempDir.toFile(), "hong.json");
        Files.writeString(file.toPath(), "{ khong phai mang JSON hop le");

        assertThrows(RuntimeException.class, () -> JsonFileUtils.readList(file.getPath(), Sample.class));
    }

    @Test
    void writeList_vaReadList_khopDuLieu() {
        File file = new File(tempDir.toFile(), "roundtrip.json");
        List<Sample> data = List.of(new Sample("S001", "Mẫu Một"), new Sample("S002", "Mẫu Hai"));

        JsonFileUtils.writeList(file.getPath(), data);
        List<Sample> result = JsonFileUtils.readList(file.getPath(), Sample.class);

        assertEquals(2, result.size());
        assertEquals("S001", result.get(0).id);
        assertEquals("Mẫu Hai", result.get(1).name);
    }

    @Test
    void writeList_thuMucChuaTonTai_tuDongTaoThuMuc() {
        File nestedFile = new File(tempDir.toFile(), "cap1/cap2/data.json");

        JsonFileUtils.writeList(nestedFile.getPath(), List.of(new Sample("S001", "Mẫu Một")));

        assertTrue(nestedFile.exists());
    }

    @Test
    void writeList_danhSachRong_taoFileVoiMangRong() {
        File file = new File(tempDir.toFile(), "rong-ghi.json");

        JsonFileUtils.writeList(file.getPath(), List.<Sample>of());
        List<Sample> result = JsonFileUtils.readList(file.getPath(), Sample.class);

        assertTrue(result.isEmpty());
    }

    @Test
    void writeList_ghiDe_khongConDuLieuCu() {
        File file = new File(tempDir.toFile(), "ghi-de.json");

        JsonFileUtils.writeList(file.getPath(), List.of(new Sample("S001", "Cũ")));
        JsonFileUtils.writeList(file.getPath(), List.of(new Sample("S002", "Mới")));

        List<Sample> result = JsonFileUtils.readList(file.getPath(), Sample.class);
        assertEquals(1, result.size());
        assertEquals("S002", result.get(0).id);
    }
}