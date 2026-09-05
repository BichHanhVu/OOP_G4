package com.group4.library.exception;

/** Ném khi không tìm thấy bạn đọc theo mã. Kế thừa ResourceNotFoundException để trả HTTP 404. */
public class ReaderNotFoundException extends ResourceNotFoundException {
    public ReaderNotFoundException(String id) {
        super("Không tìm thấy bạn đọc: " + id);
    }
}