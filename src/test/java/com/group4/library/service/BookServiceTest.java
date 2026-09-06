package com.group4.library.service;

import com.group4.library.dto.BookRequest;
import com.group4.library.dto.BookResponse;
import com.group4.library.exception.BookNotFoundException;
import com.group4.library.model.Book;
import com.group4.library.model.BorrowTicket;
import com.group4.library.model.BorrowTicketDetail;
import com.group4.library.model.TicketStatus;
import com.group4.library.repository.BookRepository;
import com.group4.library.repository.BorrowTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * BookService trước đây chưa có test riêng — bộ test này bao phủ các nhánh
 * chính: tra cứu, thêm/sửa/xóa, điều chỉnh tồn kho, và xuất/nhập CSV.
 */
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowTicketRepository borrowTicketRepository;

    @InjectMocks
    private BookService bookService;

    private Book sampleBook;
    private BookRequest validRequest;

    @BeforeEach
    void setUp() {
        sampleBook = new Book("B001", "Java cơ bản", "Nguyễn Văn A", "Công nghệ", 5, 100_000L);
        validRequest = new BookRequest("B002", "Spring Boot", "Trần Văn B", "Công nghệ", 3, 150_000L);
    }

    // ===================== getAllBooks / getBookById =====================

    @Test
    void getAllBooks_traVeDanhSachDaChuyenDoi() {
        when(bookRepository.findAll()).thenReturn(List.of(sampleBook));

        List<BookResponse> result = bookService.getAllBooks();

        assertEquals(1, result.size());
        assertEquals("B001", result.get(0).getBookId());
        assertEquals(5, result.get(0).getAvailableQuantity());
    }

    @Test
    void getBookById_tonTai_traVeDung() {
        when(bookRepository.findById("B001")).thenReturn(Optional.of(sampleBook));

        BookResponse response = bookService.getBookById("B001");

        assertEquals("Java cơ bản", response.getTitle());
    }

    @Test
    void getBookById_khongTonTai_nemBookNotFoundException() {
        when(bookRepository.findById("B999")).thenReturn(Optional.empty());
        assertThrows(BookNotFoundException.class, () -> bookService.getBookById("B999"));
    }

    @Test
    void getBookById_maRong_nemIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> bookService.getBookById("   "));
        assertThrows(IllegalArgumentException.class, () -> bookService.getBookById(null));
    }

    // ===================== searchBooks =====================

    @Test
    void searchBooks_tuKhoaRong_traVeTatCa() {
        when(bookRepository.findAll()).thenReturn(List.of(sampleBook));

        List<BookResponse> result = bookService.searchBooks("   ");

        assertEquals(1, result.size());
        verify(bookRepository, never()).searchByIdOrTitle(any());
    }

    @Test
    void searchBooks_coTuKhoa_goiSearchByIdOrTitle() {
        when(bookRepository.searchByIdOrTitle("Java")).thenReturn(List.of(sampleBook));

        List<BookResponse> result = bookService.searchBooks("Java");

        assertEquals(1, result.size());
        verify(bookRepository).searchByIdOrTitle("Java");
    }

    // ===================== addBook =====================

    @Test
    void addBook_hopLe_luuThanhCong() {
        when(bookRepository.findById("B002")).thenReturn(Optional.empty());

        BookResponse response = bookService.addBook(validRequest);

        assertEquals("B002", response.getBookId());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void addBook_trungMaSach_nemIllegalArgumentException() {
        when(bookRepository.findById("B002")).thenReturn(Optional.of(sampleBook));
        assertThrows(IllegalArgumentException.class, () -> bookService.addBook(validRequest));
        verify(bookRepository, never()).save(any());
    }

    private static Stream<Arguments> duLieuSachKhongHopLe() {
        return Stream.of(
                arguments((Object) null, "request null"),
                arguments(new BookRequest(null, "Ten", "TG", "TL", 1, 1000L), "mã sách null"),
                arguments(new BookRequest("  ", "Ten", "TG", "TL", 1, 1000L), "mã sách trống"),
                arguments(new BookRequest("B010", null, "TG", "TL", 1, 1000L), "tên sách null"),
                arguments(new BookRequest("B010", "  ", "TG", "TL", 1, 1000L), "tên sách trống"),
                arguments(new BookRequest("B010", "Ten", "TG", "TL", null, 1000L), "số lượng null"),
                arguments(new BookRequest("B010", "Ten", "TG", "TL", -1, 1000L), "số lượng âm"),
                arguments(new BookRequest("B010", "Ten", "TG", "TL", 1, null), "giá null"),
                arguments(new BookRequest("B010", "Ten", "TG", "TL", 1, -1L), "giá âm")
        );
    }

    @ParameterizedTest(name = "[{index}] {1}")
    @MethodSource("duLieuSachKhongHopLe")
    void addBook_duLieuKhongHopLe_nemIllegalArgumentException(BookRequest request, String moTa) {
        assertThrows(IllegalArgumentException.class, () -> bookService.addBook(request), moTa);
    }

    // ===================== updateBook =====================

    @Test
    void updateBook_hopLe_capNhatThanhCong() {
        when(bookRepository.findById("B001")).thenReturn(Optional.of(sampleBook));
        when(bookRepository.update(any(Book.class))).thenReturn(true);

        BookResponse response = bookService.updateBook("B001",
                new BookRequest("B001", "Java nâng cao", "Tác giả mới", "Công nghệ", 10, 200_000L));

        assertEquals("Java nâng cao", response.getTitle());
        assertEquals(10, response.getAvailableQuantity());
    }

    @Test
    void updateBook_khongTonTai_nemBookNotFoundException() {
        when(bookRepository.findById("B999")).thenReturn(Optional.empty());
        assertThrows(BookNotFoundException.class,
                () -> bookService.updateBook("B999", validRequest));
    }

    @Test
    void updateBook_maRong_nemIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> bookService.updateBook("  ", validRequest));
    }

    @Test
    void updateBook_ghiThatBai_nemIllegalStateException() {
        when(bookRepository.findById("B001")).thenReturn(Optional.of(sampleBook));
        when(bookRepository.update(any(Book.class))).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> bookService.updateBook("B001", validRequest));
    }

    // ===================== adjustQuantity =====================

    @Test
    void adjustQuantity_tangSoLuong_thanhCong() {
        when(bookRepository.findById("B001")).thenReturn(Optional.of(sampleBook));

        BookResponse response = bookService.adjustQuantity("B001", 3);

        assertEquals(8, response.getAvailableQuantity());
        verify(bookRepository).update(sampleBook);
    }

    @Test
    void adjustQuantity_giamVuaHetKho_thanhCong() {
        when(bookRepository.findById("B001")).thenReturn(Optional.of(sampleBook));

        BookResponse response = bookService.adjustQuantity("B001", -5);

        assertEquals(0, response.getAvailableQuantity());
    }

    @Test
    void adjustQuantity_giamVuotQuaKho_nemIllegalArgumentException() {
        when(bookRepository.findById("B001")).thenReturn(Optional.of(sampleBook));
        assertThrows(IllegalArgumentException.class, () -> bookService.adjustQuantity("B001", -6));
    }

    @Test
    void adjustQuantity_khongTonTaiSach_nemBookNotFoundException() {
        when(bookRepository.findById("B999")).thenReturn(Optional.empty());
        assertThrows(BookNotFoundException.class, () -> bookService.adjustQuantity("B999", 1));
    }

    // ===================== deleteBook =====================

    @Test
    void deleteBook_khongDangMuon_xoaThanhCong() {
        when(bookRepository.findById("B001")).thenReturn(Optional.of(sampleBook));
        when(borrowTicketRepository.findByStatus(TicketStatus.BORROWING)).thenReturn(List.of());
        when(bookRepository.deleteByCode("B001")).thenReturn(true);

        assertDoesNotThrow(() -> bookService.deleteBook("B001"));
        verify(bookRepository).deleteByCode("B001");
    }

    @Test
    void deleteBook_dangNamTrongPhieuMuonChuaTra_nemIllegalStateException() {
        when(bookRepository.findById("B001")).thenReturn(Optional.of(sampleBook));
        BorrowTicket dangMuon = new BorrowTicket("BT001", "R001", LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 10), null, TicketStatus.BORROWING,
                List.of(new BorrowTicketDetail("TD001", "BT001", "B001", 1)));
        when(borrowTicketRepository.findByStatus(TicketStatus.BORROWING)).thenReturn(List.of(dangMuon));

        assertThrows(IllegalStateException.class, () -> bookService.deleteBook("B001"));
        verify(bookRepository, never()).deleteByCode(any());
    }

    @Test
    void deleteBook_khongTonTai_nemBookNotFoundException() {
        when(bookRepository.findById("B999")).thenReturn(Optional.empty());
        assertThrows(BookNotFoundException.class, () -> bookService.deleteBook("B999"));
    }

    @Test
    void deleteBook_maRong_nemIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> bookService.deleteBook(""));
    }

    @Test
    void deleteBook_xoaThatBaiOTangRepository_nemIllegalStateException() {
        when(bookRepository.findById("B001")).thenReturn(Optional.of(sampleBook));
        when(borrowTicketRepository.findByStatus(TicketStatus.BORROWING)).thenReturn(List.of());
        when(bookRepository.deleteByCode("B001")).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> bookService.deleteBook("B001"));
    }

    // ===================== exportBooks =====================

    @Test
    void exportBooks_sinhDungDinhDangCsv() {
        when(bookRepository.findAll()).thenReturn(List.of(sampleBook));

        String csv = bookService.exportBooks();

        assertTrue(csv.startsWith("\uFEFF"));
        assertTrue(csv.contains("Mã sách,Tên sách,Tác giả,Thể loại,Số lượng,Giá"));
        assertTrue(csv.contains("\"B001\",\"Java cơ bản\",\"Nguyễn Văn A\",\"Công nghệ\",5,100000"));
    }

    // ===================== importBooks =====================

    private InputStream csvStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void importBooks_hopLe_luuTatCaVaTraVeSoLuongDong() throws Exception {
        String csv = "Mã sách,Tên sách,Tác giả,Thể loại,Số lượng,Giá\n"
                + "\"B010\",\"Toán rời rạc\",\"Tác giả X\",\"Giáo trình\",4,90000\n"
                + "\"B011\",\"Xác suất thống kê\",\"Tác giả Y\",\"Giáo trình\",2,85000\n";
        when(bookRepository.findById("B010")).thenReturn(Optional.empty());
        when(bookRepository.findById("B011")).thenReturn(Optional.empty());

        int count = bookService.importBooks(csvStream(csv));

        assertEquals(2, count);
        verify(bookRepository, times(2)).save(any(Book.class));
    }

    @Test
    void importBooks_boQuaDongTrong_khongLoi() throws Exception {
        String csv = "Mã sách,Tên sách,Tác giả,Thể loại,Số lượng,Giá\n"
                + "\n"
                + "\"B010\",\"Toán rời rạc\",\"Tác giả X\",\"Giáo trình\",4,90000\n";
        when(bookRepository.findById("B010")).thenReturn(Optional.empty());

        int count = bookService.importBooks(csvStream(csv));

        assertEquals(1, count);
    }

    @Test
    void importBooks_thieuCot_nemIllegalArgumentException() {
        String csv = "Mã sách,Tên sách,Tác giả,Thể loại,Số lượng,Giá\n"
                + "\"B010\",\"Thiếu cột\",\"Tác giả X\"\n";

        assertThrows(IllegalArgumentException.class, () -> bookService.importBooks(csvStream(csv)));
    }

    @Test
    void importBooks_soLuongKhongPhaiSo_nemIllegalArgumentException() {
        String csv = "Mã sách,Tên sách,Tác giả,Thể loại,Số lượng,Giá\n"
                + "\"B010\",\"Ten\",\"TG\",\"TL\",\"abc\",90000\n";

        assertThrows(IllegalArgumentException.class, () -> bookService.importBooks(csvStream(csv)));
    }

    @Test
    void importBooks_trungMaTrongFile_nemIllegalArgumentException() {
        String csv = "Mã sách,Tên sách,Tác giả,Thể loại,Số lượng,Giá\n"
                + "\"B010\",\"Ten1\",\"TG\",\"TL\",1,1000\n"
                + "\"B010\",\"Ten2\",\"TG\",\"TL\",2,2000\n";
        when(bookRepository.findById("B010")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> bookService.importBooks(csvStream(csv)));
    }

    @Test
    void importBooks_trungMaTrongCoSoDuLieu_nemIllegalArgumentException() {
        String csv = "Mã sách,Tên sách,Tác giả,Thể loại,Số lượng,Giá\n"
                + "\"B001\",\"Ten\",\"TG\",\"TL\",1,1000\n";
        when(bookRepository.findById("B001")).thenReturn(Optional.of(sampleBook));

        assertThrows(IllegalArgumentException.class, () -> bookService.importBooks(csvStream(csv)));
        verify(bookRepository, never()).save(any());
    }

    @Test
    void importBooks_boDauBOMOHeader_khongAnhHuongKetQua() throws Exception {
        String csv = "\uFEFFMã sách,Tên sách,Tác giả,Thể loại,Số lượng,Giá\n"
                + "\"B010\",\"Ten\",\"TG\",\"TL\",1,1000\n";
        when(bookRepository.findById("B010")).thenReturn(Optional.empty());

        int count = bookService.importBooks(csvStream(csv));

        assertEquals(1, count);
    }
}