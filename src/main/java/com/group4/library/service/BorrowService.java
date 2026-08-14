package com.group4.library.service;

import com.group4.library.exception.InvalidQuantityException;
import com.group4.library.exception.InvalidBorrowDateException;
import com.group4.library.exception.BorrowLimitExceededException;

import com.group4.library.dto.BorrowItemRequest;
import com.group4.library.dto.BorrowItemResponse;
import com.group4.library.dto.BorrowRequest;
import com.group4.library.dto.BorrowTicketResponse;

import com.group4.library.model.BorrowTicket;
import com.group4.library.model.BorrowTicketDetail;
import com.group4.library.model.TicketStatus;

import com.group4.library.repository.BorrowTicketRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class BorrowService {

    private final BorrowTicketRepository borrowTicketRepository;
    // // Code của Bách:
    // private final ReaderRepository readerRepository;
    // // Code của Tiệp:
    // private final BookRepository bookRepository;

    // Constructor Injection chỉ giữ lại Repository của Duyên
    public BorrowService(BorrowTicketRepository borrowTicketRepository
                         /*, ReaderRepository readerRepository,
                         BookRepository bookRepository */) {
        this.borrowTicketRepository = borrowTicketRepository;
        // this.readerRepository = readerRepository;
        // this.bookRepository = bookRepository;
    }

    public BorrowTicketResponse createBorrowTicket(BorrowRequest request) {
        // 1. Kiểm tra Request & Reader Null/Empty (Yêu cầu cmt 1)
        if (request == null) {
            throw new IllegalArgumentException("Yêu cầu mượn sách không được để trống (Request is null)!");
        }
        if (request.getReaderId() == null || request.getReaderId().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã bạn đọc không được để trống!");
        }

        // 2. Kiểm tra danh sách items không null và không rỗng (Yêu cầu cmt 1)
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidQuantityException("Danh sách sách mượn không được null hoặc để trống!");
        }

        // // Code của Bách: Kiểm tra bạn đọc có tồn tại không
        // var reader = readerRepository.findById(request.getReaderId().trim())
        //         .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bạn đọc có mã: " + request.getReaderId()));

        // 3. Kiểm tra borrowDate và dueDate không null, dueDate >= borrowDate (Yêu cầu cmt 2)
        if (request.getBorrowDate() == null) {
            throw new IllegalArgumentException("Ngày mượn (borrowDate) không được để trống!");
        }
        if (request.getDueDate() == null) {
            throw new IllegalArgumentException("Hạn trả (dueDate) không được để trống!");
        }

        LocalDate borrowDate = request.getBorrowDate();
        LocalDate dueDate = request.getDueDate();

        if (dueDate.isBefore(borrowDate)) {
            throw new InvalidBorrowDateException("Hạn trả (dueDate) không được trước ngày mượn (borrowDate)!");
        }

        // 4. Validate từng item & Gộp các mã sách bị lặp lại (Yêu cầu cmt 1: Gộp số lượng tránh giảm kho sai)
        Map<String, Integer> consolidatedItems = new HashMap<>();
        for (BorrowItemRequest item : request.getItems()) {
            if (item == null) {
                throw new IllegalArgumentException("Thông tin sách mượn không được null!");
            }

            // Check code null hoặc rỗng
            if (item.getCode() == null || item.getCode().trim().isEmpty()) {
                throw new IllegalArgumentException("Mã sách không được để trống!");
            }

            // Check số lượng <= 0
            if (item.getQuantity() <= 0) {
                throw new InvalidQuantityException(
                        String.format("Số lượng mượn của sách '%s' không hợp lệ (%d). Số lượng phải lớn hơn 0!",
                                item.getCode().trim(), item.getQuantity())
                );
            }

            // Gộp số lượng nếu trùng bookCode
            String bookCode = item.getCode().trim();
            consolidatedItems.put(bookCode, consolidatedItems.getOrDefault(bookCode, 0) + item.getQuantity());
        }

        // 5. Kiểm tra giới hạn mượn (Borrow Limit)
        int newBorrowCount = consolidatedItems.values().stream().mapToInt(Integer::intValue).sum();

        List<BorrowTicket> activeTickets = borrowTicketRepository.findByReaderIdAndStatus(
                request.getReaderId().trim(), TicketStatus.BORROWING
        );

        // Đã bổ sung check null-safe tránh crash app (Thay null bằng Stream.empty())
        int currentBorrowingCount = activeTickets.stream()
                .filter(Objects::nonNull)
                .flatMap(t -> t.getItems() != null ? t.getItems().stream() : Stream.empty())
                .filter(Objects::nonNull)
                .mapToInt(BorrowTicketDetail::getQuantity)
                .sum();

        // Mặc định giới hạn tối đa 5 cuốn (chờ Bách chốt logic Reader)
        int maxLimit = 5;
        /* // Code của Bách:
        try {
            maxLimit = reader.getMaxBorrowLimit();
        } catch (Exception ignored) {}
        */

        if (currentBorrowingCount + newBorrowCount > maxLimit) {
            throw new BorrowLimitExceededException(
                    String.format("Bạn đọc đã mượn %d cuốn. Thêm %d cuốn sẽ vượt giới hạn tối đa (%d cuốn)!",
                            currentBorrowingCount, newBorrowCount, maxLimit)
            );
        }

        /* // 6. Code của Tiệp: KIỂM TRA TỒN KHO SÁCH (Comment tạm chờ Tiệp)
        Map<String, Book> bookMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : consolidatedItems.entrySet()) {
            String bookId = entry.getKey();
            int reqQty = entry.getValue();

            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách có mã: " + bookId));

            if (book.getQuantity() < reqQty) {
                throw new OutOfStockException(
                        String.format("Sách '%s' không đủ tồn kho (Còn: %d, Yêu cầu: %d)!",
                                book.getTitle(), book.getQuantity(), reqQty)
                );
            }
            bookMap.put(bookId, book);
        }
        */

        // 7. TẠO PHIẾU MƯỢN (Phần logic chính của Duyên)
        String ticketId = "TICK-" + System.currentTimeMillis();
        List<BorrowTicketDetail> details = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : consolidatedItems.entrySet()) {
            String bookCode = entry.getKey();
            int reqQty = entry.getValue();

            /* // Code của Tiệp: Trừ kho
            Book book = bookMap.get(bookCode);
            book.setQuantity(book.getQuantity() - reqQty);
            bookRepository.save(book);
            */

            // Tạo Chi tiết phiếu
            details.add(new BorrowTicketDetail(
                    UUID.randomUUID().toString(),
                    ticketId,
                    bookCode,
                    reqQty
            ));
        }

        // 8. Lưu phiếu mượn xuống JSON (Logic của Duyên)
        BorrowTicket ticket = new BorrowTicket(
                ticketId,
                request.getReaderId().trim(),
                borrowDate,
                dueDate,
                null,
                TicketStatus.BORROWING,
                details
        );
        BorrowTicket savedTicket = borrowTicketRepository.save(ticket);

        // 9. Chuyển đổi sang Response DTO
        return mapToResponse(savedTicket);
    }

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

    public BorrowTicketResponse getTicketById(String ticketId) {
        BorrowTicket ticket = borrowTicketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn với mã: " + ticketId));
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

        // Bổ sung check Objects::nonNull cho an toàn
        if (ticket.getItems() != null) {
            List<BorrowItemResponse> itemDtos = ticket.getItems().stream()
                    .filter(Objects::nonNull)
                    .map(d -> new BorrowItemResponse(d.getBookId(), d.getQuantity()))
                    .collect(Collectors.toList());
            resp.setItems(itemDtos);
        }
        return resp;
    }
}