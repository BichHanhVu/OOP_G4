package com.group4.library.exception;

public class BookNotFoundException
        extends ResourceNotFoundException {

    public BookNotFoundException(String message) {
        super(message);
    }
}