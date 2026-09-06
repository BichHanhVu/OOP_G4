package com.group4.library.exception;

import com.group4.library.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void readerNotFound_traVe404() {
        ResponseEntity<ErrorResponse> response = handler.handleReaderNotFound(new ReaderNotFoundException("R999"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Không tìm thấy bạn đọc: R999", response.getBody().getMessage());
    }

    @Test
    void duplicateReaderId_traVe400() {
        ResponseEntity<ErrorResponse> response = handler.handleDuplicateReaderId(new DuplicateReaderIdException("R001"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void emptyReaderName_traVe400() {
        ResponseEntity<ErrorResponse> response = handler.handleEmptyReaderName(new EmptyReaderNameException());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void invalidPhoneNumber_traVe400() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidPhoneNumber(new InvalidPhoneNumberException("abc"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void invalidReaderType_traVe400() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidReaderType(new InvalidReaderTypeException("UNKNOWN"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    @Test
    void loiNgoaiDuKien_traVe500VaErrorResponseNhatQuan() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(new RuntimeException("lỗi lạ không lường trước"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Đã có lỗi không mong muốn xảy ra", response.getBody().getMessage());
    }
}