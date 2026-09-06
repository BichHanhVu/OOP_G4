package com.group4.library.exception;

/** Ném khi số điện thoại không đúng định dạng 9-11 chữ số. */
public class InvalidPhoneNumberException extends BusinessException {
    public InvalidPhoneNumberException(String phoneNumber) {
        super("Số điện thoại không hợp lệ: " + phoneNumber);
    }
}