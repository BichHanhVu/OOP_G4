package com.group4.library.policy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FinePolicyTest {
    @Test void sinhVienThuong() {
        assertEquals(15_000L, new NormalStudentFinePolicy().calculateFine(3));
    }
    @Test void sinhVienUuTien() {
        assertEquals(9_000L, new PriorityStudentFinePolicy().calculateFine(3));
    }
    @Test void giangVien() {
        assertEquals(6_000L, new LecturerFinePolicy().calculateFine(3));
    }
    @Test void khongTreKhongPhat() {
        assertEquals(0L, new NormalStudentFinePolicy().calculateFine(0));
        assertEquals(0L, new NormalStudentFinePolicy().calculateFine(-1));
    }
}
