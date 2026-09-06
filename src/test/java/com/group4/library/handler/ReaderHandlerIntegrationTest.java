package com.group4.library.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.library.dto.ReaderRequest;
import com.group4.library.model.TicketStatus;
import com.group4.library.repository.BorrowTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReaderHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BorrowTicketRepository borrowTicketRepository;

    @BeforeEach
    void chuanBiVaXoaDuLieuCu() throws Exception {
        Mockito.when(borrowTicketRepository.findByReaderIdAndStatus(
                        Mockito.anyString(), Mockito.any(TicketStatus.class)))
                .thenReturn(List.of());

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
                .thenReturn(List.of(Mockito.mock(com.group4.library.model.BorrowTicket.class)));

        mockMvc.perform(delete("/api/readers/HT009"))
                .andExpect(status().isBadRequest());
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