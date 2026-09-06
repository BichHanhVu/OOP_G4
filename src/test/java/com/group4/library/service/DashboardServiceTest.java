package com.group4.library.service;

import com.group4.library.dto.DashboardResponse;
import com.group4.library.model.BorrowTicket;
import com.group4.library.model.BorrowTicketDetail;
import com.group4.library.model.Book;
import com.group4.library.model.Reader;
import com.group4.library.model.ReturnRecord;
import com.group4.library.model.StudentReader;
import com.group4.library.model.TicketStatus;
import com.group4.library.repository.BookRepository;
import com.group4.library.repository.BorrowTicketRepository;
import com.group4.library.repository.ReaderRepository;
import com.group4.library.repository.ReturnRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

/**
 * DashboardService trước đây chưa có test riêng. Điểm quan trọng nhất cần
 * kiểm chặt là ranh giới "quá hạn" của dashboard: dùng dueDate.isBefore(today),
 * khác với ReturnService (dùng DAYS.between rồi max(0, ...)). Cụ thể, một
 * phiếu có dueDate đúng bằng hôm nay KHÔNG được tính là quá hạn ở dashboard.
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ReaderRepository readerRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BorrowTicketRepository ticketRepository;
    @Mock
    private ReturnRecordRepository returnRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private static BorrowTicket ticketDueAt(String id, LocalDate dueDate, TicketStatus status) {
        return new BorrowTicket(id, "R001", dueDate.minusDays(10), dueDate, null, status,
                List.of(new BorrowTicketDetail(id + "-D1", id, "B001", 1)));
    }

    @Test
    void thongKe_soLuongDocGiaVaSachDungTongSo() {
        Reader r1 = new StudentReader("R001", "Nguyễn Văn A", "0900000001");
        Reader r2 = new StudentReader("R002", "Trần Thị B", "0900000002");
        when(readerRepository.findAll()).thenReturn(List.of(r1, r2));
        when(bookRepository.findAll()).thenReturn(
                List.of(new Book("B001", "Java", "TG", "TL", 5, 100000L)));
        when(ticketRepository.findAll()).thenReturn(List.of());
        when(returnRepository.findAll()).thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboard();

        assertEquals(2, response.getTotalReaders());
        assertEquals(1, response.getTotalBooks());
        assertEquals(0, response.getBorrowingTickets());
        assertEquals(0, response.getOverdueTickets());
        assertEquals(0, response.getTotalFineAmount());
    }

    @Test
    void thongKe_dangMuonVaDaTra_chiDemDangMuon() {
        LocalDate today = LocalDate.now();
        BorrowTicket dangMuon = ticketDueAt("BT001", today.plusDays(5), TicketStatus.BORROWING);
        BorrowTicket daTra = ticketDueAt("BT002", today.minusDays(20), TicketStatus.RETURNED);
        when(readerRepository.findAll()).thenReturn(List.of());
        when(bookRepository.findAll()).thenReturn(List.of());
        when(ticketRepository.findAll()).thenReturn(List.of(dangMuon, daTra));
        when(returnRepository.findAll()).thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboard();

        assertEquals(1, response.getBorrowingTickets());
        assertEquals(0, response.getOverdueTickets());
    }

    @Test
    void thongKe_tongTienPhat_congDonTuTatCaPhieuTra() {
        when(readerRepository.findAll()).thenReturn(List.of());
        when(bookRepository.findAll()).thenReturn(List.of());
        when(ticketRepository.findAll()).thenReturn(List.of());
        when(returnRepository.findAll()).thenReturn(List.of(
                new ReturnRecord("RT001", "BT001", LocalDate.now(), 2, 10_000L),
                new ReturnRecord("RT002", "BT002", LocalDate.now(), 5, 25_000L)));

        DashboardResponse response = dashboardService.getDashboard();

        assertEquals(35_000L, response.getTotalFineAmount());
    }

    // ===================== Ranh giới "quá hạn" theo dueDate so với hôm nay =====================

    private static Stream<Arguments> ranhGioiQuaHanTheoHomNay() {
        return Stream.of(
                // offsetNgay so với hôm nay, soPhieuQuaHanMongMuon, moTa
                arguments(-1L, 1L, "Hạn trả hôm qua -> tính là quá hạn"),
                arguments(0L, 0L, "Hạn trả đúng hôm nay -> CHƯA tính là quá hạn"),
                arguments(1L, 0L, "Hạn trả ngày mai -> chưa quá hạn")
        );
    }

    @ParameterizedTest(name = "[{index}] {2}")
    @MethodSource("ranhGioiQuaHanTheoHomNay")
    void ranhGioiQuaHanTheoHomNay_dungVoiTungMoc(long offsetNgay, long soPhieuQuaHanMongMuon, String moTa) {
        LocalDate dueDate = LocalDate.now().plusDays(offsetNgay);
        BorrowTicket ticket = ticketDueAt("BT100", dueDate, TicketStatus.BORROWING);
        when(readerRepository.findAll()).thenReturn(List.of());
        when(bookRepository.findAll()).thenReturn(List.of());
        when(ticketRepository.findAll()).thenReturn(List.of(ticket));
        when(returnRepository.findAll()).thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboard();

        assertEquals(soPhieuQuaHanMongMuon, response.getOverdueTickets(), moTa);
    }

    @Test
    void quaHan_chiTinhPhieuDangMuon_boQuaPhieuDaTraDuMocQuaHan() {
        LocalDate today = LocalDate.now();
        BorrowTicket daTraNhungQuaHan = ticketDueAt("BT200", today.minusDays(3), TicketStatus.RETURNED);
        when(readerRepository.findAll()).thenReturn(List.of());
        when(bookRepository.findAll()).thenReturn(List.of());
        when(ticketRepository.findAll()).thenReturn(List.of(daTraNhungQuaHan));
        when(returnRepository.findAll()).thenReturn(List.of());

        DashboardResponse response = dashboardService.getDashboard();

        assertEquals(0, response.getOverdueTickets());
    }
}