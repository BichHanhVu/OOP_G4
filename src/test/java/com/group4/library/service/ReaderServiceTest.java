//package com.group4.library.service;
//
//import com.group4.library.dto.ReaderRequest;
//import com.group4.library.dto.ReaderResponse;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//// Import JUnit 5 (JUnit Jupiter)
//import org.junit.jupiter.api.Test;
//import static org.junit.jupiter.api.Assertions.*;
//
//// Import các Exception custom của Duyên
//import com.group4.library.exception.*;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//class ReaderServiceTest {
//
//    private ReaderService readerService;
//    private InMemoryReaderRepository repository;
//
//    @BeforeEach
//    void setUp() {
//        repository = new InMemoryReaderRepository();
//        readerService = new ReaderService(repository);
//    }
//
//    @Test
//    void themBanDoc_thanhCong() {
//        ReaderResponse response = readerService.create(
//                buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));
//
//        assertEquals("R001", response.getId());
//        assertEquals("Nguyễn Văn A", response.getName());
//        assertEquals(3, response.getMaxBorrowLimit());
//        assertTrue(repository.existsById("R001"));
//    }
//
//    @Test
//    void themBanDoc_maTrung_baoLoi() {
//        readerService.create(buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));
//
//        ReaderRequest trung = buildRequest("R001", "Trần Thị B", "0987654321", "LECTURER");
//
//        BusinessException ex = assertThrows(BusinessException.class,
//                () -> readerService.create(trung));
//        assertTrue(ex.getMessage().contains("đã tồn tại"));
//    }
//
//    @Test
//    void themBanDoc_tenRong_baoLoi() {
//        ReaderRequest request = buildRequest(null, "   ", "0912345678", "STUDENT");
//
//        assertThrows(BusinessException.class, () -> readerService.create(request));
//    }
//
//    @Test
//    void themBanDoc_sdtSaiDinhDang_baoLoi() {
//        ReaderRequest request = buildRequest(null, "Nguyễn Văn A", "abc123", "STUDENT");
//
//        assertThrows(BusinessException.class, () -> readerService.create(request));
//    }
//
//    @Test
//    void timKiem_dungKetQua() {
//        readerService.create(buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));
//        readerService.create(buildRequest("R002", "Trần Thị B", "0987654321", "LECTURER"));
//
//        List<ReaderResponse> byName = readerService.getAll("văn a", null);
//        List<ReaderResponse> byId = readerService.getAll("R002", null);
//        List<ReaderResponse> byType = readerService.getAll(null, "LECTURER");
//
//        assertEquals(1, byName.size());
//        assertEquals("R001", byName.get(0).getId());
//        assertEquals("R002", byId.get(0).getId());
//        assertEquals(1, byType.size());
//        assertEquals("R002", byType.get(0).getId());
//    }
//
//    @Test
//    void suaBanDoc_dungKetQua() {
//        readerService.create(buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));
//
//        readerService.update("R001",
//                buildRequest(null, "Nguyễn Văn A Sửa", "0999999999", "PRIORITY_STUDENT"));
//
//        ReaderResponse updated = readerService.getById("R001");
//        assertEquals("Nguyễn Văn A Sửa", updated.getName());
//        assertEquals("PRIORITY_STUDENT", updated.getType());
//        assertEquals(5, updated.getMaxBorrowLimit());
//    }
//
//    @Test
//    void xoaBanDoc_dungKetQua() {
//        readerService.create(buildRequest("R001", "Nguyễn Văn A", "0912345678", "STUDENT"));
//
//        readerService.delete("R001");
//
//        assertThrows(ResourceNotFoundException.class, () -> readerService.getById("R001"));
//    }
//
//    private ReaderRequest buildRequest(String id, String name, String phone, String type) {
//        ReaderRequest request = new ReaderRequest();
//        request.setId(id);
//        request.setName(name);
//        request.setPhoneNumber(phone);
//        request.setType(type);
//        return request;
//    }
//}
