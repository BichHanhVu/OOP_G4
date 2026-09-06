package com.group4.library.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdGeneratorTest {

    private static final Pattern ID_PATTERN = Pattern.compile("^R\\d{3,}$");

    @Test
    void danhSachRong_traVeR001() {
        String id = IdGenerator.nextReaderId(List.of());

        assertEquals("R001", id);
        assertTrue(ID_PATTERN.matcher(id).matches());
    }

    @Test
    void danhSachCoSan_traVeMaTiepTheo() {
        List<String> existingIds = List.of("R001", "R002", "R003");

        String id = IdGenerator.nextReaderId(existingIds);

        assertEquals("R004", id);
    }

    @Test
    void danhSachKhongTheoThuTu_vanTinhDungMaLonNhat() {
        List<String> existingIds = List.of("R005", "R001", "R010", "R003");

        String id = IdGenerator.nextReaderId(existingIds);

        assertEquals("R011", id);
    }

    @Test
    void danhSachChuaMaKhongDungDinhDang_boQuaKhiTinh() {
        List<String> existingIds = new ArrayList<>();
        existingIds.add("R001");
        existingIds.add("KHONG_HOP_LE");
        existingIds.add(null);

        String id = IdGenerator.nextReaderId(existingIds);

        assertEquals("R002", id);
    }

    @Test
    void ketQuaLuonDungDinhDangRxxx() {
        String id = IdGenerator.nextReaderId(List.of("R099"));

        assertTrue(ID_PATTERN.matcher(id).matches());
        assertEquals("R100", id);
    }
}