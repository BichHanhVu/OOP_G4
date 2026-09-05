package com.group4.library.service;

import com.group4.library.dto.BorrowItemRequest;
import com.group4.library.dto.BorrowItemResponse;
import com.group4.library.dto.BorrowRequest;
import com.group4.library.dto.BorrowTicketResponse;

import com.group4.library.exception.BorrowLimitExceededException;
import com.group4.library.exception.InvalidBorrowDateException;
import com.group4.library.exception.InvalidQuantityException;
import com.group4.library.exception.OutOfStockException;
import com.group4.library.exception.ResourceNotFoundException;

import com.group4.library.model.Book;
import com.group4.library.model.BorrowTicket;
import com.group4.library.model.BorrowTicketDetail;
import com.group4.library.model.Reader;
import com.group4.library.model.TicketStatus;

import com.group4.library.repository.BookRepository;
import com.group4.library.repository.BorrowTicketRepository;
import com.group4.library.repository.ReaderRepository;

import com.group4.library.dto.RenewTicketRequest;
import com.group4.library.dto.RenewTicketResponse;
import com.group4.library.exception.RenewalNotAllowedException;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class BorrowService {

    private final BorrowTicketRepository borrowTicketRepository;
    private final ReaderRepository readerRepository;
    private final BookRepository bookRepository;

    public BorrowService(BorrowTicketRepository borrowTicketRepository,
                         ReaderRepository readerRepository,
                         BookRepository bookRepository) {
        this.borrowTicketRepository = borrowTicketRepository;
        this.readerRepository = readerRepository;
        this.bookRepository = bookRepository;
    }

    public BorrowTicketResponse createBorrowTicket(BorrowRequest request) {
        // 1. Validation request & reader
        if (request == null) {
            throw new IllegalArgumentException("Yêu cầu mượn sách không được để trống");
        }

        if (request.getReaderId() == null || request.getReaderId().isBlank()) {
            throw new IllegalArgumentException("Mã bạn đọc không được để trống");
        }

        // 2. Validation ngày mượn & hạn trả
        if (request.getBorrowDate() == null) {
            throw new InvalidBorrowDateException("Ngày mượn không được để trống");
        }

        if (request.getDueDate() == null) {
            throw new InvalidBorrowDateException("Hạn trả không được để trống");
        }

        if (request.getDueDate().isBefore(request.getBorrowDate())) {
            throw new InvalidBorrowDateException("Hạn trả không được trước ngày mượn");
        }

        // 3. Validation danh sách items
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidQuantityException("Danh sách sách mượn không được để trống");
        }

        // 4. Kiểm tra bạn đọc có tồn tại trong hệ thống (Mục 11)
        Reader reader = readerRepository.findById(request.getReaderId().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bạn đọc: " + request.getReaderId()));

        // 5. Gộp các mã sách bị lặp bằng LinkedHashMap (Mục 14)
        Map<String, Integer> consolidatedItems = new LinkedHashMap<>();

        for (BorrowItemRequest item : request.getItems()) {
            if (item == null) {
                throw new IllegalArgumentException("Thông tin sách mượn không được null");
            }

            if (item.getBookId() == null || item.getBookId().isBlank()) {
                throw new IllegalArgumentException("Mã sách không được để trống");
            }

            if (item.getQuantity() <= 0) {
                throw new InvalidQuantityException("Số lượng mượn phải lớn hơn 0");
            }

            String bookId = item.getBookId()
                    .trim()
                    .toUpperCase();

            consolidatedItems.merge(
                    bookId,
                    item.getQuantity(),
                    Integer::sum
            );
        }

        // 6. Kiểm tra giới hạn mượn lấy từ Reader (Mục 12)
        int newBorrowCount = consolidatedItems.values().stream().mapToInt(Integer::intValue).sum();

        List<BorrowTicket> activeTickets = borrowTicketRepository.findByReaderIdAndStatus(
                request.getReaderId().trim(), TicketStatus.BORROWING
        );

        int currentBorrowingCount = activeTickets.stream()
                .filter(Objects::nonNull)
                .flatMap(ticket -> ticket.getItems() != null ? ticket.getItems().stream() : Stream.empty())
                .filter(Objects::nonNull)
                .mapToInt(BorrowTicketDetail::getQuantity)
                .sum();

        int maxLimit = reader.getMaxBorrowLimit();

        if (currentBorrowingCount + newBorrowCount > maxLimit) {
            throw new BorrowLimitExceededException(
                    String.format("Bạn đọc đã mượn %d cuốn. Thêm %d cuốn sẽ vượt giới hạn tối đa (%d cuốn)!",
                            currentBorrowingCount, newBorrowCount, maxLimit)
            );
        }

        // 7. Kiểm tra tồn kho sách
        Map<String, Book> bookMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : consolidatedItems.entrySet()) {
            String bookId = entry.getKey();
            int reqQty = entry.getValue();

            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách: " + bookId));

            if (!book.canBorrow(reqQty)) {
                throw new OutOfStockException(
                        String.format(
                                "Sách '%s' không đủ tồn kho (Còn: %d, Yêu cầu: %d)!",
                                book.getTitle(),
                                book.getAvailableQuantity(),
                                reqQty
                        )
                );
            }
            bookMap.put(bookId, book);
        }

        // 8. Tạo phiếu mượn & trừ kho
        String ticketId = "TICK-" + System.currentTimeMillis();
        List<BorrowTicketDetail> details = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : consolidatedItems.entrySet()) {
            String bookId = entry.getKey();
            int requestedQuantity = entry.getValue();

            Book book = bookMap.get(bookId);
            book.borrow(requestedQuantity);

            boolean updated = bookRepository.update(book);

            if (!updated) {
                throw new IllegalStateException(
                        "Không thể cập nhật tồn kho sách: " + bookId
                );
            }

            details.add(new BorrowTicketDetail(
                    UUID.randomUUID().toString(),
                    ticketId,
                    bookId,
                    requestedQuantity
            ));
        }

        BorrowTicket ticket = new BorrowTicket(
                ticketId,
                request.getReaderId().trim(),
                request.getBorrowDate(),
                request.getDueDate(),
                null,
                TicketStatus.BORROWING,
                details
        );
        BorrowTicket savedTicket = borrowTicketRepository.save(ticket);

        return mapToResponse(savedTicket);
    }

    // API Lấy danh sách phiếu (Mục 15)
    public List<BorrowTicketResponse> getAllTickets(String readerId, TicketStatus status) {
        List<BorrowTicket> tickets;
        if (readerId != null && status != null) {
            tickets = borrowTicketRepository.findByReaderIdAndStatus(readerId, status);
        } else if (readerId != null) {
            tickets = borrowTicketRepository.findByReaderId(readerId);
        } else if (status != null) {
            tickets = borrowTicketRepository.findByStatus(status);
        } else {
            tickets = borrowTicketRepository.findAll();
        }

        return tickets.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // API Lấy phiếu theo ID (Mục 15 - Dùng ResourceNotFoundException)
    public BorrowTicketResponse getTicketById(String ticketId) {
        BorrowTicket ticket = borrowTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu mượn: " + ticketId));
        return mapToResponse(ticket);
    }

    private BorrowTicketResponse mapToResponse(BorrowTicket ticket) {
        BorrowTicketResponse resp = new BorrowTicketResponse();
        resp.setTicketId(ticket.getTicketId());
        resp.setReaderId(ticket.getReaderId());
        resp.setBorrowDate(ticket.getBorrowDate());
        resp.setDueDate(ticket.getDueDate());
        resp.setReturnDate(ticket.getReturnDate());
        resp.setStatus(ticket.getStatus());
        resp.setRenewalCount(ticket.getRenewalCount());

        if (ticket.getItems() != null) {
            List<BorrowItemResponse> itemDtos = ticket.getItems().stream()
                    .filter(Objects::nonNull)
                    .map(d -> new BorrowItemResponse(d.getBookId(), d.getQuantity()))
                    .collect(Collectors.toList());
            resp.setItems(itemDtos);
        }
        return resp;
    }
    public RenewTicketResponse renewBorrowTicket(
            String ticketId,
            RenewTicketRequest request) {

        // 1. Kiểm tra request
        if (request == null) {
            throw new RenewalNotAllowedException(
                    "Thông tin gia hạn không được để trống"
            );
        }

        if (request.getNewDueDate() == null) {
            throw new RenewalNotAllowedException(
                    "Ngày hẹn trả mới không được để trống"
            );
        }

        // 2. Tìm phiếu
        BorrowTicket ticket = borrowTicketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy phiếu mượn: " + ticketId
                        )
                );

        // 3. Chỉ BORROWING mới được gia hạn
        if (ticket.getStatus() != TicketStatus.BORROWING) {
            throw new RenewalNotAllowedException(
                    "Chỉ phiếu đang mượn mới được gia hạn"
            );
        }

        LocalDate today = LocalDate.now();

        // 4. Không cho gia hạn phiếu đã quá hạn
        if (ticket.getDueDate() != null
                && ticket.getDueDate().isBefore(today)) {

            throw new RenewalNotAllowedException(
                    "Không thể gia hạn phiếu đã quá hạn"
            );
        }

        // 5. Chỉ được gia hạn một lần
        if (ticket.getRenewalCount() >= 1) {
            throw new RenewalNotAllowedException(
                    "Phiếu mượn này đã được gia hạn trước đó"
            );
        }

        LocalDate oldDueDate = ticket.getDueDate();
        LocalDate newDueDate = request.getNewDueDate();

        // 6. Ngày mới phải sau hạn cũ
        if (!newDueDate.isAfter(oldDueDate)) {
            throw new RenewalNotAllowedException(
                    "Ngày hẹn trả mới phải sau ngày hẹn trả hiện tại"
            );
        }

        // 7. Ngày mới không được trước ngày hiện tại
        if (newDueDate.isBefore(today)) {
            throw new RenewalNotAllowedException(
                    "Ngày hẹn trả mới không được trước ngày hiện tại"
            );
        }

        // 8. Cập nhật phiếu
        ticket.setDueDate(newDueDate);
        ticket.setRenewalCount(ticket.getRenewalCount() + 1);

        // 9. Lưu lại
        BorrowTicket savedTicket =
                borrowTicketRepository.save(ticket);

        // 10. Tạo response riêng cho thao tác gia hạn
        RenewTicketResponse response =
                new RenewTicketResponse();

        response.setTicketId(savedTicket.getTicketId());
        response.setOldDueDate(oldDueDate);
        response.setNewDueDate(savedTicket.getDueDate());
        response.setRenewalCount(savedTicket.getRenewalCount());
        response.setStatus(savedTicket.getStatus());

        return response;
    }
}