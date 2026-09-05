package com.group4.library.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LecturerReaderTest {

    @Test
    void gioiHanMuonMacDinhLa7() {
        LecturerReader reader = new LecturerReader("R003", "Lê Văn C", "0901112223");

        assertEquals(7, reader.getMaxBorrowLimit());
        assertEquals(ReaderType.LECTURER, reader.getType());
    }

    @Test
    void luuDungThongTinCoBan() {
        LecturerReader reader = new LecturerReader("R003", "Lê Văn C", "0901112223");

        assertEquals("R003", reader.getId());
        assertEquals("Lê Văn C", reader.getName());
    }
}