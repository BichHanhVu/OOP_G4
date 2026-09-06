package com.group4.library.service;

import com.group4.library.dto.DashboardResponse;
import com.group4.library.dto.TopBorrowedBookItem;
import com.group4.library.model.Book;
import com.group4.library.model.BorrowTicket;
import com.group4.library.model.BorrowTicketDetail;
import com.group4.library.model.ReturnRecord;
import com.group4.library.model.TicketStatus;
import com.group4.library.repository.BookRepository;
import com.group4.library.repository.BorrowTicketRepository;
import com.group4.library.repository.ReaderRepository;
import com.group4.library.repository.ReturnRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private static final int TOP_BORROWED_LIMIT = 5;

    private final ReaderRepository readerRepository;
    private final BookRepository bookRepository;
    private final BorrowTicketRepository ticketRepository;
    private final ReturnRecordRepository returnRepository;

    public DashboardService(ReaderRepository readerRepository, BookRepository bookRepository,
                            BorrowTicketRepository ticketRepository,
                            ReturnRecordRepository returnRepository) {
        this.readerRepository = readerRepository;
        this.bookRepository = bookRepository;
        this.ticketRepository = ticketRepository;
        this.returnRepository = returnRepository;
    }

    public DashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();
        List<BorrowTicket> allTickets = ticketRepository.findAll();
        List<ReturnRecord> allReturns = returnRepository.findAll();
        List<Book> allBooks = bookRepository.findAll();

        long borrowing = allTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.BORROWING).count();
        long overdue = allTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.BORROWING)
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today)).count();
        long totalFine = allReturns.stream()
                .mapToLong(ReturnRecord::getFineAmount).sum();
        long unpaidFine = allReturns.stream()
                .filter(r -> !r.isPaid())
                .mapToLong(ReturnRecord::getFineAmount).sum();
        long paidFine = allReturns.stream()
                .filter(ReturnRecord::isPaid)
                .mapToLong(ReturnRecord::getFineAmount).sum();

        List<TopBorrowedBookItem> topBorrowedBooks = buildTopBorrowedBooks(allTickets, allBooks);

        return new DashboardResponse(readerRepository.findAll().size(),
                allBooks.size(), borrowing, overdue, totalFine, unpaidFine, paidFine, topBorrowedBooks);
    }

    private List<TopBorrowedBookItem> buildTopBorrowedBooks(List<BorrowTicket> tickets, List<Book> books) {
        Map<String, Long> quantityByBook = new HashMap<>();
        Map<String, Long> timesByBook = new HashMap<>();

        for (BorrowTicket ticket : tickets) {
            if (ticket.getItems() == null) continue;
            for (BorrowTicketDetail detail : ticket.getItems()) {
                if (detail == null || detail.getBookId() == null) continue;
                quantityByBook.merge(detail.getBookId(), (long) detail.getQuantity(), Long::sum);
                timesByBook.merge(detail.getBookId(), 1L, Long::sum);
            }
        }

        Map<String, String> titleById = books.stream()
                .collect(Collectors.toMap(Book::getBookId, Book::getTitle, (a, b) -> a));

        return quantityByBook.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(TOP_BORROWED_LIMIT)
                .map(e -> new TopBorrowedBookItem(
                        e.getKey(),
                        titleById.getOrDefault(e.getKey(), "(Sách không còn tồn tại)"),
                        timesByBook.getOrDefault(e.getKey(), 0L),
                        e.getValue()))
                .collect(Collectors.toList());
    }
}