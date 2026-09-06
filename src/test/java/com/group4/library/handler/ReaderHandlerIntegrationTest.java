package com.group4.library.handler;
import com.group4.library.main.LibraryApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.library.dto.ReaderRequest;
import com.group4.library.model.Book;
import com.group4.library.model.BorrowTicket;
import com.group4.library.model.BorrowTicketDetail;
import com.group4.library.model.TicketStatus;
import com.group4.library.repository.BookRepository;
import com.group4.library.repository.BorrowTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = LibraryApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReaderHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BorrowTicketRepository borrowTicketRepository;

    @MockBean
    private BookRepository bookRepository;

    @BeforeEach
    void chuanBiVaXoaDuLieuCu() throws Exception {
        Mockito.when(borrowTicketRepository.findByReaderIdAndStatus(
                        Mockito.anyString(), Mockito.any(TicketStatus.class)))
                .thenReturn(List.of());
        Mockito.when(borrowTicketRepository.findByReaderId(Mockito.anyString()))
                .thenReturn(List.of());
        Mockito.when(bookRepository.findById(any())).thenReturn(Optional.empty());

        String body = mockMvc.perform(get("/api/readers").param("size", "1000"))
                .andReturn().getResponse().getContentAsString();
        var content = objectMapper.readTree(body).get("content");
        for (var node : content) {
            mockMvc.perform(delete("/api/readers/" + node.get("id").asText()));
        }
    }

    @Test
    void themBanDoc_traVe201VaDungDuLieu() throws Exception {
        ReaderRequest request = buildRequest("HT001", "Nguyễn Văn A", "0912345678", "STUDENT");

        mockMvc.perform(post("/api/readers")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("HT001"))
                .andExpect(jsonPath("$.maxBorrowLimit").value(3));
    }

    @Test
    void themBanDoc_maTrung_traVe400() throws Exception {
        ReaderRequest request = buildRequest("HT002", "Nguyễn Văn A", "0912345678", "STUDENT");
        mockMvc.perform(post("/api/readers")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(post("/api/readers")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Mã bạn đọc đã tồn tại: HT002"));
    }

    @Test
    void themBanDoc_tenRong_traVe400() throws Exception {
        ReaderRequest request = buildRequest("HT003", "   ", "0912345678", "STUDENT");

        mockMvc.perform(post("/api/readers")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Họ tên không được để trống"));
    }

    @Test
    void themBanDoc_sdtSaiDinhDang_traVe400() throws Exception {
        ReaderRequest request = buildRequest("HT004", "Nguyễn Văn A", "abc", "STUDENT");

        mockMvc.perform(post("/api/readers")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void layChiTiet_khongTonTai_traVe404() throws Exception {
        mockMvc.perform(get("/api/readers/KHONG_TON_TAI"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Không tìm thấy bạn đọc: KHONG_TON_TAI"));
    }

    @Test
    void danhSach_theoLoai_locDungKetQua() throws Exception {
        mockMvc.perform(post("/api/readers").contentType("application/json")
                .content(objectMapper.writeValueAsString(buildRequest("HT005", "Nguyễn Văn A", "0912345678", "STUDENT"))));
        mockMvc.perform(post("/api/readers").contentType("application/json")
                .content(objectMapper.writeValueAsString(buildRequest("HT006", "Trần Thị B", "0987654321", "LECTURER"))));

        mockMvc.perform(get("/api/readers").param("type", "LECTURER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value("HT006"));
    }

    @Test
    void suaBanDoc_traVeDungDuLieuMoi() throws Exception {
        mockMvc.perform(post("/api/readers").contentType("application/json")
                .content(objectMapper.writeValueAsString(buildRequest("HT007", "Nguyễn Văn A", "0912345678", "STUDENT"))));

        ReaderRequest capNhat = buildRequest(null, "Nguyễn Văn A Sửa", "0999999999", "LECTURER");

        mockMvc.perform(put("/api/readers/HT007")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(capNhat)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nguyễn Văn A Sửa"))
                .andExpect(jsonPath("$.maxBorrowLimit").value(7));
    }

    @Test
    void suaBanDoc_khongTonTai_traVe404() throws Exception {
        ReaderRequest capNhat = buildRequest(null, "Nguyễn Văn A", "0912345678", "STUDENT");

        mockMvc.perform(put("/api/readers/KHONG_TON_TAI")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(capNhat)))
                .andExpect(status().isNotFound());
    }

    @Test
    void xoaBanDoc_traVe204_vaKhongConTrongDanhSach() throws Exception {
        mockMvc.perform(post("/api/readers").contentType("application/json")
                .content(objectMapper.writeValueAsString(buildRequest("HT008", "Nguyễn Văn A", "0912345678", "STUDENT"))));

        mockMvc.perform(delete("/api/readers/HT008"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/readers/HT008"))
                .andExpect(status().isNotFound());
    }

    @Test
    void xoaBanDoc_khongTonTai_traVe404() throws Exception {
        mockMvc.perform(delete("/api/readers/KHONG_TON_TAI"))
                .andExpect(status().isNotFound());
    }

    @Test
    void xoaBanDoc_conPhieuDangMuon_traVe400() throws Exception {
        mockMvc.perform(post("/api/readers").contentType("application/json")
                .content(objectMapper.writeValueAsString(buildRequest("HT009", "Nguyễn Văn A", "0912345678", "STUDENT"))));

        Mockito.when(borrowTicketRepository.findByReaderIdAndStatus(
                        Mockito.eq("HT009"), Mockito.any(TicketStatus.class)))
                .thenReturn(List.of(Mockito.mock(BorrowTicket.class)));

        mockMvc.perform(delete("/api/readers/HT009"))
                .andExpect(status().isBadRequest());
    }

    // ===================== Test mới cho Gói 2: /detail =====================

    @Test
    void layChiTiet_khongCoPhieu_traVeTongHopBangKhong() throws Exception {
        mockMvc.perform(post("/api/readers").contentType("application/json")
                .content(objectMapper.writeValueAsString(buildRequest("HT010", "Nguyễn Văn A", "0912345678", "STUDENT"))));

        mockMvc.perform(get("/api/readers/HT010/detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("HT010"))
                .andExpect(jsonPath("$.borrowSummary.currentlyBorrowedCount").value(0))
                .andExpect(jsonPath("$.borrowSummary.reachedLimit").value(false))
                .andExpect(jsonPath("$.borrowSummary.tickets.length()").value(0));
    }

    @Test
    void layChiTiet_coPhieuQuaHan_traVeDungThongTin() throws Exception {
        mockMvc.perform(post("/api/readers").contentType("application/json")
                .content(objectMapper.writeValueAsString(buildRequest("HT011", "Trần Thị B", "0987654321", "STUDENT"))));

        LocalDate homNay = LocalDate.now();
        BorrowTicket phieuQuaHan = new BorrowTicket("T001", "HT011", homNay.minusDays(20), homNay.minusDays(5),
                null, TicketStatus.BORROWING,
                List.of(new BorrowTicketDetail("D001", "T001", "B001", 1)));

        Mockito.when(borrowTicketRepository.findByReaderId("HT011")).thenReturn(List.of(phieuQuaHan));
        Mockito.when(bookRepository.findById("B001"))
                .thenReturn(Optional.of(new Book("B001", "Clean Code", "Robert C. Martin", "IT", 5, 150000L)));

        mockMvc.perform(get("/api/readers/HT011/detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.borrowSummary.overdueTicketCount").value(1))
                .andExpect(jsonPath("$.borrowSummary.tickets[0].overdue").value(true))
                .andExpect(jsonPath("$.borrowSummary.tickets[0].books[0].title").value("Clean Code"));
    }

    @Test
    void layChiTiet_banDocKhongTonTai_traVe404() throws Exception {
        mockMvc.perform(get("/api/readers/KHONG_TON_TAI/detail"))
                .andExpect(status().isNotFound());
    }

    // ===================== Test mới cho Gói 4: /statistics =====================

    @Test
    void layThongKe_khongCoBanDoc_traVeTatCaBangKhong() throws Exception {
        Mockito.when(borrowTicketRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/readers/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReaders").value(0))
                .andExpect(jsonPath("$.currentlyBorrowingReaderCount").value(0));
    }

    @Test
    void layThongKe_coBanDoc_demDungTheoLoai() throws Exception {
        Mockito.when(borrowTicketRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/api/readers").contentType("application/json")
                .content(objectMapper.writeValueAsString(buildRequest("HT012", "A", "0900000001", "STUDENT"))));
        mockMvc.perform(post("/api/readers").contentType("application/json")
                .content(objectMapper.writeValueAsString(buildRequest("HT013", "B", "0900000002", "LECTURER"))));

        mockMvc.perform(get("/api/readers/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReaders").value(2))
                .andExpect(jsonPath("$.countByType.STUDENT").value(1))
                .andExpect(jsonPath("$.countByType.LECTURER").value(1));
    }

    // ===================== Test mới cho Gói 3: /import, /export =====================

    @Test
    void importCsv_fileHopLe_traVe200VaTaoBanDoc() throws Exception {
        String csv = "id,name,phoneNumber,type\nHT014,Nguyễn Văn A,0912345678,STUDENT\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "readers.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/readers/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows").value(1))
                .andExpect(jsonPath("$.successCount").value(1));

        mockMvc.perform(get("/api/readers/HT014"))
                .andExpect(status().isOk());
    }

    @Test
    void importCsv_khongCoFile_traVe400() throws Exception {
        mockMvc.perform(multipart("/api/readers/import"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void importCsv_fileRong_traVe400() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/api/readers/import").file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Vui lòng chọn file CSV để import"));
    }

    @Test
    void exportCsv_traVeDungContentTypeVaDuLieu() throws Exception {
        mockMvc.perform(post("/api/readers").contentType("application/json")
                .content(objectMapper.writeValueAsString(buildRequest("HT015", "Nguyễn Văn A", "0912345678", "STUDENT"))));

        mockMvc.perform(get("/api/readers/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=readers.csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("HT015")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id,name,phoneNumber,type,maxBorrowLimit")));
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