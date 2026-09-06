package com.group4.library.service;

import com.group4.library.dto.BookDetailResponse;
import com.group4.library.dto.BookStatisticsResponse;
import com.group4.library.exception.BookNotFoundException;
import com.group4.library.model.Book;
import com.group4.library.model.BorrowTicket;
import com.group4.library.model.BorrowTicketDetail;
import com.group4.library.model.LecturerReader;
import com.group4.library.model.Reader;
import com.group4.library.model.TicketStatus;
import com.group4.library.repository.BookRepository;
import com.group4.library.repository.BorrowTicketRepository;
import com.group4.library.repository.ReaderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Test cho hai nghiệp vụ mới của BookService: xem chi tiết & lịch sử mượn của
 * một cuốn sách (getDetail) và thống kê tổng quan kho sách (getStatistics).
 */
@ExtendWith(MockitoExtension.class)
class BookServiceDetailStatisticsTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private BorrowTicketRepository borrowTicketRepository;
    @Mock
    private ReaderRepository readerRepository;

    @InjectMocks
    private BookService bookService;

    private Book sampleBook;
    private Reader sampleReader;

    @BeforeEach
    void setUp() {
        sampleBook = new Book("B001", "Java Core", "Tác giả A", "CNTT", 3, 120_000L);
        sampleReader = new LecturerReader("R001", "Nguyễn Văn A", "0912345678");
    }

    @Test
    void chiTietSach_traVeLichSuMuonVaTongHopSoLieu() {
        BorrowTicket dangMuon = new BorrowTicket("BT001", "R001",
                LocalDate.now().minusDays(5), LocalDate.now().plusDays(2), null,
                TicketStatus.BORROWING, List.of(new BorrowTicketDetail("TD1", "BT001", "B001", 2)));
        BorrowTicket daTra = new BorrowTicket("BT002", "R001",
                LocalDate.now().minusDays(20), LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(9), TicketStatus.RETURNED,
                List.of(new BorrowTicketDetail("TD2", "BT002", "B001", 1)));

        when(bookRepository.findById("B001")).thenReturn(Optional.of(sampleBook));
        when(borrowTicketRepository.findAll()).thenReturn(List.of(dangMuon, daTra));
        when(readerRepository.findById("R001")).thenReturn(Optional.of(sampleReader));

        BookDetailResponse detail = bookService.getDetail("B001");

        assertEquals("B001", detail.getBookId());
        assertEquals(2, detail.getTimesBorrowed());
        assertEquals(3, detail.getTotalQuantityBorrowed());
        assertEquals(2, detail.getCurrentBorrowingQuantity());
        assertEquals(2, detail.getHistory().size());
        assertEquals("Nguyễn Văn A", detail.getHistory().get(0).getReaderName());
    }

    @Test
    void chiTietSach_khongTonTai_nemBookNotFoundException() {
        when(bookRepository.findById("B999")).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.getDetail("B999"));
    }

    @Test
    void thongKe_tinhDungTongSoBanSaoVaDanhSachTonKhoThap() {
        Book conNhieu = new Book("B001", "Java Core", "TG1", "CNTT", 5, 100_000L);
        Book sapHet = new Book("B002", "Clean Code", "TG2", "CNTT", 1, 150_000L);
        BorrowTicket dangMuon = new BorrowTicket("BT010", "R001",
                LocalDate.now().minusDays(3), LocalDate.now().plusDays(4), null,
                TicketStatus.BORROWING, List.of(new BorrowTicketDetail("TD10", "BT010", "B002", 2)));

        when(bookRepository.findAll()).thenReturn(List.of(conNhieu, sapHet));
        when(borrowTicketRepository.findAll()).thenReturn(List.of(dangMuon));

        BookStatisticsResponse stats = bookService.getStatistics();

        assertEquals(2, stats.getTotalTitles());
        assertEquals(6, stats.getTotalAvailableCopies());
        assertEquals(2, stats.getTotalBorrowedCopies());
        assertEquals(8, stats.getTotalCopies());
        assertEquals(1, stats.getLowStockBooks().size());
        assertEquals("B002", stats.getLowStockBooks().get(0).getBookId());
        assertFalse(stats.getTopBorrowedBooks().isEmpty());
        assertEquals("B002", stats.getTopBorrowedBooks().get(0).getBookId());
    }
}