package com.group4.library.exception;

/** Ném khi không tìm thấy phiếu trả sách theo mã. Kế thừa ResourceNotFoundException để trả HTTP 404. */
public class ReturnRecordNotFoundException extends ResourceNotFoundException {
    public ReturnRecordNotFoundException(String message) {
        super(message);
    }
}