package com.group4.library.policy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

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

    // ===================== Ranh giới số ngày quá hạn =====================
    // lateDays được tính bằng Math.max(0, DAYS.between(dueDate, actualReturnDate)),
    // nên các mốc cần chặt chẽ là: trước hạn (âm), đúng hạn (0) và trễ 1 ngày (1).

    private static Stream<Arguments> ranhGioiNgayQuaHan() {
        return Stream.of(
                // policy, lateDays, tienPhatMongMuon, moTa
                arguments(new NormalStudentFinePolicy(), -2L, 0L, "SV thường - trước hạn 2 ngày"),
                arguments(new NormalStudentFinePolicy(), -1L, 0L, "SV thường - trước hạn 1 ngày"),
                arguments(new NormalStudentFinePolicy(), 0L, 0L, "SV thường - đúng hạn"),
                arguments(new NormalStudentFinePolicy(), 1L, 5_000L, "SV thường - trễ 1 ngày"),
                arguments(new PriorityStudentFinePolicy(), -1L, 0L, "SV ưu tiên - trước hạn 1 ngày"),
                arguments(new PriorityStudentFinePolicy(), 0L, 0L, "SV ưu tiên - đúng hạn"),
                arguments(new PriorityStudentFinePolicy(), 1L, 3_000L, "SV ưu tiên - trễ 1 ngày"),
                arguments(new LecturerFinePolicy(), -1L, 0L, "Giảng viên - trước hạn 1 ngày"),
                arguments(new LecturerFinePolicy(), 0L, 0L, "Giảng viên - đúng hạn"),
                arguments(new LecturerFinePolicy(), 1L, 2_000L, "Giảng viên - trễ 1 ngày")
        );
    }

    @ParameterizedTest(name = "[{index}] {3}: lateDays={1} -> phạt={2}")
    @MethodSource("ranhGioiNgayQuaHan")
    void ranhGioiSoNgayQuaHan(FinePolicy policy, long lateDays, long tienPhatMongMuon, String moTa) {
        assertEquals(tienPhatMongMuon, policy.calculateFine(lateDays), moTa);
    }
}