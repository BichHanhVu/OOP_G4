package com.group4.library.service;

import com.group4.library.dto.BorrowTicketResponse;
import com.group4.library.dto.RenewTicketRequest;
import com.group4.library.exception.InvalidBorrowDateException;
import com.group4.library.exception.ResourceNotFoundException;
import com.group4.library.exception.TicketCancelNotAllowedException;
import com.group4.library.exception.TicketRenewalNotAllowedException;
import com.group4.library.model.Book;
import com.group4.library.model.BorrowTicket;
import com.group4.library.model.BorrowTicketDetail;
import com.group4.library.model.TicketStatus;
import com.group4.library.repository.BookRepository;
import com.group4.library.repository.BorrowTicketRepository;
import com.group4.library.repository.ReaderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * Test cho hai nghiệp vụ mới của BorrowService: hủy phiếu mượn (cancelTicket)
 * và gia hạn phiếu mượn (renewTicket).
 */
@ExtendWith(MockitoExtension.class)
class BorrowServiceCancelRenewTest {

    @Mock
    private BorrowTicketRepository borrowTicketRepository;
    @Mock
    private ReaderRepository readerRepository;
    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BorrowService borrowService;

    private BorrowTicket borrowingTicket(LocalDate dueDate, int renewalCount) {
        BorrowTicket ticket = new BorrowTicket("BT001", "R001", dueDate.minusDays(7), dueDate,
                null, TicketStatus.BORROWING,
                List.of(new BorrowTicketDetail("TD001", "BT001", "B001", 2)));
        ticket.setRenewalCount(renewalCount);
        return ticket;
    }

    @Test
    void huyPhieu_thanhCong_hoanKhoVaDoiTrangThaiThanhCancelled() {
        BorrowTicket ticket = borrowingTicket(LocalDate.now().plusDays(3), 0);
        Book book = new Book("B001", "Java Core", "Tác giả", "CNTT", 1, 100_000L);

        when(borrowTicketRepository.findById("BT001")).thenReturn(Optional.of(ticket));
        when(bookRepository.findById("B001")).thenReturn(Optional.of(book));
        when(bookRepository.update(any())).thenReturn(true);
        when(borrowTicketRepository.save(any())).thenReturn(ticket);

        BorrowTicketResponse response = borrowService.cancelTicket("BT001");

        assertEquals(TicketStatus.CANCELLED, response.getStatus());
        assertEquals(3, book.getAvailableQuantity());
        verify(bookRepository).update(book);
        verify(borrowTicketRepository).save(ticket);
    }

    @Test
    void huyPhieu_khongTonTai_nemResourceNotFoundException() {
        when(borrowTicketRepository.findById("BT404")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> borrowService.cancelTicket("BT404"));
        verifyNoInteractions(bookRepository);
    }

    @Test
    void huyPhieu_phieuDaTra_khongChoPhepHuy() {
        BorrowTicket ticket = borrowingTicket(LocalDate.now().plusDays(3), 0);
        ticket.setStatus(TicketStatus.RETURNED);
        when(borrowTicketRepository.findById("BT001")).thenReturn(Optional.of(ticket));

        assertThrows(TicketCancelNotAllowedException.class, () -> borrowService.cancelTicket("BT001"));
        verifyNoInteractions(bookRepository);
    }

    @Test
    void giaHan_thanhCong_capNhatHanTraVaTangSoLanGiaHan() {
        LocalDate currentDueDate = LocalDate.now().plusDays(2);
        BorrowTicket ticket = borrowingTicket(currentDueDate, 0);
        when(borrowTicketRepository.findById("BT001")).thenReturn(Optional.of(ticket));
        when(borrowTicketRepository.save(any())).thenReturn(ticket);

        LocalDate newDueDate = currentDueDate.plusDays(7);
        BorrowTicketResponse response = borrowService.renewTicket("BT001", new RenewTicketRequest(newDueDate));

        assertEquals(newDueDate, response.getDueDate());
        assertEquals(1, ticket.getRenewalCount());
    }

    @Test
    void giaHan_phieuDaQuaHan_nemTicketRenewalNotAllowedException() {
        LocalDate pastDueDate = LocalDate.now().minusDays(1);
        BorrowTicket ticket = borrowingTicket(pastDueDate, 0);
        when(borrowTicketRepository.findById("BT001")).thenReturn(Optional.of(ticket));

        assertThrows(TicketRenewalNotAllowedException.class,
                () -> borrowService.renewTicket("BT001", new RenewTicketRequest(pastDueDate.plusDays(5))));
    }

    @Test
    void giaHan_vuotQuaSoLanToiDa_nemTicketRenewalNotAllowedException() {
        LocalDate dueDate = LocalDate.now().plusDays(3);
        BorrowTicket ticket = borrowingTicket(dueDate, 2);
        when(borrowTicketRepository.findById("BT001")).thenReturn(Optional.of(ticket));

        assertThrows(TicketRenewalNotAllowedException.class,
                () -> borrowService.renewTicket("BT001", new RenewTicketRequest(dueDate.plusDays(5))));
    }

    @Test
    void giaHan_hanMoiKhongSauHanCu_nemInvalidBorrowDateException() {
        LocalDate dueDate = LocalDate.now().plusDays(3);
        BorrowTicket ticket = borrowingTicket(dueDate, 0);
        when(borrowTicketRepository.findById("BT001")).thenReturn(Optional.of(ticket));

        assertThrows(InvalidBorrowDateException.class,
                () -> borrowService.renewTicket("BT001", new RenewTicketRequest(dueDate.minusDays(1))));
    }
}