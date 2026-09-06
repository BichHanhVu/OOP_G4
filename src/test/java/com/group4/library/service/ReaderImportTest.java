package com.group4.library.service;

import com.group4.library.dto.ReaderImportSummary;
import com.group4.library.exception.BusinessException;
import com.group4.library.model.LecturerReader;
import com.group4.library.model.StudentReader;
import com.group4.library.repository.BorrowTicketRepository;
import com.group4.library.utils.CsvUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReaderImportTest {

    private ReaderService readerService;
    private ReaderCsvService readerCsvService;
    private InMemoryReaderRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryReaderRepository();
        BorrowTicketRepository borrowTicketRepository = Mockito.mock(BorrowTicketRepository.class);
        readerService = new ReaderService(repository, borrowTicketRepository);
        readerCsvService = new ReaderCsvService(readerService);
    }

    @Test
    void importCsv_fileDung_tatCaDongThanhCong() {
        String csv = "id,name,phoneNumber,type\n"
                + "R001,Nguyễn Văn A,0912345678,STUDENT\n"
                + "R002,Trần Thị B,0987654321,LECTURER\n";

        ReaderImportSummary summary = readerCsvService.importFromCsv(toStream(csv));

        assertEquals(2, summary.getTotalRows());
        assertEquals(2, summary.getSuccessCount());
        assertEquals(0, summary.getFailureCount());
        assertTrue(repository.existsById("R001"));
        assertTrue(repository.existsById("R002"));
    }

    @Test
    void importCsv_khongTruyenId_tuSinhMaChoDong() {
        String csv = "id,name,phoneNumber,type\n"
                + ",Nguyễn Văn A,0912345678,STUDENT\n";

        ReaderImportSummary summary = readerCsvService.importFromCsv(toStream(csv));

        assertEquals(1, summary.getSuccessCount());
        assertEquals("R001", summary.getResults().get(0).getReader().getId());
    }

    @Test
    void importCsv_rong_khongCoDongNaoSauHeader() {
        String csv = "id,name,phoneNumber,type\n";

        ReaderImportSummary summary = readerCsvService.importFromCsv(toStream(csv));

        assertEquals(0, summary.getTotalRows());
    }

    @Test
    void importCsv_fileHoanToanRong_nemBusinessExceptionDoThieuHeader() {
        InputStream empty = toStream("");

        assertThrows(BusinessException.class, () -> readerCsvService.importFromCsv(empty));
    }

    @Test
    void importCsv_headerSaiThuTu_nemBusinessException() {
        String csv = "name,id,phoneNumber,type\n"
                + "Nguyễn Văn A,R001,0912345678,STUDENT\n";
        InputStream stream = toStream(csv);

        assertThrows(BusinessException.class, () -> readerCsvService.importFromCsv(stream));
    }

    @Test
    void importCsv_headerThieuCot_nemBusinessException() {
        String csv = "id,name,phoneNumber\n"
                + "R001,Nguyễn Văn A,0912345678\n";
        InputStream stream = toStream(csv);

        assertThrows(BusinessException.class, () -> readerCsvService.importFromCsv(stream));
    }

    @Test
    void importCsv_motDongThieuCot_dongDoLoiCacDongKhacVanThanhCong() {
        String csv = "id,name,phoneNumber,type\n"
                + "R001,Nguyễn Văn A,0912345678,STUDENT\n"
                + "R002,Thiếu cột\n"
                + "R003,Lê Văn C,0901112223,LECTURER\n";

        ReaderImportSummary summary = readerCsvService.importFromCsv(toStream(csv));

        assertEquals(3, summary.getTotalRows());
        assertEquals(2, summary.getSuccessCount());
        assertEquals(1, summary.getFailureCount());
        assertFalse(summary.getResults().get(1).isSuccess());
        assertTrue(repository.existsById("R001"));
        assertTrue(repository.existsById("R003"));
    }

    @Test
    void importCsv_loaiBanDocSai_dongDoLoiCacDongKhacVanThanhCong() {
        String csv = "id,name,phoneNumber,type\n"
                + "R001,Nguyễn Văn A,0912345678,STUDENT\n"
                + "R002,Trần Thị B,0987654321,UNKNOWN_TYPE\n";

        ReaderImportSummary summary = readerCsvService.importFromCsv(toStream(csv));

        assertEquals(1, summary.getSuccessCount());
        assertEquals(1, summary.getFailureCount());
        assertTrue(repository.existsById("R001"));
        assertFalse(repository.existsById("R002"));
    }

    @Test
    void importCsv_maTrungVoiBanGhiDaCoTrongHeThong_dongDoLoiKhongAnhHuongDongKhac() {
        repository.save(new StudentReader("R001", "Bạn đọc cũ", "0900000000"));

        String csv = "id,name,phoneNumber,type\n"
                + "R001,Nguyễn Văn A,0912345678,STUDENT\n"
                + "R002,Trần Thị B,0987654321,LECTURER\n";

        ReaderImportSummary summary = readerCsvService.importFromCsv(toStream(csv));

        assertEquals(1, summary.getSuccessCount());
        assertEquals(1, summary.getFailureCount());
        assertEquals("Bạn đọc cũ", repository.findById("R001").get().getName());
        assertTrue(repository.existsById("R002"));
    }

    @Test
    void importCsv_maTrungGiuaHaiDongTrongCungFile_dongThuHaiBaoLoi() {
        String csv = "id,name,phoneNumber,type\n"
                + "R001,Nguyễn Văn A,0912345678,STUDENT\n"
                + "R001,Trần Thị B,0987654321,LECTURER\n";

        ReaderImportSummary summary = readerCsvService.importFromCsv(toStream(csv));

        assertEquals(1, summary.getSuccessCount());
        assertEquals(1, summary.getFailureCount());
        assertEquals("Nguyễn Văn A", repository.findById("R001").get().getName());
    }

    @Test
    void importCsv_sdtSaiDinhDang_dongDoLoiKhongAnhHuongDongKhac() {
        String csv = "id,name,phoneNumber,type\n"
                + "R001,Nguyễn Văn A,abc,STUDENT\n"
                + "R002,Trần Thị B,0987654321,LECTURER\n";

        ReaderImportSummary summary = readerCsvService.importFromCsv(toStream(csv));

        assertEquals(1, summary.getSuccessCount());
        assertEquals(1, summary.getFailureCount());
        assertFalse(repository.existsById("R001"));
        assertTrue(repository.existsById("R002"));
    }

    @Test
    void importCsv_tenChuaDauPhayVaNgoacKep_vanDocDungNhoEscape() {
        String ten = CsvUtils.escapeField("Nguyễn, \"Ba\" Văn A");
        String csv = "id,name,phoneNumber,type\n"
                + "R001," + ten + ",0912345678,STUDENT\n";

        ReaderImportSummary summary = readerCsvService.importFromCsv(toStream(csv));

        assertEquals(1, summary.getSuccessCount());
        assertEquals("Nguyễn, \"Ba\" Văn A", repository.findById("R001").get().getName());
    }

    @Test
    void exportCsv_traVeDungHeaderVaDuLieu() {
        readerService.create(buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));
        readerService.create(buildRequest("R002", "Trần Thị B", "0987654321", "LECTURER"));

        String csv = readerCsvService.exportToCsv();
        List<String> lines = List.of(csv.split("\n"));

        assertEquals("id,name,phoneNumber,type,maxBorrowLimit", lines.get(0).trim());
        assertEquals(3, lines.size());
        assertTrue(lines.get(1).contains("R001"));
        assertTrue(lines.get(1).contains("Nguyễn Văn A"));
        assertTrue(lines.get(1).contains("3"));
    }

    @Test
    void exportCsv_khongCoBanDoc_chiCoDongHeader() {
        String csv = readerCsvService.exportToCsv();

        assertEquals(1, csv.split("\n").length);
    }

    @Test
    void exportRoiImportLai_khongMatDuLieuHoacSaiLoaiBanDoc() {
        readerService.create(buildRequest("R001", "Nguyễn Văn A, Bí danh", "0912345678", "LECTURER"));

        String exported = readerCsvService.exportToCsv();

        InMemoryReaderRepository repositoryMoi = new InMemoryReaderRepository();
        ReaderService serviceMoi = new ReaderService(repositoryMoi, Mockito.mock(BorrowTicketRepository.class));
        ReaderCsvService csvServiceMoi = new ReaderCsvService(serviceMoi);

        // exportToCsv có thêm cột maxBorrowLimit ở cuối (không dùng khi import lại) -> cắt về đúng 4 cột đầu
        String[] lines = exported.split("\n");
        StringBuilder importable = new StringBuilder("id,name,phoneNumber,type\n");
        for (int i = 1; i < lines.length; i++) {
            List<String> fields = CsvUtils.parseLine(lines[i]);
            importable.append(String.join(",",
                            CsvUtils.escapeField(fields.get(0)),
                            CsvUtils.escapeField(fields.get(1)),
                            CsvUtils.escapeField(fields.get(2)),
                            CsvUtils.escapeField(fields.get(3))))
                    .append("\n");
        }

        ReaderImportSummary summary = csvServiceMoi.importFromCsv(toStream(importable.toString()));

        assertEquals(1, summary.getSuccessCount());
        var reader = repositoryMoi.findById("R001").get();
        assertEquals("Nguyễn Văn A, Bí danh", reader.getName());
        assertEquals(7, reader.getMaxBorrowLimit());
        assertTrue(reader instanceof LecturerReader);
    }

    private InputStream toStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    private com.group4.library.dto.ReaderRequest buildRequest(String id, String name, String phone, String type) {
        com.group4.library.dto.ReaderRequest request = new com.group4.library.dto.ReaderRequest();
        request.setId(id);
        request.setName(name);
        request.setPhoneNumber(phone);
        request.setType(type);
        return request;
    }
}