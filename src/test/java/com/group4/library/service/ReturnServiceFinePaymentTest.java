package com.group4.library.service;

import com.group4.library.dto.ReturnResponse;
import com.group4.library.exception.FineAlreadyPaidException;
import com.group4.library.exception.ReturnRecordNotFoundException;
import com.group4.library.model.ReturnRecord;
import com.group4.library.policy.FinePolicyFactory;
import com.group4.library.repository.BookRepository;
import com.group4.library.repository.BorrowTicketRepository;
import com.group4.library.repository.ReaderRepository;
import com.group4.library.repository.ReturnRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test cho nghiệp vụ thanh toán tiền phạt (payFine) và tra cứu các khoản
 * phạt chưa thanh toán (getUnpaidFines) trong ReturnService.
 */
@ExtendWith(MockitoExtension.class)
class ReturnServiceFinePaymentTest {

    @Mock
    private ReaderRepository readerRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BorrowTicketRepository ticketRepository;
    @Mock
    private ReturnRecordRepository returnRepository;
    @Mock
    private FinePolicyFactory finePolicyFactory;

    @InjectMocks
    private ReturnService returnService;

    @Test
    void thanhToanTienPhat_thanhCong_danhDauDaTraVaGhiNhanNgayTra() {
        ReturnRecord record = new ReturnRecord("RT001", "BT001", LocalDate.of(2026, 8, 13), 3, 15_000L);
        when(returnRepository.findById("RT001")).thenReturn(Optional.of(record));
        when(returnRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ReturnResponse response = returnService.payFine("RT001");

        assertTrue(response.isPaid());
        assertEquals(LocalDate.now(), response.getPaidDate());

        ArgumentCaptor<ReturnRecord> captor = ArgumentCaptor.forClass(ReturnRecord.class);
        verify(returnRepository).save(captor.capture());
        assertTrue(captor.getValue().isPaid());
    }

    @Test
    void thanhToanTienPhat_khongTimThayPhieuTra_nemReturnRecordNotFoundException() {
        when(returnRepository.findById("RT404")).thenReturn(Optional.empty());

        assertThrows(ReturnRecordNotFoundException.class, () -> returnService.payFine("RT404"));
        verify(returnRepository, never()).save(any());
    }

    @Test
    void thanhToanTienPhat_daThanhToanTruocDo_nemFineAlreadyPaidException() {
        ReturnRecord record = new ReturnRecord("RT002", "BT002", LocalDate.now(), 1, 5_000L);
        record.setPaid(true);
        record.setPaidDate(LocalDate.now().minusDays(1));
        when(returnRepository.findById("RT002")).thenReturn(Optional.of(record));

        assertThrows(FineAlreadyPaidException.class, () -> returnService.payFine("RT002"));
        verify(returnRepository, never()).save(any());
    }

    @Test
    void layDanhSachChuaThanhToan_chiTraVeBanGhiCoTienPhatVaChuaTra() {
        ReturnRecord chuaTra = new ReturnRecord("RT010", "BT010", LocalDate.now(), 2, 10_000L);
        ReturnRecord daTra = new ReturnRecord("RT011", "BT011", LocalDate.now(), 2, 10_000L);
        daTra.setPaid(true);
        ReturnRecord khongPhat = new ReturnRecord("RT012", "BT012", LocalDate.now(), 0, 0L);
        when(returnRepository.findAll()).thenReturn(List.of(chuaTra, daTra, khongPhat));

        List<ReturnResponse> unpaid = returnService.getUnpaidFines();

        assertEquals(1, unpaid.size());
        assertEquals("RT010", unpaid.get(0).getReturnId());
    }
}