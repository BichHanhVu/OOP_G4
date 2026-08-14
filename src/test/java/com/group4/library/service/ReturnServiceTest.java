package com.group4.library.service;

import com.group4.library.dto.ReturnRequest;
import com.group4.library.dto.ReturnResponse;
import com.group4.library.exception.TicketAlreadyReturnedException;
import com.group4.library.model.*;
import com.group4.library.policy.FinePolicyFactory;
import com.group4.library.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ReturnServiceTest {
    private MemoryReaderRepository readers;
    private MemoryBookRepository books;
    private MemoryTicketRepository tickets;
    private MemoryReturnRepository returns;
    private ReturnService service;

    @BeforeEach
    void setUp() {
        readers = new MemoryReaderRepository();
        books = new MemoryBookRepository();
        tickets = new MemoryTicketRepository();
        returns = new MemoryReturnRepository();
        service = new ReturnService(readers, books, tickets, returns, new FinePolicyFactory());

        readers.save(new StudentReader("R001", "Nguyễn Văn A", "0912345678"));
        books.save(new Book("B001", "Java", "Tác giả", "Công nghệ", 2, 100000L));
        tickets.save(new BorrowTicket("BT001", "R001", LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 10), TicketStatus.BORROWING,
                List.of(new BorrowTicketDetail("B001", 2))));
    }

    @Test
    void traQuaHan_tinhPhiVaHoanKhoDung() {
        ReturnResponse response = service.returnBooks(
                new ReturnRequest("BT001", LocalDate.of(2026, 8, 13)));

        assertEquals(3, response.getLateDays());
        assertEquals(15_000L, response.getFineAmount());
        assertEquals(4, books.findById("B001").orElseThrow().getAvailableQuantity());
        assertEquals(TicketStatus.RETURNED, tickets.findById("BT001").orElseThrow().getStatus());
        assertEquals(1, returns.findAll().size());
        assertEquals(actualReturnDate, tickets.findById("BT001").orElseThrow().getReturnDate());
    }

    @Test
    void traDungHan_khongPhat() {
        ReturnResponse response = service.returnBooks(
                new ReturnRequest("BT001", LocalDate.of(2026, 8, 10)));
        assertEquals(0, response.getLateDays());
        assertEquals(0, response.getFineAmount());
    }

    @Test
    void khongChoTraLaiPhieuDaTra() {
        service.returnBooks(new ReturnRequest("BT001", LocalDate.of(2026, 8, 10)));
        assertThrows(TicketAlreadyReturnedException.class,
                () -> service.returnBooks(new ReturnRequest("BT001", LocalDate.of(2026, 8, 11))));
    }

    private static class MemoryTicketRepository
            implements BorrowTicketRepository {

        private final Map<String, BorrowTicket> data = new HashMap<>();

        @Override
        public List<BorrowTicket> findAll() {
            return new ArrayList<>(data.values());
        }

        @Override
        public Optional<BorrowTicket> findById(String id) {
            return Optional.ofNullable(data.get(id));
        }

        @Override
        public BorrowTicket save(BorrowTicket ticket) {
            data.put(ticket.getTicketId(), ticket);
            return ticket;
        }

        @Override
        public List<BorrowTicket> findByReaderId(String readerId) {
            return data.values().stream()
                    .filter(ticket ->
                            readerId.equalsIgnoreCase(ticket.getReaderId()))
                    .toList();
        }

        @Override
        public List<BorrowTicket> findByStatus(TicketStatus status) {
            return data.values().stream()
                    .filter(ticket -> ticket.getStatus() == status)
                    .toList();
        }

        @Override
        public List<BorrowTicket> findByReaderIdAndStatus(
                String readerId,
                TicketStatus status) {

            return data.values().stream()
                    .filter(ticket ->
                            readerId.equalsIgnoreCase(ticket.getReaderId()))
                    .filter(ticket -> ticket.getStatus() == status)
                    .toList();
        }
    }
}
