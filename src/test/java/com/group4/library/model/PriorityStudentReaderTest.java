package com.group4.library.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriorityStudentReaderTest {

    @Test
    void gioiHanMuonMacDinhLa5() {
        PriorityStudentReader reader = new PriorityStudentReader("R002", "Trần Thị B", "0987654321");

        assertEquals(5, reader.getMaxBorrowLimit());
        assertEquals(ReaderType.PRIORITY_STUDENT, reader.getType());
    }

    @Test
    void luuDungThongTinCoBan() {
        PriorityStudentReader reader = new PriorityStudentReader("R002", "Trần Thị B", "0987654321");

        assertEquals("R002", reader.getId());
        assertEquals("Trần Thị B", reader.getName());
    }
}