package com.group4.library.mapper;

import com.group4.library.dto.ReaderRequest;
import com.group4.library.dto.ReaderResponse;
import com.group4.library.exception.InvalidReaderTypeException;
import com.group4.library.model.LecturerReader;
import com.group4.library.model.PriorityStudentReader;
import com.group4.library.model.Reader;
import com.group4.library.model.StudentReader;

/** Chuyển đổi qua lại giữa DTO (ReaderRequest/ReaderResponse) và model Reader. */
public class ReaderMapper {

    private ReaderMapper() {
    }

    public static Reader toModel(String id, ReaderRequest request) {
        String name = request.getName();
        String phoneNumber = request.getPhoneNumber();

        return switch (request.getType()) {
            case "STUDENT" -> new StudentReader(id, name, phoneNumber);
            case "PRIORITY_STUDENT" -> new PriorityStudentReader(id, name, phoneNumber);
            case "LECTURER" -> new LecturerReader(id, name, phoneNumber);
            default -> throw new InvalidReaderTypeException(request.getType());
        };
    }

    public static ReaderResponse toResponse(Reader reader) {
        return new ReaderResponse(
                reader.getId(),
                reader.getName(),
                reader.getPhoneNumber(),
                reader.getType().name(),
                reader.getMaxBorrowLimit()
        );
    }
}