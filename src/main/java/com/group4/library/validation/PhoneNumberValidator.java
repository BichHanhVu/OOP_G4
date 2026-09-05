package com.group4.library.validation;

import com.group4.library.exception.InvalidPhoneNumberException;

public class PhoneNumberValidator {

    private static final String PHONE_PATTERN = "\\d{9,11}";

    private PhoneNumberValidator() {
    }

    public static String normalize(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        return phoneNumber.trim();
    }

    public static void validate(String phoneNumber) {
        if (phoneNumber == null || !phoneNumber.matches(PHONE_PATTERN)) {
            throw new InvalidPhoneNumberException(phoneNumber);
        }
    }
}