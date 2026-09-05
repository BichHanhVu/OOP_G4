package com.group4.library.exception;

/** Ném khi thêm/sửa bạn đọc với mã đã tồn tại. */
public class DuplicateReaderIdException extends BusinessException {
    public DuplicateReaderIdException(String id) {
        super("Mã bạn đọc đã tồn tại: " + id);
    }
}