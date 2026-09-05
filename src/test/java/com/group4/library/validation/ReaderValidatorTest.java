package com.group4.library.validation;

import com.group4.library.dto.ReaderRequest;
import com.group4.library.exception.EmptyReaderNameException;
import com.group4.library.exception.InvalidPhoneNumberException;
import com.group4.library.exception.InvalidReaderTypeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReaderValidatorTest {

    @Test
    void duLieuHopLe_khongNemLoi() {
        ReaderRequest request = buildRequest("Nguyễn Văn A", "0912345678", "STUDENT");

        assertDoesNotThrow(() -> ReaderValidator.validate(request));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void ten_rongHoacKhoangTrang_nemEmptyReaderNameException(String ten) {
        ReaderRequest request = buildRequest(ten, "0912345678", "STUDENT");

        assertThrows(EmptyReaderNameException.class, () -> ReaderValidator.validate(request));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Nguyễn Văn A",
            "Lê Thị Bích Hạnh",
            "Đặng Xuân Bách",
            "A",
            "Trần Văn Tiệp - Nhóm 04"
    })
    void ten_hopLe_khongNemLoi(String ten) {
        assertDoesNotThrow(() -> ReaderValidator.validateName(ten));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "123",
            "12345678",
            "123456789012",
            "abc123456",
            "091-234-5678",
            "0912 345 678",
            "091234567a",
            "+84912345678"
    })
    void soDienThoai_saiDinhDang_nemInvalidPhoneNumberException(String soDienThoai) {
        ReaderRequest request = buildRequest("Nguyễn Văn A", soDienThoai, "STUDENT");

        assertThrows(InvalidPhoneNumberException.class, () -> ReaderValidator.validate(request));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "091234567",
            "0912345678",
            "09123456789",
            "0987654321"
    })
    void soDienThoai_dungDinhDang9Den11So_khongNemLoi(String soDienThoai) {
        assertDoesNotThrow(() -> ReaderValidator.validatePhoneNumber(soDienThoai));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "student",
            "Student",
            "UNKNOWN",
            "STUDENT ",
            " STUDENT",
            "PRIORITY",
            "TEACHER",
            "GIANG_VIEN"
    })
    void loaiBanDoc_khongHopLe_nemInvalidReaderTypeException(String loai) {
        ReaderRequest request = buildRequest("Nguyễn Văn A", "0912345678", loai);

        assertThrows(InvalidReaderTypeException.class, () -> ReaderValidator.validate(request));
    }

    @ParameterizedTest
    @CsvSource({
            "STUDENT",
            "PRIORITY_STUDENT",
            "LECTURER"
    })
    void loaiBanDoc_hopLe_khongNemLoi(String loai) {
        assertDoesNotThrow(() -> ReaderValidator.validateType(loai));
    }

    private ReaderRequest buildRequest(String name, String phone, String type) {
        ReaderRequest request = new ReaderRequest();
        request.setName(name);
        request.setPhoneNumber(phone);
        request.setType(type);
        return request;
    }
}