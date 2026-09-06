package com.group4.library.service;

import com.group4.library.dto.ReaderDetailResponse;
import com.group4.library.dto.ReaderStatisticsResponse;
import com.group4.library.exception.ReaderNotFoundException;
import com.group4.library.model.Book;
import com.group4.library.model.BorrowTicket;
import com.group4.library.model.BorrowTicketDetail;
import com.group4.library.model.LecturerReader;
import com.group4.library.model.PriorityStudentReader;
import com.group4.library.model.StudentReader;
import com.group4.library.model.TicketStatus;
import com.group4.library.repository.BookRepository;
import com.group4.library.repository.BorrowTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ReaderDetailStatisticsTest {

    private InMemoryReaderRepository readerRepository;
    private BorrowTicketRepository borrowTicketRepository;
    private BookRepository bookRepository;
    private ReaderService readerService;

    @BeforeEach
    void setUp() {
        readerRepository = new InMemoryReaderRepository();
        borrowTicketRepository = Mockito.mock(BorrowTicketRepository.class);
        bookRepository = Mockito.mock(BookRepository.class);
        readerService = new ReaderService(readerRepository, borrowTicketRepository, bookRepository);

        when(bookRepository.findById(any())).thenReturn(Optional.empty());
    }

    // ===================== Gói 2: getDetail =====================

    @Test
    void getDetail_khongTonTai_nemReaderNotFoundException() {
        assertThrows(ReaderNotFoundException.class, () -> readerService.getDetail("R999"));
    }

    @Test
    void getDetail_khongCoPhieuNao_soLuongDangGiuBangKhong() {
        readerRepository.save(new StudentReader("R001", "Nguyễn Văn A", "0912345678"));
        when(borrowTicketRepository.findByReaderId("R001")).thenReturn(List.of());

        ReaderDetailResponse detail = readerService.getDetail("R001");

        assertEquals(0, detail.getBorrowSummary().getCurrentlyBorrowedCount());
        assertEquals(0, detail.getBorrowSummary().getActiveTicketCount());
        assertEquals(0, detail.getBorrowSummary().getOverdueTicketCount());
        assertFalse(detail.getBorrowSummary().isReachedLimit());
        assertTrue(detail.getBorrowSummary().getTickets().isEmpty());
    }

    @Test
    void getDetail_coNhieuPhieu_tongHopDungSoLuongVaDanhSachSach() {
        readerRepository.save(new StudentReader("R001", "Nguyễn Văn A", "0912345678"));

        LocalDate homNay = LocalDate.now();
        BorrowTicket phieu1 = ticket("T1", "R001", homNay.minusDays(5), homNay.plusDays(9),
                TicketStatus.BORROWING, detail("D1", "T1", "B001", 1));
        BorrowTicket phieu2 = ticket("T2", "R001", homNay.minusDays(20), homNay.minusDays(6),
                TicketStatus.RETURNED, detail("D2", "T2", "B002", 1));

        when(borrowTicketRepository.findByReaderId("R001")).thenReturn(List.of(phieu1, phieu2));
        when(bookRepository.findById("B001")).thenReturn(Optional.of(book("B001", "Clean Code")));

        ReaderDetailResponse detail = readerService.getDetail("R001");

        assertEquals(2, detail.getBorrowSummary().getTickets().size());
        assertEquals(1, detail.getBorrowSummary().getActiveTicketCount());
        assertEquals(1, detail.getBorrowSummary().getCurrentlyBorrowedCount());
        assertEquals("Clean Code",
                detail.getBorrowSummary().getTickets().stream()
                        .filter(t -> t.getTicketId().equals("T1"))
                        .findFirst().orElseThrow()
                        .getBooks().get(0).getTitle());
    }

    @Test
    void getDetail_coPhieuQuaHan_danhDauOverdueDung() {
        readerRepository.save(new StudentReader("R001", "Nguyễn Văn A", "0912345678"));

        LocalDate homNay = LocalDate.now();
        BorrowTicket phieuQuaHan = ticket("T1", "R001", homNay.minusDays(20), homNay.minusDays(5),
                TicketStatus.BORROWING, detail("D1", "T1", "B001", 1));

        when(borrowTicketRepository.findByReaderId("R001")).thenReturn(List.of(phieuQuaHan));

        ReaderDetailResponse detail = readerService.getDetail("R001");

        assertEquals(1, detail.getBorrowSummary().getOverdueTicketCount());
        assertTrue(detail.getBorrowSummary().getTickets().get(0).isOverdue());
    }

    @Test
    void getDetail_daDatGioiHanMuon_reachedLimitTrue() {
        readerRepository.save(new StudentReader("R001", "Nguyễn Văn A", "0912345678"));

        LocalDate homNay = LocalDate.now();
        BorrowTicket phieu = ticket("T1", "R001", homNay.minusDays(1), homNay.plusDays(13),
                TicketStatus.BORROWING, detail("D1", "T1", "B001", 3));

        when(borrowTicketRepository.findByReaderId("R001")).thenReturn(List.of(phieu));

        ReaderDetailResponse detail = readerService.getDetail("R001");

        assertEquals(3, detail.getBorrowSummary().getCurrentlyBorrowedCount());
        assertTrue(detail.getBorrowSummary().isReachedLimit());
    }

    // ===================== Gói 4: getStatistics =====================

    @Test
    void getStatistics_duLieuRong_tatCaBangKhong() {
        when(borrowTicketRepository.findAll()).thenReturn(List.of());

        ReaderStatisticsResponse stats = readerService.getStatistics();

        assertEquals(0, stats.getTotalReaders());
        assertEquals(0, stats.getCurrentlyBorrowingReaderCount());
        assertEquals(0, stats.getOverdueReaderCount());
        assertEquals(0, stats.getReachedLimitReaderCount());
        assertEquals(0L, stats.getCountByType().get("STUDENT"));
    }

    @Test
    void getStatistics_nhieuLoaiBanDoc_demDungTheoTungLoai() {
        readerRepository.save(new StudentReader("R001", "A", "0900000001"));
        readerRepository.save(new StudentReader("R002", "B", "0900000002"));
        readerRepository.save(new PriorityStudentReader("R003", "C", "0900000003"));
        readerRepository.save(new LecturerReader("R004", "D", "0900000004"));
        when(borrowTicketRepository.findAll()).thenReturn(List.of());

        ReaderStatisticsResponse stats = readerService.getStatistics();

        assertEquals(4, stats.getTotalReaders());
        assertEquals(2L, stats.getCountByType().get("STUDENT"));
        assertEquals(1L, stats.getCountByType().get("PRIORITY_STUDENT"));
        assertEquals(1L, stats.getCountByType().get("LECTURER"));
    }

    @Test
    void getStatistics_motNguoiNhieuPhieu_chiDemMotLanKhongTrung() {
        readerRepository.save(new StudentReader("R001", "Nguyễn Văn A", "0912345678"));

        LocalDate homNay = LocalDate.now();
        BorrowTicket phieu1 = ticket("T1", "R001", homNay.minusDays(1), homNay.plusDays(13),
                TicketStatus.BORROWING, detail("D1", "T1", "B001", 1));
        BorrowTicket phieu2 = ticket("T2", "R001", homNay.minusDays(2), homNay.plusDays(12),
                TicketStatus.BORROWING, detail("D2", "T2", "B002", 1));

        when(borrowTicketRepository.findAll()).thenReturn(List.of(phieu1, phieu2));

        ReaderStatisticsResponse stats = readerService.getStatistics();

        assertEquals(1, stats.getCurrentlyBorrowingReaderCount(),
                "1 bạn đọc có 2 phiếu vẫn chỉ được đếm 1 lần");
    }

    @Test
    void getStatistics_cacTrangThaiPhieuKhacNhau_demDungOverdueVaDangMuon() {
        readerRepository.save(new StudentReader("R001", "Đang mượn đúng hạn", "0900000001"));
        readerRepository.save(new StudentReader("R002", "Đang quá hạn", "0900000002"));
        readerRepository.save(new StudentReader("R003", "Đã trả", "0900000003"));

        LocalDate homNay = LocalDate.now();
        BorrowTicket dungHan = ticket("T1", "R001", homNay.minusDays(1), homNay.plusDays(13),
                TicketStatus.BORROWING, detail("D1", "T1", "B001", 1));
        BorrowTicket quaHan = ticket("T2", "R002", homNay.minusDays(20), homNay.minusDays(5),
                TicketStatus.BORROWING, detail("D2", "T2", "B002", 1));
        BorrowTicket daTra = ticket("T3", "R003", homNay.minusDays(20), homNay.minusDays(5),
                TicketStatus.RETURNED, detail("D3", "T3", "B003", 1));

        when(borrowTicketRepository.findAll()).thenReturn(List.of(dungHan, quaHan, daTra));

        ReaderStatisticsResponse stats = readerService.getStatistics();

        assertEquals(2, stats.getCurrentlyBorrowingReaderCount());
        assertEquals(1, stats.getOverdueReaderCount());
    }

    private BorrowTicket ticket(String ticketId, String readerId, LocalDate borrowDate, LocalDate dueDate,
                                TicketStatus status, BorrowTicketDetail... items) {
        return new BorrowTicket(ticketId, readerId, borrowDate, dueDate, null, status, List.of(items));
    }

    private BorrowTicketDetail detail(String id, String ticketId, String bookId, int quantity) {
        return new BorrowTicketDetail(id, ticketId, bookId, quantity);
    }

    private Book book(String bookId, String title) {
        return new Book(bookId, title, "Tác giả", "Thể loại", 10, 100000L);
    }
}