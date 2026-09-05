package com.group4.library.exception;

/** Ném khi họ tên bạn đọc rỗng hoặc chỉ chứa khoảng trắng. */
public class EmptyReaderNameException extends BusinessException {
    public EmptyReaderNameException() {
        super("Họ tên không được để trống");
    }
}