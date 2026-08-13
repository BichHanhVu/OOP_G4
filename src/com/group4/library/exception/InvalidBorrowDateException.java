package com.group4.library.exception;

public class InvalidBorrowDateException extends RuntimeException {
    public InvalidBorrowDateException(String message) {
        super(message);
    }
}
