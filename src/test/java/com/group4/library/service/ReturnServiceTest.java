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
        books.save(new Book("B001", "Java", "Tác giả", "Công nghệ", 2, 100_000));
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
        assertEquals(4, books.findByCode("B001").orElseThrow().getAvailableQuantity());
        assertEquals(TicketStatus.RETURNED, tickets.findById("BT001").orElseThrow().getStatus());
        assertEquals(1, returns.findAll().size());
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

    private static class MemoryReaderRepository implements ReaderRepository {
        private final Map<String, Reader> data = new HashMap<>();
        public List<Reader> findAll() { return new ArrayList<>(data.values()); }
        public Optional<Reader> findById(String id) { return Optional.ofNullable(data.get(id)); }
        public Reader save(Reader reader) { data.put(reader.getId(), reader); return reader; }
        public void deleteById(String id) { data.remove(id); }
        public boolean existsById(String id) { return data.containsKey(id); }
    }

    private static class MemoryBookRepository implements BookRepository {
        private final Map<String, Book> data = new HashMap<>();
        public List<Book> findAll() { return new ArrayList<>(data.values()); }
        public Optional<Book> findByCode(String code) { return Optional.ofNullable(data.get(code)); }
        public void save(Book book) { data.put(book.getCode(), book); }
        public void update(Book book) { data.put(book.getCode(), book); }
        public void deleteByCode(String code) { data.remove(code); }
    }

    private static class MemoryTicketRepository implements BorrowTicketRepository {
        private final Map<String, BorrowTicket> data = new HashMap<>();
        public List<BorrowTicket> findAll() { return new ArrayList<>(data.values()); }
        public Optional<BorrowTicket> findById(String id) { return Optional.ofNullable(data.get(id)); }
        public BorrowTicket save(BorrowTicket ticket) { data.put(ticket.getTicketId(), ticket); return ticket; }
    }

    private static class MemoryReturnRepository implements ReturnRecordRepository {
        private final Map<String, ReturnRecord> data = new LinkedHashMap<>();
        public List<ReturnRecord> findAll() { return new ArrayList<>(data.values()); }
        public Optional<ReturnRecord> findById(String id) { return Optional.ofNullable(data.get(id)); }
        public ReturnRecord save(ReturnRecord record) { data.put(record.getReturnId(), record); return record; }
    }
}
