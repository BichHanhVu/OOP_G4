package com.group4.library.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RenewalNotAllowedException extends RuntimeException {

    public RenewalNotAllowedException(String message) {
        super(message);
    }
}