package com.group4.library.service;

import com.group4.library.dto.ReturnRequest;
import com.group4.library.dto.ReturnResponse;
import com.group4.library.exception.BusinessException;
import com.group4.library.exception.ResourceNotFoundException;
import com.group4.library.exception.TicketAlreadyReturnedException;
import com.group4.library.exception.TicketNotFoundException;
import com.group4.library.model.Book;
import com.group4.library.model.BorrowTicket;
import com.group4.library.model.BorrowTicketDetail;
import com.group4.library.model.Reader;
import com.group4.library.model.ReturnRecord;
import com.group4.library.model.TicketStatus;
import com.group4.library.policy.FinePolicy;
import com.group4.library.policy.FinePolicyFactory;
import com.group4.library.repository.BookRepository;
import com.group4.library.repository.BorrowTicketRepository;
import com.group4.library.repository.ReaderRepository;
import com.group4.library.repository.ReturnRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReturnService {
    private final ReaderRepository readerRepository;
    private final BookRepository bookRepository;
    private final BorrowTicketRepository ticketRepository;
    private final ReturnRecordRepository returnRepository;
    private final FinePolicyFactory finePolicyFactory;

    public ReturnService(ReaderRepository readerRepository, BookRepository bookRepository,
                         BorrowTicketRepository ticketRepository,
                         ReturnRecordRepository returnRepository,
                         FinePolicyFactory finePolicyFactory) {
        this.readerRepository = readerRepository;
        this.bookRepository = bookRepository;
        this.ticketRepository = ticketRepository;
        this.returnRepository = returnRepository;
        this.finePolicyFactory = finePolicyFactory;
    }

    public List<ReturnResponse> getAll() {
        return returnRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ReturnResponse returnBooks(ReturnRequest request) {
        validateRequest(request);

        BorrowTicket ticket = ticketRepository.findById(request.getTicketId().trim())
                .orElseThrow(() -> new TicketNotFoundException(
                        "Không tìm thấy phiếu mượn: " + request.getTicketId()));

        if (ticket.getStatus() != TicketStatus.BORROWING) {
            throw new TicketAlreadyReturnedException("Phiếu mượn đã được trả: " + ticket.getTicketId());
        }
        if (ticket.getBorrowDate() == null || ticket.getDueDate() == null) {
            throw new BusinessException("Dữ liệu ngày của phiếu mượn không hợp lệ");
        }
        if (request.getActualReturnDate().isBefore(ticket.getBorrowDate())) {
            throw new BusinessException("Ngày trả không được trước ngày mượn");
        }

        Reader reader = readerRepository.findById(ticket.getReaderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy bạn đọc: " + ticket.getReaderId()));

        List<BookUpdate> updates = validateAndPrepareBooks(ticket.getItems());
        long lateDays = Math.max(0,
                ChronoUnit.DAYS.between(ticket.getDueDate(), request.getActualReturnDate()));
        FinePolicy policy = finePolicyFactory.getPolicy(reader.getType());
        long fineAmount = policy.calculateFine(lateDays);

        for (BookUpdate update : updates) {
            for (int i = 0; i < update.quantity(); i++) update.book().returnItem();
            bookRepository.update(update.book());
        }

        ticket.setStatus(TicketStatus.RETURNED);
        ticketRepository.save(ticket);

        ReturnRecord record = new ReturnRecord(generateReturnId(), ticket.getTicketId(),
                request.getActualReturnDate(), lateDays, fineAmount);
        returnRepository.save(record);
        return toResponse(record);
    }

    private void validateRequest(ReturnRequest request) {
        if (request == null) throw new BusinessException("Yêu cầu trả sách không được để trống");
        if (request.getTicketId() == null || request.getTicketId().trim().isEmpty()) {
            throw new BusinessException("Mã phiếu mượn không được để trống");
        }
        if (request.getActualReturnDate() == null) {
            throw new BusinessException("Ngày trả thực tế không được để trống");
        }
    }

    private List<BookUpdate> validateAndPrepareBooks(List<BorrowTicketDetail> items) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException("Phiếu mượn không có sách");
        }
        List<BookUpdate> updates = new ArrayList<>();
        for (BorrowTicketDetail item : items) {
            if (item == null || item.getBookId() == null || item.getBookId().isBlank()) {
                throw new BusinessException("Mã sách trong phiếu không hợp lệ");
            }
            if (item.getQuantity() <= 0) {
                throw new BusinessException("Số lượng sách trong phiếu phải lớn hơn 0");
            }
            Book book = bookRepository.findByCode(item.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy sách: " + item.getBookId()));
            updates.add(new BookUpdate(book, item.getQuantity()));
        }
        return updates;
    }

    private String generateReturnId() {
        int max = returnRepository.findAll().stream()
                .map(ReturnRecord::getReturnId)
                .filter(id -> id != null && id.matches("RT\\d+"))
                .mapToInt(id -> Integer.parseInt(id.substring(2)))
                .max().orElse(0);
        return String.format("RT%03d", max + 1);
    }

    private ReturnResponse toResponse(ReturnRecord r) {
        return new ReturnResponse(r.getReturnId(), r.getTicketId(), r.getActualReturnDate(),
                r.getLateDays(), r.getFineAmount());
    }

    private record BookUpdate(Book book, int quantity) {}
}
