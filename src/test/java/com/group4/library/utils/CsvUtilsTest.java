package com.group4.library.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvUtilsTest {

    @Test
    void parseLine_dongDonGian_tachDungTheoDauPhay() {
        List<String> values = CsvUtils.parseLine("R001,Nguyễn Văn A,0912345678,STUDENT");

        assertEquals(4, values.size());
        assertEquals("R001", values.get(0));
        assertEquals("Nguyễn Văn A", values.get(1));
        assertEquals("0912345678", values.get(2));
        assertEquals("STUDENT", values.get(3));
    }

    @Test
    void parseLine_truongCoDauPhayTrongNgoacKep_khongBiTachSai() {
        List<String> values = CsvUtils.parseLine("R001,\"Nguyễn, Văn A\",0912345678,STUDENT");

        assertEquals(4, values.size());
        assertEquals("Nguyễn, Văn A", values.get(1));
    }

    @Test
    void parseLine_dauNgoacKepKep_giaiMaThanhMotDauNgoacKep() {
        List<String> values = CsvUtils.parseLine("R001,\"Ten co \"\"biet danh\"\"\",0912345678,STUDENT");

        assertEquals("Ten co \"biet danh\"", values.get(1));
    }

    @Test
    void parseLine_truongRongOCuoiDong_vanTraVeChuoiRong() {
        List<String> values = CsvUtils.parseLine("R001,Nguyễn Văn A,0912345678,");

        assertEquals(4, values.size());
        assertEquals("", values.get(3));
    }

    @Test
    void parseLine_thieuCot_traVeSoPhanTuThieu() {
        List<String> values = CsvUtils.parseLine("R001,Nguyễn Văn A");

        assertEquals(2, values.size());
    }

    @Test
    void escapeField_giaTriRong_traVeChuoiRong() {
        assertEquals("", CsvUtils.escapeField(null));
    }

    @Test
    void escapeField_khongCoKyTuDacBiet_giuNguyen() {
        assertEquals("Nguyễn Văn A", CsvUtils.escapeField("Nguyễn Văn A"));
    }

    @Test
    void escapeField_coDauPhay_boQuanhNgoacKep() {
        assertEquals("\"Hà Nội, Việt Nam\"", CsvUtils.escapeField("Hà Nội, Việt Nam"));
    }

    @Test
    void escapeField_coDauNgoacKep_nhanDoiVaBoQuanhNgoac() {
        assertEquals("\"Ten \"\"biet danh\"\"\"", CsvUtils.escapeField("Ten \"biet danh\""));
    }

    @Test
    void escapeVaParse_roundTrip_giuNguyenDuLieuGoc() {
        String original = "Trần, \"Bảy\" Nguyễn";

        String escaped = CsvUtils.escapeField(original);
        List<String> parsed = CsvUtils.parseLine("R001," + escaped + ",0912345678,STUDENT");

        assertEquals(original, parsed.get(1));
    }
}