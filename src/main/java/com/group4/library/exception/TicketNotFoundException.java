package com.group4.library.exception;

public class TicketNotFoundException extends ResourceNotFoundException {
    public TicketNotFoundException(String message) { super(message); }
}
