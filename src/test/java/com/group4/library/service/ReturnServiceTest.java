package com.group4.library.service;

import com.group4.library.dto.ReturnRequest;
import com.group4.library.dto.ReturnResponse;
import com.group4.library.exception.BusinessException;
import com.group4.library.exception.ResourceNotFoundException;
import com.group4.library.exception.TicketAlreadyReturnedException;
import com.group4.library.exception.TicketNotFoundException;
import com.group4.library.model.*;
import com.group4.library.policy.FinePolicyFactory;
import com.group4.library.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

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

    // ===================== Validate đầu vào của yêu cầu trả sách =====================

    @Test
    void returnBooks_requestNull_nemBusinessException() {
        assertThrows(BusinessException.class, () -> service.returnBooks(null));
    }

    @Test
    void returnBooks_maPhieuRong_nemBusinessException() {
        assertThrows(BusinessException.class,
                () -> service.returnBooks(new ReturnRequest("   ", LocalDate.of(2026, 8, 10))));
    }

    @Test
    void returnBooks_ngayTraNull_nemBusinessException() {
        assertThrows(BusinessException.class,
                () -> service.returnBooks(new ReturnRequest("BT001", null)));
    }

    @Test
    void returnBooks_khongTimThayPhieuMuon_nemTicketNotFoundException() {
        assertThrows(TicketNotFoundException.class,
                () -> service.returnBooks(new ReturnRequest("BT999", LocalDate.of(2026, 8, 10))));
    }

    @Test
    void returnBooks_ngayTraTruocNgayMuon_nemBusinessException() {
        assertThrows(BusinessException.class,
                () -> service.returnBooks(new ReturnRequest("BT001", LocalDate.of(2026, 7, 31))));
    }

    @Test
    void returnBooks_hanTraBiThieu_nemBusinessException() {
        // Tạo phiếu bằng constructor rỗng + setter để bỏ qua validate ở BorrowTicket,
        // mô phỏng dữ liệu cũ/hỏng thiếu hạn trả (dueDate == null).
        BorrowTicket phieuThieuHanTra = new BorrowTicket();
        phieuThieuHanTra.setTicketId("BT500");
        phieuThieuHanTra.setReaderId("R001");
        phieuThieuHanTra.setBorrowDate(LocalDate.of(2026, 8, 1));
        phieuThieuHanTra.setStatus(TicketStatus.BORROWING);
        phieuThieuHanTra.setItems(List.of(new BorrowTicketDetail("TD500", "BT500", "B001", 1)));
        tickets.save(phieuThieuHanTra);

        assertThrows(BusinessException.class,
                () -> service.returnBooks(new ReturnRequest("BT500", LocalDate.of(2026, 8, 10))));
    }

    @Test
    void returnBooks_phieuKhongCoSach_nemBusinessException() {
        BorrowTicket phieuRongSach = new BorrowTicket("BT501", "R001", LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 10), null, TicketStatus.BORROWING, List.of());
        tickets.save(phieuRongSach);

        assertThrows(BusinessException.class,
                () -> service.returnBooks(new ReturnRequest("BT501", LocalDate.of(2026, 8, 10))));
    }

    @Test
    void returnBooks_sachTrongPhieuKhongTonTai_nemResourceNotFoundException() {
        BorrowTicket phieuSachLa = new BorrowTicket("BT502", "R001", LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 10), null, TicketStatus.BORROWING,
                List.of(new BorrowTicketDetail("TD502", "BT502", "B999", 1)));
        tickets.save(phieuSachLa);

        assertThrows(ResourceNotFoundException.class,
                () -> service.returnBooks(new ReturnRequest("BT502", LocalDate.of(2026, 8, 10))));
    }

    @Test
    void returnBooks_khongTimThayBanDoc_nemResourceNotFoundException() {
        BorrowTicket phieuBanDocLa = new BorrowTicket("BT503", "R999", LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 10), null, TicketStatus.BORROWING,
                List.of(new BorrowTicketDetail("TD503", "BT503", "B001", 1)));
        tickets.save(phieuBanDocLa);

        assertThrows(ResourceNotFoundException.class,
                () -> service.returnBooks(new ReturnRequest("BT503", LocalDate.of(2026, 8, 10))));
    }

    // ===================== Ranh giới ngày trả so với hạn trả (dueDate) =====================
    // BT001 có dueDate = 2026-08-10. Kiểm tra chặt các mốc: trước hạn 1 ngày,
    // đúng hạn (0 ngày trễ) và trễ 1 ngày ngay sau hạn.

    private static Stream<Arguments> ranhGioiNgayTraSoVoiHanTra() {
        return Stream.of(
                // offsetNgay so với dueDate, lateDays mong muốn, tienPhat mong muốn (SV thường)
                arguments(-1L, 0L, 0L),
                arguments(0L, 0L, 0L),
                arguments(1L, 1L, 5_000L)
        );
    }

    @ParameterizedTest(name = "[{index}] trả hạn+{0} ngày -> lateDays={1}, phạt={2}")
    @MethodSource("ranhGioiNgayTraSoVoiHanTra")
    void ranhGioiNgayTraSoVoiHanTra(long offsetNgay, long lateDaysMongMuon, long tienPhatMongMuon) {
        LocalDate dueDate = LocalDate.of(2026, 8, 10);
        LocalDate actualReturnDate = dueDate.plusDays(offsetNgay);

        ReturnResponse response = service.returnBooks(new ReturnRequest("BT001", actualReturnDate));

        assertEquals(lateDaysMongMuon, response.getLateDays());
        assertEquals(tienPhatMongMuon, response.getFineAmount());
        assertEquals(actualReturnDate, tickets.findById("BT001").orElseThrow().getReturnDate());
    }

    // ===================== Nhiều bạn đọc cùng quá hạn - thống kê theo loại =====================

    @Test
    void nhieuBanDoc_quaHanCungLuc_thongKeTongPhatTheoTungLoaiBanDoc() {
        // Bổ sung bạn đọc ưu tiên và giảng viên, mỗi người một phiếu mượn riêng
        readers.save(new PriorityStudentReader("R002", "Trần Thị B", "0900000002"));
        readers.save(new LecturerReader("R003", "Lê Văn C", "0900000003"));
        books.save(new Book("B002", "Spring Boot", "Tác giả 2", "Công nghệ", 3, 120000L));
        books.save(new Book("B003", "Clean Code", "Tác giả 3", "Công nghệ", 3, 150000L));

        LocalDate dueDate = LocalDate.of(2026, 8, 10);
        tickets.save(new BorrowTicket("BT002", "R002", LocalDate.of(2026, 8, 1), dueDate, null,
                TicketStatus.BORROWING, List.of(new BorrowTicketDetail("TD002", "BT002", "B002", 1))));
        tickets.save(new BorrowTicket("BT003", "R003", LocalDate.of(2026, 8, 1), dueDate, null,
                TicketStatus.BORROWING, List.of(new BorrowTicketDetail("TD003", "BT003", "B003", 1))));

        LocalDate actualReturnDate = dueDate.plusDays(5); // Cả 3 phiếu cùng quá hạn 5 ngày, cùng một ngày trả

        service.returnBooks(new ReturnRequest("BT001", actualReturnDate)); // R001 - Sinh viên thường
        service.returnBooks(new ReturnRequest("BT002", actualReturnDate)); // R002 - Sinh viên ưu tiên
        service.returnBooks(new ReturnRequest("BT003", actualReturnDate)); // R003 - Giảng viên

        // Thống kê tổng tiền phạt theo từng loại bạn đọc, kết hợp thông tin quá hạn của từng phiếu
        Map<ReaderType, Long> tongPhatTheoLoai = new EnumMap<>(ReaderType.class);
        for (ReturnRecord record : returns.findAll()) {
            assertEquals(5, record.getLateDays(),
                    "Phiếu " + record.getTicketId() + " phải quá hạn đúng 5 ngày");
            BorrowTicket ticket = tickets.findById(record.getTicketId()).orElseThrow();
            Reader reader = readers.findById(ticket.getReaderId()).orElseThrow();
            tongPhatTheoLoai.merge(reader.getType(), record.getFineAmount(), Long::sum);
        }

        assertEquals(3, returns.findAll().size());
        assertEquals(25_000L, tongPhatTheoLoai.get(ReaderType.STUDENT));
        assertEquals(15_000L, tongPhatTheoLoai.get(ReaderType.PRIORITY_STUDENT));
        assertEquals(10_000L, tongPhatTheoLoai.get(ReaderType.LECTURER));
        assertEquals(50_000L, tongPhatTheoLoai.values().stream().mapToLong(Long::longValue).sum());
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