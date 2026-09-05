package com.group4.library.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StudentReaderTest {

    @Test
    void gioiHanMuonMacDinhLa3() {
        StudentReader reader = new StudentReader("R001", "Nguyễn Văn A", "0912345678");

        assertEquals(3, reader.getMaxBorrowLimit());
        assertEquals(ReaderType.STUDENT, reader.getType());
    }

    @Test
    void luuDungThongTinCoBan() {
        StudentReader reader = new StudentReader("R001", "Nguyễn Văn A", "0912345678");

        assertEquals("R001", reader.getId());
        assertEquals("Nguyễn Văn A", reader.getName());
        assertEquals("0912345678", reader.getPhoneNumber());
    }
}