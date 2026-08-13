package com.group4.library.service;

import com.group4.library.dto.ReaderRequest;
import com.group4.library.dto.ReaderResponse;
import com.group4.library.exception.BusinessException;
import com.group4.library.exception.ResourceNotFoundException;
import com.group4.library.model.LecturerReader;
import com.group4.library.model.PriorityStudentReader;
import com.group4.library.model.Reader;
import com.group4.library.model.StudentReader;
import com.group4.library.repository.ReaderRepository;
import com.group4.library.utils.IdGenerator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReaderService {

    private final ReaderRepository readerRepository;

    public ReaderService(ReaderRepository readerRepository) {
        this.readerRepository = readerRepository;
    }

    public List<ReaderResponse> getAll(String keyword, String type) {
        return readerRepository.findAll().stream()
                .filter(r -> keyword == null || keyword.isBlank()
                        || r.getName().toLowerCase().contains(keyword.toLowerCase())
                        || r.getId().equalsIgnoreCase(keyword))
                .filter(r -> type == null || type.isBlank() || r.getType().name().equals(type))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ReaderResponse getById(String id) {
        return toResponse(findOrThrow(id));
    }

    public ReaderResponse create(ReaderRequest request) {
        validateRequest(request);

        String id = (request.getId() != null && !request.getId().isBlank())
                ? request.getId() : IdGenerator.nextReaderId();

        if (readerRepository.existsById(id)) {
            throw new BusinessException("Mã bạn đọc đã tồn tại: " + id);
        }

        Reader reader = buildReader(id, request);
        readerRepository.save(reader);
        return toResponse(reader);
    }

    public ReaderResponse update(String id, ReaderRequest request) {
        findOrThrow(id);
        validateRequest(request);

        Reader updated = buildReader(id, request);
        readerRepository.save(updated);
        return toResponse(updated);
    }

    public void delete(String id) {
        findOrThrow(id);
        readerRepository.deleteById(id);
    }

    private void validateRequest(ReaderRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessException("Họ tên không được để trống");
        }
        if (request.getPhoneNumber() == null || !request.getPhoneNumber().matches("\\d{9,11}")) {
            throw new BusinessException("Số điện thoại không hợp lệ");
        }
        if (request.getType() == null || !List.of("STUDENT", "PRIORITY_STUDENT", "LECTURER").contains(request.getType())) {
            throw new BusinessException("Loại bạn đọc không hợp lệ");
        }
    }

    private Reader buildReader(String id, ReaderRequest request) {
        return switch (request.getType()) {
            case "STUDENT" -> new StudentReader(id, request.getName(), request.getPhoneNumber());
            case "PRIORITY_STUDENT" -> new PriorityStudentReader(id, request.getName(), request.getPhoneNumber());
            case "LECTURER" -> new LecturerReader(id, request.getName(), request.getPhoneNumber());
            default -> throw new BusinessException("Loại bạn đọc không hợp lệ");
        };
    }

    private Reader findOrThrow(String id) {
        return readerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bạn đọc: " + id));
    }

    private ReaderResponse toResponse(Reader reader) {
        return new ReaderResponse(reader.getId(), reader.getName(), reader.getPhoneNumber(),
                reader.getType().name(), reader.getMaxBorrowLimit());
    }
}
