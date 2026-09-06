package com.group4.library.exception;

/** Ném khi loại bạn đọc không thuộc STUDENT, PRIORITY_STUDENT, LECTURER. */
public class InvalidReaderTypeException extends BusinessException {
    public InvalidReaderTypeException(String type) {
        super("Loại bạn đọc không hợp lệ: " + type);
    }
}