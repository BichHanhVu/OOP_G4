package com.group4.library.service;

import com.group4.library.dto.PagedReaderResponse;
import com.group4.library.dto.ReaderRequest;
import com.group4.library.dto.ReaderResponse;
import com.group4.library.dto.ReaderSearchRequest;
import com.group4.library.exception.DuplicateReaderIdException;
import com.group4.library.exception.ReaderNotFoundException;
import com.group4.library.repository.BorrowTicketRepository;
import com.group4.library.repository.json.JsonReaderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReaderServiceIntegrationTest {

    private ReaderService readerService;
    private JsonReaderRepository repository;
    private BorrowTicketRepository borrowTicketRepository;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        repository = new JsonReaderRepository();
        File tempFile = new File(tempDir.toFile(), "readers-service-it.json");
        repository.setFilePathForTest(tempFile.getPath());

        borrowTicketRepository = Mockito.mock(BorrowTicketRepository.class);
        Mockito.when(borrowTicketRepository.findByReaderIdAndStatus(Mockito.anyString(), Mockito.any()))
                .thenReturn(List.of());

        readerService = new ReaderService(repository, borrowTicketRepository);
    }

    @Test
    void themBanDoc_ghiThatVaoFile_docLaiDungDuLieu() {
        readerService.create(buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));

        ReaderResponse fromDisk = readerService.getById("R001");

        assertEquals("Nguyễn Văn A", fromDisk.getName());
        assertEquals(3, fromDisk.getMaxBorrowLimit());
    }

    @Test
    void themBanDoc_maTrung_phatHienDungQuaFileThat() {
        readerService.create(buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));

        ReaderService serviceKhac = new ReaderService(repository, borrowTicketRepository);
        ReaderRequest trung = buildRequest("R001", "Trần Thị B", "0987654321", "LECTURER");

        assertThrows(DuplicateReaderIdException.class, () -> serviceKhac.create(trung));
    }

    @Test
    void suaBanDoc_ghiDeDungFile_khongTaoBanGhiThuHai() {
        readerService.create(buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));

        readerService.update("R001", buildRequest(null, "Nguyễn Văn A Sửa", "0999999999", "LECTURER"));

        PagedReaderResponse<ReaderResponse> tatCa = searchAll();
        assertEquals(1, tatCa.getTotalElements());
        assertEquals("Nguyễn Văn A Sửa", tatCa.getContent().get(0).getName());
    }

    @Test
    void xoaBanDoc_khongConTrongFile() {
        readerService.create(buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));

        readerService.delete("R001");

        assertTrue(searchAll().getContent().isEmpty());
        assertThrows(ReaderNotFoundException.class, () -> readerService.getById("R001"));
    }

    @Test
    void themNhieuBanDoc_lienTuc_luuDungThuTuVaSoLuong() {
        readerService.create(buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));
        readerService.create(buildRequest("R002", "Trần Thị B", "0987654321", "PRIORITY_STUDENT"));
        readerService.create(buildRequest("R003", "Lê Văn C", "0901112223", "LECTURER"));

        assertEquals(3, searchAll().getTotalElements());
    }

    @Test
    void khoiTaoServiceMoi_vanDocDuocDuLieuServiceTruocDaGhi() {
        readerService.create(buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));

        ReaderService serviceMoi = new ReaderService(repository, borrowTicketRepository);
        ReaderResponse ketQua = serviceMoi.getById("R001");

        assertEquals("Nguyễn Văn A", ketQua.getName());
    }

    private PagedReaderResponse<ReaderResponse> searchAll() {
        return readerService.search(new ReaderSearchRequest(null, null, null, null, null, 100));
    }

    private ReaderRequest buildRequest(String id, String name, String phone, String type) {
        ReaderRequest request = new ReaderRequest();
        request.setId(id);
        request.setName(name);
        request.setPhoneNumber(phone);
        request.setType(type);
        return request;
    }
}