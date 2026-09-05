package com.group4.library.mapper;

import com.group4.library.dto.ReaderRequest;
import com.group4.library.dto.ReaderResponse;
import com.group4.library.exception.InvalidReaderTypeException;
import com.group4.library.model.LecturerReader;
import com.group4.library.model.PriorityStudentReader;
import com.group4.library.model.Reader;
import com.group4.library.model.StudentReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReaderMapperTest {

    @Test
    void toModel_studentType_taoStudentReader() {
        Reader reader = ReaderMapper.toModel("R001", buildRequest("Nguyễn Văn A", "0912345678", "STUDENT"));

        assertInstanceOf(StudentReader.class, reader);
        assertEquals(3, reader.getMaxBorrowLimit());
    }

    @Test
    void toModel_priorityStudentType_taoPriorityStudentReader() {
        Reader reader = ReaderMapper.toModel("R002", buildRequest("Trần Thị B", "0987654321", "PRIORITY_STUDENT"));

        assertInstanceOf(PriorityStudentReader.class, reader);
        assertEquals(5, reader.getMaxBorrowLimit());
    }

    @Test
    void toModel_lecturerType_taoLecturerReader() {
        Reader reader = ReaderMapper.toModel("R003", buildRequest("Lê Văn C", "0901112223", "LECTURER"));

        assertInstanceOf(LecturerReader.class, reader);
        assertEquals(7, reader.getMaxBorrowLimit());
    }

    @Test
    void toModel_loaiKhongHopLe_nemLoi() {
        ReaderRequest request = buildRequest("Nguyễn Văn A", "0912345678", "UNKNOWN");

        assertThrows(InvalidReaderTypeException.class, () -> ReaderMapper.toModel("R001", request));
    }

    @Test
    void toResponse_anhXaDungTatCaTruong() {
        Reader reader = new StudentReader("R001", "Nguyễn Văn A", "0912345678");

        ReaderResponse response = ReaderMapper.toResponse(reader);

        assertEquals("R001", response.getId());
        assertEquals("Nguyễn Văn A", response.getName());
        assertEquals("0912345678", response.getPhoneNumber());
        assertEquals("STUDENT", response.getType());
        assertEquals(3, response.getMaxBorrowLimit());
    }

    private ReaderRequest buildRequest(String name, String phone, String type) {
        ReaderRequest request = new ReaderRequest();
        request.setName(name);
        request.setPhoneNumber(phone);
        request.setType(type);
        return request;
    }
}