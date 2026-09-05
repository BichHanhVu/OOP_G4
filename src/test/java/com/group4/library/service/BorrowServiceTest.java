package com.group4.library.service;

import com.group4.library.dto.BorrowItemRequest;
import com.group4.library.dto.BorrowRequest;
import com.group4.library.dto.BorrowTicketResponse;
import com.group4.library.dto.RenewTicketRequest;
import com.group4.library.dto.RenewTicketResponse;

import com.group4.library.exception.BorrowLimitExceededException;
import com.group4.library.exception.InvalidBorrowDateException;
import com.group4.library.exception.InvalidQuantityException;
import com.group4.library.exception.ResourceNotFoundException;
import com.group4.library.exception.OutOfStockException;
import com.group4.library.exception.RenewalNotAllowedException;

import com.group4.library.model.Book;
import com.group4.library.model.BorrowTicket;
import com.group4.library.model.Reader;
import com.group4.library.model.TicketStatus;

import com.group4.library.repository.BookRepository;
import com.group4.library.repository.BorrowTicketRepository;
import com.group4.library.repository.ReaderRepository;

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


    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() {

        // 1. Mock Reader
        sampleReader = mock(Reader.class);

        lenient()
                .when(sampleReader.getId())
                .thenReturn("R001");

        lenient()
                .when(sampleReader.getMaxBorrowLimit())
                .thenReturn(5);


        // 2. Mock Book
        sampleBook = mock(Book.class);

        lenient()
                .when(sampleBook.getBookId())
                .thenReturn("BK001");

        lenient()
                .when(sampleBook.getAvailableQuantity())
                .thenReturn(10);

        lenient()
                .when(sampleBook.getTitle())
                .thenReturn("Lập trình Java");

        lenient()
                .when(sampleBook.canBorrow(anyInt()))
                .thenReturn(true);

        lenient()
                .when(bookRepository.update(any(Book.class)))
                .thenReturn(true);


        // 3. Request mẫu
        BorrowItemRequest item =
                new BorrowItemRequest("BK001", 1);

        validRequest = new BorrowRequest();

        validRequest.setReaderId("R001");

        validRequest.setBorrowDate(
                LocalDate.now()
        );

        validRequest.setDueDate(
                LocalDate.now().plusDays(7)
        );

        validRequest.setItems(
                List.of(item)
        );
    }


    // =========================================================
    // CREATE BORROW TICKET TESTS
    // =========================================================

    @Test
    void testRequestNull() {

        assertThrows(
                IllegalArgumentException.class,
                () -> borrowService.createBorrowTicket(null)
        );
    }


    @Test
    void testReaderIdEmpty() {

        validRequest.setReaderId("");

        assertThrows(
                IllegalArgumentException.class,
                () -> borrowService.createBorrowTicket(validRequest)
        );
    }


    @Test
    void testReaderNotFound() {

        when(readerRepository.findById("R001"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> borrowService.createBorrowTicket(validRequest)
        );
    }


    @Test
    void testBorrowDateNull() {

        validRequest.setBorrowDate(null);

        assertThrows(
                InvalidBorrowDateException.class,
                () -> borrowService.createBorrowTicket(validRequest)
        );
    }


    @Test
    void testDueDateNull() {

        validRequest.setDueDate(null);

        assertThrows(
                InvalidBorrowDateException.class,
                () -> borrowService.createBorrowTicket(validRequest)
        );
    }


    @Test
    void testDueDateBeforeBorrowDate() {

        validRequest.setBorrowDate(
                LocalDate.now()
        );

        validRequest.setDueDate(
                LocalDate.now().minusDays(1)
        );

        assertThrows(
                InvalidBorrowDateException.class,
                () -> borrowService.createBorrowTicket(validRequest)
        );
    }


    @Test
    void testItemsNullOrEmpty() {

        validRequest.setItems(null);

        assertThrows(
                InvalidQuantityException.class,
                () -> borrowService.createBorrowTicket(validRequest)
        );


        validRequest.setItems(
                Collections.emptyList()
        );

        assertThrows(
                InvalidQuantityException.class,
                () -> borrowService.createBorrowTicket(validRequest)
        );
    }


    @Test
    void testQuantityZeroOrNegative() {

        when(readerRepository.findById("R001"))
                .thenReturn(Optional.of(sampleReader));

        BorrowItemRequest invalidItem =
                new BorrowItemRequest("BK001", 0);

        validRequest.setItems(
                List.of(invalidItem)
        );

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

        BorrowItemRequest item1 =
                new BorrowItemRequest("bk001", 1);

        BorrowItemRequest item2 =
                new BorrowItemRequest("BK001", 2);

        validRequest.setItems(
                List.of(item1, item2)
        );


        when(readerRepository.findById("R001"))
                .thenReturn(Optional.of(sampleReader));

        when(
                borrowTicketRepository.findByReaderIdAndStatus(
                        "R001",
                        TicketStatus.BORROWING
                )
        )
                .thenReturn(Collections.emptyList());

        when(bookRepository.findById("BK001"))
                .thenReturn(Optional.of(sampleBook));

        when(borrowTicketRepository.save(any()))
                .thenAnswer(
                        invocation -> invocation.getArgument(0)
                );


        BorrowTicketResponse response =
                borrowService.createBorrowTicket(validRequest);


        assertNotNull(response);

        assertEquals(
                1,
                response.getItems().size()
        );

        assertEquals(
                3,
                response.getItems().get(0).getQuantity()
        );


        verify(sampleBook, times(1))
                .borrow(3);

        verify(bookRepository, times(1))
                .update(sampleBook);

        verify(bookRepository, never())
                .save(sampleBook);
    }


    @Test
    void testExceedBorrowLimit() {

        BorrowItemRequest item =
                new BorrowItemRequest("BK001", 6);

        validRequest.setItems(
                List.of(item)
        );


        when(readerRepository.findById("R001"))
                .thenReturn(Optional.of(sampleReader));

        when(
                borrowTicketRepository.findByReaderIdAndStatus(
                        "R001",
                        TicketStatus.BORROWING
                )
        )
                .thenReturn(Collections.emptyList());


        assertThrows(
                BorrowLimitExceededException.class,
                () -> borrowService.createBorrowTicket(validRequest)
        );
    }


    @Test
    void testFilterTicketsByReader() {

        when(
                borrowTicketRepository.findByReaderId("R001")
        )
                .thenReturn(Collections.emptyList());


        List<BorrowTicketResponse> list =
                borrowService.getAllTickets(
                        "R001",
                        null
                );


        assertNotNull(list);

        verify(
                borrowTicketRepository,
                times(1)
        )
                .findByReaderId("R001");
    }


    @Test
    void testFilterTicketsByStatus() {

        when(
                borrowTicketRepository.findByStatus(
                        TicketStatus.BORROWING
                )
        )
                .thenReturn(Collections.emptyList());


        List<BorrowTicketResponse> list =
                borrowService.getAllTickets(
                        null,
                        TicketStatus.BORROWING
                );


        assertNotNull(list);

        verify(
                borrowTicketRepository,
                times(1)
        )
                .findByStatus(TicketStatus.BORROWING);
    }


    @Test
    void testBookNotFound() {

        when(readerRepository.findById("R001"))
                .thenReturn(Optional.of(sampleReader));

        when(
                borrowTicketRepository.findByReaderIdAndStatus(
                        "R001",
                        TicketStatus.BORROWING
                )
        )
                .thenReturn(Collections.emptyList());

        when(bookRepository.findById("BK001"))
                .thenReturn(Optional.empty());


        assertThrows(
                ResourceNotFoundException.class,
                () -> borrowService.createBorrowTicket(validRequest)
        );
    }


    @Test
    void testOutOfStock() {

        validRequest.setItems(
                List.of(
                        new BorrowItemRequest("BK001", 2)
                )
        );


        when(readerRepository.findById("R001"))
                .thenReturn(Optional.of(sampleReader));

        when(
                borrowTicketRepository.findByReaderIdAndStatus(
                        "R001",
                        TicketStatus.BORROWING
                )
        )
                .thenReturn(Collections.emptyList());

        when(bookRepository.findById("BK001"))
                .thenReturn(Optional.of(sampleBook));

        when(sampleBook.canBorrow(2))
                .thenReturn(false);


        assertThrows(
                OutOfStockException.class,
                () -> borrowService.createBorrowTicket(validRequest)
        );


        verify(
                bookRepository,
                never()
        )
                .update(any(Book.class));
    }


    // =========================================================
    // RENEW TICKET TESTS
    // =========================================================

    /**
     * TEST 1
     * Gia hạn thành công
     */
    @Test
    void testRenewTicketSuccessfully() {

        LocalDate oldDueDate =
                LocalDate.now().plusDays(7);

        LocalDate newDueDate =
                LocalDate.now().plusDays(14);


        BorrowTicket ticket =
                new BorrowTicket(
                        "TICK-001",
                        "R001",
                        LocalDate.now(),
                        oldDueDate,
                        null,
                        TicketStatus.BORROWING,
                        Collections.emptyList()
                );


        when(
                borrowTicketRepository.findById("TICK-001")
        )
                .thenReturn(Optional.of(ticket));


        when(
                borrowTicketRepository.save(
                        any(BorrowTicket.class)
                )
        )
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );


        RenewTicketRequest request =
                new RenewTicketRequest();

        request.setNewDueDate(newDueDate);


        RenewTicketResponse response =
                borrowService.renewBorrowTicket(
                        "TICK-001",
                        request
                );


        assertNotNull(response);

        assertEquals(
                "TICK-001",
                response.getTicketId()
        );

        assertEquals(
                oldDueDate,
                response.getOldDueDate()
        );

        assertEquals(
                newDueDate,
                response.getNewDueDate()
        );

        assertEquals(
                1,
                response.getRenewalCount()
        );

        assertEquals(
                newDueDate,
                ticket.getDueDate()
        );


        verify(
                borrowTicketRepository,
                times(1)
        )
                .save(ticket);
    }


    /**
     * TEST 2
     * Phiếu không tồn tại
     */
    @Test
    void testRenewTicketNotFound() {

        when(
                borrowTicketRepository.findById("TICK-404")
        )
                .thenReturn(Optional.empty());


        RenewTicketRequest request =
                new RenewTicketRequest();

        request.setNewDueDate(
                LocalDate.now().plusDays(10)
        );


        assertThrows(
                ResourceNotFoundException.class,
                () ->
                        borrowService.renewBorrowTicket(
                                "TICK-404",
                                request
                        )
        );


        verify(
                borrowTicketRepository,
                never()
        )
                .save(any());
    }


    /**
     * TEST 3
     * Phiếu đã trả
     */
    @Test
    void testRenewReturnedTicket() {

        BorrowTicket ticket =
                new BorrowTicket(
                        "TICK-002",
                        "R001",
                        LocalDate.now().minusDays(10),
                        LocalDate.now().minusDays(3),
                        LocalDate.now(),
                        TicketStatus.RETURNED,
                        Collections.emptyList()
                );


        when(
                borrowTicketRepository.findById("TICK-002")
        )
                .thenReturn(Optional.of(ticket));


        RenewTicketRequest request =
                new RenewTicketRequest();

        request.setNewDueDate(
                LocalDate.now().plusDays(7)
        );


        assertThrows(
                RenewalNotAllowedException.class,
                () ->
                        borrowService.renewBorrowTicket(
                                "TICK-002",
                                request
                        )
        );


        verify(
                borrowTicketRepository,
                never()
        )
                .save(any());
    }


    /**
     * TEST 4
     * Phiếu đã quá hạn
     */
    @Test
    void testRenewOverdueTicket() {

        BorrowTicket ticket =
                new BorrowTicket(
                        "TICK-003",
                        "R001",
                        LocalDate.now().minusDays(10),
                        LocalDate.now().minusDays(1),
                        null,
                        TicketStatus.BORROWING,
                        Collections.emptyList()
                );


        when(
                borrowTicketRepository.findById("TICK-003")
        )
                .thenReturn(Optional.of(ticket));


        RenewTicketRequest request =
                new RenewTicketRequest();

        request.setNewDueDate(
                LocalDate.now().plusDays(10)
        );


        assertThrows(
                RenewalNotAllowedException.class,
                () ->
                        borrowService.renewBorrowTicket(
                                "TICK-003",
                                request
                        )
        );


        verify(
                borrowTicketRepository,
                never()
        )
                .save(any());
    }


    /**
     * TEST 5
     * Gia hạn lần thứ hai
     */
    @Test
    void testRenewTicketSecondTime() {

        BorrowTicket ticket =
                new BorrowTicket(
                        "TICK-004",
                        "R001",
                        LocalDate.now(),
                        LocalDate.now().plusDays(7),
                        null,
                        TicketStatus.BORROWING,
                        Collections.emptyList()
                );


        ticket.setRenewalCount(1);


        when(
                borrowTicketRepository.findById("TICK-004")
        )
                .thenReturn(Optional.of(ticket));


        RenewTicketRequest request =
                new RenewTicketRequest();

        request.setNewDueDate(
                LocalDate.now().plusDays(14)
        );


        assertThrows(
                RenewalNotAllowedException.class,
                () ->
                        borrowService.renewBorrowTicket(
                                "TICK-004",
                                request
                        )
        );


        verify(
                borrowTicketRepository,
                never()
        )
                .save(any());
    }


    /**
     * TEST 6
     * Ngày mới bằng hạn cũ
     */
    @Test
    void testRenewWithSameDueDate() {

        LocalDate dueDate =
                LocalDate.now().plusDays(7);


        BorrowTicket ticket =
                new BorrowTicket(
                        "TICK-005",
                        "R001",
                        LocalDate.now(),
                        dueDate,
                        null,
                        TicketStatus.BORROWING,
                        Collections.emptyList()
                );


        when(
                borrowTicketRepository.findById("TICK-005")
        )
                .thenReturn(Optional.of(ticket));


        RenewTicketRequest request =
                new RenewTicketRequest();

        request.setNewDueDate(dueDate);


        assertThrows(
                RenewalNotAllowedException.class,
                () ->
                        borrowService.renewBorrowTicket(
                                "TICK-005",
                                request
                        )
        );


        verify(
                borrowTicketRepository,
                never()
        )
                .save(any());
    }


    /**
     * TEST 7
     * Ngày mới trước hạn cũ
     */
    @Test
    void testRenewWithEarlierDueDate() {

        LocalDate oldDueDate =
                LocalDate.now().plusDays(10);


        BorrowTicket ticket =
                new BorrowTicket(
                        "TICK-006",
                        "R001",
                        LocalDate.now(),
                        oldDueDate,
                        null,
                        TicketStatus.BORROWING,
                        Collections.emptyList()
                );


        when(
                borrowTicketRepository.findById("TICK-006")
        )
                .thenReturn(Optional.of(ticket));


        RenewTicketRequest request =
                new RenewTicketRequest();

        request.setNewDueDate(
                LocalDate.now().plusDays(5)
        );


        assertThrows(
                RenewalNotAllowedException.class,
                () ->
                        borrowService.renewBorrowTicket(
                                "TICK-006",
                                request
                        )
        );


        verify(
                borrowTicketRepository,
                never()
        )
                .save(any());
    }
}