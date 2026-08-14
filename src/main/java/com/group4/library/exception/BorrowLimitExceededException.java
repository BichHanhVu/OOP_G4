package com.group4.library.exception;

public class BorrowLimitExceededException extends BusinessException {
    public BorrowLimitExceededException(String message) {
        super(message);
    }
}