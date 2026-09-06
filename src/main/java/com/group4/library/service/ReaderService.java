package com.group4.library.service;

import com.group4.library.dto.PagedReaderResponse;
import com.group4.library.dto.ReaderBorrowSummaryResponse;
import com.group4.library.dto.ReaderDetailResponse;
import com.group4.library.dto.ReaderRequest;
import com.group4.library.dto.ReaderResponse;
import com.group4.library.dto.ReaderSearchRequest;
import com.group4.library.dto.ReaderStatisticsResponse;
import com.group4.library.dto.ReaderTicketSummaryResponse;
import com.group4.library.exception.BusinessException;
import com.group4.library.exception.DuplicateReaderIdException;
import com.group4.library.exception.ReaderNotFoundException;
import com.group4.library.mapper.ReaderMapper;
import com.group4.library.model.Book;
import com.group4.library.model.BorrowTicket;
import com.group4.library.model.BorrowTicketDetail;
import com.group4.library.model.Reader;
import com.group4.library.model.ReaderType;
import com.group4.library.model.TicketStatus;
import com.group4.library.repository.BookRepository;
import com.group4.library.repository.BorrowTicketRepository;
import com.group4.library.repository.ReaderRepository;
import com.group4.library.utils.IdGenerator;
import com.group4.library.validation.ReaderValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReaderService {

    private static final Logger log = LoggerFactory.getLogger(ReaderService.class);

    private final ReaderRepository readerRepository;
    private final BorrowTicketRepository borrowTicketRepository;
    private final BookRepository bookRepository;

    public ReaderService(
            ReaderRepository readerRepository,
            BorrowTicketRepository borrowTicketRepository,
            BookRepository bookRepository
    ) {
        this.readerRepository = readerRepository;
        this.borrowTicketRepository = borrowTicketRepository;
        this.bookRepository = bookRepository;
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

    // ===================== Gói 2: Chi tiết bạn đọc & tình trạng mượn =====================

    public ReaderDetailResponse getDetail(String id) {
        Reader reader = findOrThrow(id);
        LocalDate today = LocalDate.now();

        List<BorrowTicket> tickets = borrowTicketRepository.findByReaderId(id);

        List<ReaderTicketSummaryResponse> ticketSummaries = tickets.stream()
                .sorted(Comparator.comparing(BorrowTicket::getBorrowDate).reversed())
                .map(ticket -> toTicketSummary(ticket, today))
                .collect(Collectors.toList());

        int currentlyBorrowedCount = sumBorrowedQuantity(tickets);
        long activeTicketCount = countActiveTickets(tickets);
        long overdueTicketCount = countOverdueTickets(tickets, today);
        boolean reachedLimit = currentlyBorrowedCount >= reader.getMaxBorrowLimit();

        ReaderBorrowSummaryResponse borrowSummary = new ReaderBorrowSummaryResponse(
                currentlyBorrowedCount,
                activeTicketCount,
                overdueTicketCount,
                reachedLimit,
                ticketSummaries
        );

        return new ReaderDetailResponse(
                reader.getId(),
                reader.getName(),
                reader.getPhoneNumber(),
                reader.getType().name(),
                reader.getMaxBorrowLimit(),
                borrowSummary
        );
    }

    private ReaderTicketSummaryResponse toTicketSummary(BorrowTicket ticket, LocalDate today) {
        boolean overdue = ticket.getStatus() == TicketStatus.BORROWING
                && ticket.getDueDate().isBefore(today);

        List<ReaderTicketSummaryResponse.BookItem> books = ticket.getItems().stream()
                .map(item -> new ReaderTicketSummaryResponse.BookItem(
                        item.getBookId(),
                        bookRepository.findById(item.getBookId())
                                .map(Book::getTitle)
                                .orElse("(Sách không còn tồn tại)"),
                        item.getQuantity()))
                .collect(Collectors.toList());

        return new ReaderTicketSummaryResponse(
                ticket.getTicketId(),
                ticket.getBorrowDate(),
                ticket.getDueDate(),
                ticket.getStatus().name(),
                overdue,
                books
        );
    }

    private int sumBorrowedQuantity(List<BorrowTicket> tickets) {
        return tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.BORROWING)
                .flatMap(t -> t.getItems().stream())
                .mapToInt(BorrowTicketDetail::getQuantity)
                .sum();
    }

    private long countActiveTickets(List<BorrowTicket> tickets) {
        return tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.BORROWING)
                .count();
    }

    private long countOverdueTickets(List<BorrowTicket> tickets, LocalDate today) {
        return tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.BORROWING && t.getDueDate().isBefore(today))
                .count();
    }

    // ===================== Gói 4: Thống kê bạn đọc =====================

    public ReaderStatisticsResponse getStatistics() {
        List<Reader> allReaders = readerRepository.findAll();
        List<BorrowTicket> allTickets = borrowTicketRepository.findAll();
        LocalDate today = LocalDate.now();

        Map<String, Long> countByType = new LinkedHashMap<>();
        Map<ReaderType, Long> grouped = allReaders.stream()
                .collect(Collectors.groupingBy(Reader::getType, Collectors.counting()));
        for (ReaderType type : ReaderType.values()) {
            countByType.put(type.name(), grouped.getOrDefault(type, 0L));
        }

        Map<String, Reader> readerById = allReaders.stream()
                .collect(Collectors.toMap(Reader::getId, reader -> reader, (a, b) -> a));

        // Một bạn đọc có nhiều phiếu BORROWING vẫn chỉ được đếm một lần -> gom nhóm theo readerId trước
        Map<String, List<BorrowTicket>> activeTicketsByReader = allTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.BORROWING)
                .collect(Collectors.groupingBy(BorrowTicket::getReaderId));

        long currentlyBorrowingReaderCount = activeTicketsByReader.size();

        long overdueReaderCount = activeTicketsByReader.values().stream()
                .filter(readerTickets -> readerTickets.stream()
                        .anyMatch(t -> t.getDueDate().isBefore(today)))
                .count();

        long reachedLimitReaderCount = activeTicketsByReader.entrySet().stream()
                .filter(entry -> {
                    Reader reader = readerById.get(entry.getKey());
                    if (reader == null) {
                        return false;
                    }
                    int borrowed = sumBorrowedQuantity(entry.getValue());
                    return borrowed >= reader.getMaxBorrowLimit();
                })
                .count();

        return new ReaderStatisticsResponse(
                allReaders.size(),
                countByType,
                currentlyBorrowingReaderCount,
                overdueReaderCount,
                reachedLimitReaderCount
        );
    }
}