package com.group4.library.service;

import com.group4.library.dto.PagedReaderResponse;
import com.group4.library.dto.ReaderRequest;
import com.group4.library.dto.ReaderResponse;
import com.group4.library.dto.ReaderSearchRequest;
import com.group4.library.exception.DuplicateReaderIdException;
import com.group4.library.exception.EmptyReaderNameException;
import com.group4.library.exception.InvalidPhoneNumberException;
import com.group4.library.exception.InvalidReaderTypeException;
import com.group4.library.exception.ReaderNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group4.library.repository.BorrowTicketRepository;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReaderServiceTest {

    private ReaderService readerService;
    private InMemoryReaderRepository repository;
    private BorrowTicketRepository borrowTicketRepository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryReaderRepository();
        borrowTicketRepository = Mockito.mock(BorrowTicketRepository.class);

        readerService = new ReaderService(
                repository,
                borrowTicketRepository
        );
    }

    @Test
    void themBanDoc_thanhCong() {
        ReaderResponse response = readerService.create(
                buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));

        assertEquals("R001", response.getId());
        assertEquals("Nguyễn Văn A", response.getName());
        assertEquals(3, response.getMaxBorrowLimit());
        assertTrue(repository.existsById("R001"));
    }

    @Test
    void themBanDoc_khongTruyenId_sinhMaKeTiepDungTheoDuLieuHienCo() {
        readerService.create(buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));
        readerService.create(buildRequest("R002", "Trần Thị B", "0987654321", "PRIORITY_STUDENT"));
        readerService.create(buildRequest("R003", "Lê Văn C", "0901112223", "LECTURER"));

        ReaderResponse response = readerService.create(
                buildRequest(null, "Phạm Thị D", "0909998887", "STUDENT"));

        assertEquals("R004", response.getId());
    }

    @Test
    void themBanDoc_maTrung_nemDuplicateReaderIdException() {
        readerService.create(buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));

        ReaderRequest trung = buildRequest("R001", "Trần Thị B", "0987654321", "LECTURER");

        assertThrows(DuplicateReaderIdException.class, () -> readerService.create(trung));
    }

    @Test
    void themBanDoc_tenRong_nemEmptyReaderNameException() {
        ReaderRequest request = buildRequest(null, "   ", "0912345678", "STUDENT");

        assertThrows(EmptyReaderNameException.class, () -> readerService.create(request));
    }

    @Test
    void themBanDoc_sdtSaiDinhDang_nemInvalidPhoneNumberException() {
        ReaderRequest request = buildRequest(null, "Nguyễn Văn A", "abc123", "STUDENT");

        assertThrows(InvalidPhoneNumberException.class, () -> readerService.create(request));
    }

    @Test
    void themBanDoc_loaiKhongHopLe_nemInvalidReaderTypeException() {
        ReaderRequest request = buildRequest(null, "Nguyễn Văn A", "0912345678", "UNKNOWN");

        assertThrows(InvalidReaderTypeException.class, () -> readerService.create(request));
    }

    @Test
    void timKiem_theoTen_dungKetQua() {
        readerService.create(buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));
        readerService.create(buildRequest("R002", "Trần Thị B", "0987654321", "LECTURER"));

        List<ReaderResponse> ketQua = search("văn a", null).getContent();

        assertEquals(1, ketQua.size());
        assertEquals("R001", ketQua.get(0).getId());
    }

    @Test
    void timKiem_theoMa_dungKetQua() {
        readerService.create(buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));
        readerService.create(buildRequest("R002", "Trần Thị B", "0987654321", "LECTURER"));

        List<ReaderResponse> ketQua = search("R002", null).getContent();

        assertEquals(1, ketQua.size());
        assertEquals("R002", ketQua.get(0).getId());
    }

    @Test
    void timKiem_theoLoai_dungKetQua() {
        readerService.create(buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));
        readerService.create(buildRequest("R002", "Trần Thị B", "0987654321", "LECTURER"));

        List<ReaderResponse> ketQua = search(null, "LECTURER").getContent();

        assertEquals(1, ketQua.size());
        assertEquals("R002", ketQua.get(0).getId());
    }

    @Test
    void timKiem_khongCoDieuKien_traVeTatCa() {
        readerService.create(buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));
        readerService.create(buildRequest("R002", "Trần Thị B", "0987654321", "LECTURER"));

        List<ReaderResponse> ketQua = search(null, null).getContent();

        assertEquals(2, ketQua.size());
    }

    @Test
    void suaBanDoc_dungKetQua() {
        readerService.create(buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));

        readerService.update("R001",
                buildRequest(null, "Nguyễn Văn A Sửa", "0999999999", "PRIORITY_STUDENT"));

        ReaderResponse updated = readerService.getById("R001");
        assertEquals("Nguyễn Văn A Sửa", updated.getName());
        assertEquals("PRIORITY_STUDENT", updated.getType());
        assertEquals(5, updated.getMaxBorrowLimit());
    }

    @Test
    void suaBanDoc_khongTonTai_nemReaderNotFoundException() {
        ReaderRequest request = buildRequest(null, "Nguyễn Văn A", "0912345678", "STUDENT");

        assertThrows(ReaderNotFoundException.class, () -> readerService.update("R999", request));
    }

    @Test
    void xoaBanDoc_dungKetQua() {
        readerService.create(buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));

        readerService.delete("R001");

        assertThrows(ReaderNotFoundException.class, () -> readerService.getById("R001"));
    }

    @Test
    void xoaBanDoc_khongTonTai_nemReaderNotFoundException() {
        assertThrows(ReaderNotFoundException.class, () -> readerService.delete("R999"));
    }

    @Test
    void layChiTiet_khongTonTai_nemReaderNotFoundException() {
        assertThrows(ReaderNotFoundException.class, () -> readerService.getById("R999"));
    }

    private ReaderRequest buildRequest(String id, String name, String phone, String type) {
        ReaderRequest request = new ReaderRequest();
        request.setId(id);
        request.setName(name);
        request.setPhoneNumber(phone);
        request.setType(type);
        return request;
    }

    private PagedReaderResponse<ReaderResponse> search(String keyword, String type) {
        return readerService.search(new ReaderSearchRequest(keyword, type, null, null, null, 100));
    }
    @Test
    void xoaBanDoc_conPhieuDangMuon_nemBusinessException() {
        readerService.create(buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));

        Mockito.when(borrowTicketRepository.findByReaderIdAndStatus(
                        Mockito.eq("R001"), Mockito.any()))
                .thenReturn(List.of(Mockito.mock(com.group4.library.model.BorrowTicket.class)));

        assertThrows(com.group4.library.exception.BusinessException.class,
                () -> readerService.delete("R001"));
    }

    @Test
    void xoaBanDoc_khongConPhieuDangMuon_xoaThanhCong() {
        readerService.create(buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));

        Mockito.when(borrowTicketRepository.findByReaderIdAndStatus(
                        Mockito.eq("R001"), Mockito.any()))
                .thenReturn(List.of());

        readerService.delete("R001");

        assertThrows(ReaderNotFoundException.class, () -> readerService.getById("R001"));
    }
}