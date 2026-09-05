package com.group4.library.service;

import com.group4.library.dto.PagedReaderResponse;
import com.group4.library.dto.ReaderRequest;
import com.group4.library.dto.ReaderResponse;
import com.group4.library.dto.ReaderSearchRequest;
import com.group4.library.exception.BusinessException;
import com.group4.library.exception.DuplicateReaderIdException;
import com.group4.library.exception.ReaderNotFoundException;
import com.group4.library.mapper.ReaderMapper;
import com.group4.library.model.Reader;
import com.group4.library.model.TicketStatus;
import com.group4.library.repository.BorrowTicketRepository;
import com.group4.library.repository.ReaderRepository;
import com.group4.library.utils.IdGenerator;
import com.group4.library.validation.ReaderValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReaderService {

    private static final Logger log = LoggerFactory.getLogger(ReaderService.class);

    private final ReaderRepository readerRepository;
    private final BorrowTicketRepository borrowTicketRepository;

    public ReaderService(
            ReaderRepository readerRepository,
            BorrowTicketRepository borrowTicketRepository
    ) {
        this.readerRepository = readerRepository;
        this.borrowTicketRepository = borrowTicketRepository;
    }

    public PagedReaderResponse<ReaderResponse> search(ReaderSearchRequest request) {
        List<Reader> filtered = readerRepository.findAll().stream()
                .filter(reader -> matchesKeyword(reader, request.getKeyword()))
                .filter(reader -> matchesType(reader, request.getType()))
                .sorted(buildComparator(request.getSortBy(), request.getSortDirection()))
                .collect(Collectors.toList());

        long totalElements = filtered.size();

        List<ReaderResponse> pageContent = filtered.stream()
                .skip((long) request.getPage() * request.getSize())
                .limit(request.getSize())
                .map(ReaderMapper::toResponse)
                .collect(Collectors.toList());

        return new PagedReaderResponse<>(
                pageContent,
                request.getPage(),
                request.getSize(),
                totalElements
        );
    }

    public ReaderResponse getById(String id) {
        return ReaderMapper.toResponse(findOrThrow(id));
    }

    public ReaderResponse create(ReaderRequest request) {
        ReaderValidator.normalize(request);
        ReaderValidator.validate(request);

        String id = resolveId(request);

        if (readerRepository.existsById(id)) {
            log.warn("Từ chối thêm bạn đọc trùng mã: {}", id);
            throw new DuplicateReaderIdException(id);
        }

        Reader reader = ReaderMapper.toModel(id, request);
        readerRepository.save(reader);

        log.info("Đã thêm bạn đọc mới: {}", id);

        return ReaderMapper.toResponse(reader);
    }

    public ReaderResponse update(String id, ReaderRequest request) {
        findOrThrow(id);

        ReaderValidator.normalize(request);
        ReaderValidator.validate(request);

        Reader updated = ReaderMapper.toModel(id, request);
        readerRepository.save(updated);

        log.info("Đã cập nhật bạn đọc: {}", id);

        return ReaderMapper.toResponse(updated);
    }

    public void delete(String id) {
        findOrThrow(id);

        boolean hasActiveTicket =
                !borrowTicketRepository
                        .findByReaderIdAndStatus(id, TicketStatus.BORROWING)
                        .isEmpty();

        if (hasActiveTicket) {
            throw new BusinessException(
                    "Không thể xóa bạn đọc đang có phiếu mượn chưa trả"
            );
        }

        readerRepository.deleteById(id);
        log.info("Đã xóa bạn đọc: {}", id);
    }

    private String resolveId(ReaderRequest request) {
        if (request.getId() != null && !request.getId().isBlank()) {
            return request.getId();
        }

        List<String> existingIds = readerRepository.findAll().stream()
                .map(Reader::getId)
                .collect(Collectors.toList());

        return IdGenerator.nextReaderId(existingIds);
    }

    private boolean matchesKeyword(Reader reader, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }

        String trimmedKeyword = keyword.trim().toLowerCase();

        boolean matchesName =
                reader.getName().toLowerCase().contains(trimmedKeyword);

        boolean matchesId =
                reader.getId().equalsIgnoreCase(keyword.trim());

        boolean matchesPhone =
                reader.getPhoneNumber().contains(trimmedKeyword);

        return matchesName || matchesId || matchesPhone;
    }

    private boolean matchesType(Reader reader, String type) {
        if (type == null || type.isBlank()) {
            return true;
        }

        return reader.getType().name().equals(type);
    }

    private Comparator<Reader> buildComparator(
            String sortBy,
            String sortDirection
    ) {
        Comparator<Reader> comparator = "name".equals(sortBy)
                ? Comparator.comparing(
                Reader::getName,
                String.CASE_INSENSITIVE_ORDER
        )
                : Comparator.comparing(Reader::getId);

        return "desc".equals(sortDirection)
                ? comparator.reversed()
                : comparator;
    }

    private Reader findOrThrow(String id) {
        return readerRepository.findById(id)
                .orElseThrow(() -> new ReaderNotFoundException(id));
    }
    public List<ReaderResponse> getAllForExport() {
        return readerRepository.findAll().stream()
                .sorted(Comparator.comparing(Reader::getId))
                .map(ReaderMapper::toResponse)
                .collect(Collectors.toList());
    }
}