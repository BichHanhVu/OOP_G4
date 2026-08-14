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

        // Cập nhật constructor BorrowTicketDetail truyền đủ 4 tham số: (detailId, ticketId, bookId, quantity)
        tickets.save(new BorrowTicket("BT001", "R001", LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 10), null, TicketStatus.BORROWING,
                List.of(new BorrowTicketDetail("TD001", "BT001", "B001", 2))));
    }

    @Test
    void traQuaHan_tinhPhiVaHoanKhoDung() {
        LocalDate actualReturnDate = LocalDate.of(2026, 8, 13);
        ReturnResponse response = service.returnBooks(new ReturnRequest("BT001", actualReturnDate));

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

    // --- Fake Memory Repositories chuẩn khớp Interface ---

    private static class MemoryTicketRepository implements BorrowTicketRepository {
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
                    .filter(ticket -> readerId.equalsIgnoreCase(ticket.getReaderId()))
                    .toList();
        }

        @Override
        public List<BorrowTicket> findByStatus(TicketStatus status) {
            return data.values().stream()
                    .filter(ticket -> ticket.getStatus() == status)
                    .toList();
        }

        @Override
        public List<BorrowTicket> findByReaderIdAndStatus(String readerId, TicketStatus status) {
            return data.values().stream()
                    .filter(ticket -> readerId.equalsIgnoreCase(ticket.getReaderId()))
                    .filter(ticket -> ticket.getStatus() == status)
                    .toList();
        }
    }

    private static class MemoryReaderRepository implements ReaderRepository {
        private final Map<String, Reader> data = new HashMap<>();

        @Override
        public Optional<Reader> findById(String id) {
            return Optional.ofNullable(data.get(id));
        }

        @Override
        public Reader save(Reader reader) {
            data.put(reader.getId(), reader); // Đổi reader.getReaderId() -> reader.getId()
            return reader;
        }

        @Override
        public List<Reader> findAll() {
            return new ArrayList<>(data.values());
        }

        @Override
        public boolean existsById(String id) {
            return data.containsKey(id);
        }

        @Override
        public void deleteById(String id) { // Đổi kiếu trả về void khớp với ReaderRepository
            data.remove(id);
        }
    }

    private static class MemoryBookRepository implements BookRepository {
        private final Map<String, Book> data = new HashMap<>();

        @Override
        public Optional<Book> findById(String id) {
            return Optional.ofNullable(data.get(id));
        }

        @Override
        public void save(Book book) { // Đổi kiểu trả về void khớp với BookRepository
            data.put(book.getBookId(), book);
        }

        @Override
        public List<Book> findAll() {
            return new ArrayList<>(data.values());
        }

        @Override
        public boolean update(Book book) {
            data.put(book.getBookId(), book);
            return true;
        }

        @Override
        public boolean deleteByCode(String code) {
            return data.remove(code) != null;
        }

        @Override
        public List<Book> searchByIdOrTitle(String keyword) {
            return data.values().stream()
                    .filter(b -> b.getBookId().contains(keyword) || b.getTitle().contains(keyword))
                    .toList();
        }

        @Override
        public List<Book> findByTitleContaining(String title) {
            if (title == null) return Collections.emptyList();
            return data.values().stream()
                    .filter(b -> b.getTitle() != null && b.getTitle().toLowerCase().contains(title.toLowerCase()))
                    .toList();
        }
    }

    private static class MemoryReturnRepository implements ReturnRecordRepository {
        private final Map<String, ReturnRecord> data = new HashMap<>();

        @Override
        public ReturnRecord save(ReturnRecord record) {
            data.put(record.getReturnId(), record);
            return record;
        }

        @Override
        public List<ReturnRecord> findAll() {
            return new ArrayList<>(data.values());
        }

        @Override
        public Optional<ReturnRecord> findById(String id) {
            return Optional.ofNullable(data.get(id));
        }
    }
}