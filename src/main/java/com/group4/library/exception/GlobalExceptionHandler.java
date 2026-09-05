package com.group4.library.exception;

import com.group4.library.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ReaderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReaderNotFound(ReaderNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DuplicateReaderIdException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateReaderId(DuplicateReaderIdException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(EmptyReaderNameException.class)
    public ResponseEntity<ErrorResponse> handleEmptyReaderName(EmptyReaderNameException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidPhoneNumberException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPhoneNumber(InvalidPhoneNumberException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidReaderTypeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidReaderType(InvalidReaderTypeException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({BusinessException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Lỗi không mong muốn", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Đã có lỗi không mong muốn xảy ra");
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        log.warn("Trả lỗi {} - {}", status.value(), message);
        return ResponseEntity.status(status).body(new ErrorResponse(message));
    }
}
