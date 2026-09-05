package com.group4.library.validation;

import com.group4.library.dto.ReaderRequest;
import com.group4.library.exception.EmptyReaderNameException;
import com.group4.library.exception.InvalidReaderTypeException;

import java.util.List;

public class ReaderValidator {

    private static final List<String> VALID_TYPES = List.of("STUDENT", "PRIORITY_STUDENT", "LECTURER");

    private ReaderValidator() {
    }

    public static ReaderRequest normalize(ReaderRequest request) {
        request.setName(normalizeName(request.getName()));
        request.setPhoneNumber(PhoneNumberValidator.normalize(request.getPhoneNumber()));
        return request;
    }

    public static void validate(ReaderRequest request) {
        validateName(request.getName());
        PhoneNumberValidator.validate(request.getPhoneNumber());
        validateType(request.getType());
    }

    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new EmptyReaderNameException();
        }
    }

    public static void validatePhoneNumber(String phoneNumber) {
        PhoneNumberValidator.validate(phoneNumber);
    }

    public static void validateType(String type) {
        if (type == null || !VALID_TYPES.contains(type)) {
            throw new InvalidReaderTypeException(type);
        }
    }

    private static String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        return name.trim().replaceAll("\\s+", " ");
    }
}