package com.group4.library.service;

import com.group4.library.dto.BorrowItemRequest;
import com.group4.library.dto.BorrowRequest;
import com.group4.library.dto.BorrowTicketResponse;
import com.group4.library.exception.BorrowLimitExceededException;
import com.group4.library.exception.InvalidBorrowDateException;
import com.group4.library.exception.InvalidQuantityException;
import com.group4.library.exception.ResourceNotFoundException;
import com.group4.library.model.Book;
import com.group4.library.model.Reader;
import com.group4.library.model.TicketStatus;
import com.group4.library.repository.BookRepository;
import com.group4.library.repository.BorrowTicketRepository;
import com.group4.library.repository.ReaderRepository;
import com.group4.library.exception.OutOfStockException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock
    private BorrowTicketRepository borrowTicketRepository;

    @Mock
    private ReaderRepository readerRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BorrowService borrowService;

    private BorrowRequest validRequest;
    private Reader sampleReader;
    private Book sampleBook;

    @BeforeEach
    void setUp() {
        // 1. Mock Reader (Tránh lỗi 'Reader' is abstract)
        sampleReader = mock(Reader.class);
        lenient().when(sampleReader.getId()).thenReturn("R001");
        lenient().when(sampleReader.getMaxBorrowLimit()).thenReturn(5);

        // 2. Mock Book (Tránh lỗi Constructor)
        sampleBook = mock(Book.class);
        lenient().when(sampleBook.getBookId()).thenReturn("BK001");
        lenient().when(sampleBook.getAvailableQuantity()).thenReturn(10);
        lenient().when(sampleBook.getTitle()).thenReturn("Lập trình Java");
        lenient().when(sampleBook.canBorrow(anyInt()))
                .thenReturn(true);

        lenient().when(bookRepository.update(any(Book.class)))
                .thenReturn(true);

        // 3. Tạo Request mẫu hợp lệ
        BorrowItemRequest item = new BorrowItemRequest("BK001", 1);
        validRequest = new BorrowRequest();
        validRequest.setReaderId("R001");
        validRequest.setBorrowDate(LocalDate.now());
        validRequest.setDueDate(LocalDate.now().plusDays(7));
        validRequest.setItems(List.of(item));
    }

    @Test
    void testRequestNull() {
        assertThrows(IllegalArgumentException.class, () -> borrowService.createBorrowTicket(null));
    }

    @Test
    void testReaderIdEmpty() {
        validRequest.setReaderId("");
        assertThrows(IllegalArgumentException.class, () -> borrowService.createBorrowTicket(validRequest));
    }

    @Test
    void testReaderNotFound() {
        when(readerRepository.findById("R001")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> borrowService.createBorrowTicket(validRequest));
    }

    @Test
    void testBorrowDateNull() {
        validRequest.setBorrowDate(null);
        assertThrows(InvalidBorrowDateException.class, () -> borrowService.createBorrowTicket(validRequest));
    }

    @Test
    void testDueDateNull() {
        validRequest.setDueDate(null);
        assertThrows(InvalidBorrowDateException.class, () -> borrowService.createBorrowTicket(validRequest));
    }

    @Test
    void testDueDateBeforeBorrowDate() {
        validRequest.setBorrowDate(LocalDate.now());
        validRequest.setDueDate(LocalDate.now().minusDays(1));
        assertThrows(InvalidBorrowDateException.class, () -> borrowService.createBorrowTicket(validRequest));
    }

    @Test
    void testItemsNullOrEmpty() {
        validRequest.setItems(null);
        assertThrows(InvalidQuantityException.class, () -> borrowService.createBorrowTicket(validRequest));

        validRequest.setItems(Collections.emptyList());
        assertThrows(InvalidQuantityException.class, () -> borrowService.createBorrowTicket(validRequest));
    }

    @Test
    void testQuantityZeroOrNegative() {
        when(readerRepository.findById("R001"))
                .thenReturn(Optional.of(sampleReader));

        BorrowItemRequest invalidItem =
                new BorrowItemRequest("BK001", 0);

        validRequest.setItems(List.of(invalidItem));

        assertThrows(
                InvalidQuantityException.class,
                () -> borrowService.createBorrowTicket(validRequest)
        );

        invalidItem.setQuantity(-2);

        assertThrows(
                InvalidQuantityException.class,
                () -> borrowService.createBorrowTicket(validRequest)
        );
    }

    @Test
    void testConsolidateDuplicateBookId() {
        BorrowItemRequest item1 = new BorrowItemRequest("bk001", 1);
        BorrowItemRequest item2 = new BorrowItemRequest("BK001", 2);
        validRequest.setItems(List.of(item1, item2));

        when(readerRepository.findById("R001")).thenReturn(Optional.of(sampleReader));
        when(borrowTicketRepository.findByReaderIdAndStatus("R001", TicketStatus.BORROWING)).thenReturn(Collections.emptyList());
        when(bookRepository.findById("BK001")).thenReturn(Optional.of(sampleBook));
        when(borrowTicketRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BorrowTicketResponse response = borrowService.createBorrowTicket(validRequest);

        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        assertEquals(3, response.getItems().get(0).getQuantity());
        verify(sampleBook, times(1)).borrow(3);

        verify(bookRepository, times(1))
                .update(sampleBook);

        verify(bookRepository, never())
                .save(sampleBook);
    }

    @Test
    void testExceedBorrowLimit() {
        BorrowItemRequest item = new BorrowItemRequest("BK001", 6); // Vượt quá limit (5)
        validRequest.setItems(List.of(item));

        when(readerRepository.findById("R001")).thenReturn(Optional.of(sampleReader));
        when(borrowTicketRepository.findByReaderIdAndStatus("R001", TicketStatus.BORROWING)).thenReturn(Collections.emptyList());

        assertThrows(BorrowLimitExceededException.class, () -> borrowService.createBorrowTicket(validRequest));
    }

    @Test
    void testFilterTicketsByReader() {
        when(borrowTicketRepository.findByReaderId("R001")).thenReturn(Collections.emptyList());
        List<BorrowTicketResponse> list = borrowService.getAllTickets("R001", null);
        assertNotNull(list);
        verify(borrowTicketRepository, times(1)).findByReaderId("R001");
    }

    @Test
    void testFilterTicketsByStatus() {
        when(borrowTicketRepository.findByStatus(TicketStatus.BORROWING)).thenReturn(Collections.emptyList());
        List<BorrowTicketResponse> list = borrowService.getAllTickets(null, TicketStatus.BORROWING);
        assertNotNull(list);
        verify(borrowTicketRepository, times(1)).findByStatus(TicketStatus.BORROWING);
    }

    @Test
    void testBookNotFound() {
        when(readerRepository.findById("R001"))
                .thenReturn(Optional.of(sampleReader));

        when(borrowTicketRepository
                .findByReaderIdAndStatus(
                        "R001",
                        TicketStatus.BORROWING))
                .thenReturn(Collections.emptyList());

        when(bookRepository.findById("BK001"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> borrowService.createBorrowTicket(validRequest)
        );
    }

    @Test
    void testOutOfStock() {
        validRequest.setItems(
                List.of(new BorrowItemRequest("BK001", 2))
        );

        when(readerRepository.findById("R001"))
                .thenReturn(Optional.of(sampleReader));

        when(borrowTicketRepository
                .findByReaderIdAndStatus(
                        "R001",
                        TicketStatus.BORROWING))
                .thenReturn(Collections.emptyList());

        when(bookRepository.findById("BK001"))
                .thenReturn(Optional.of(sampleBook));

        when(sampleBook.canBorrow(2))
                .thenReturn(false);

        assertThrows(
                OutOfStockException.class,
                () -> borrowService.createBorrowTicket(validRequest)
        );

        verify(bookRepository, never())
                .update(any(Book.class));
    }
}