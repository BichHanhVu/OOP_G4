package com.group4.library.service;

import com.group4.library.dto.DashboardResponse;
import com.group4.library.model.TicketStatus;
import com.group4.library.repository.BookRepository;
import com.group4.library.repository.BorrowTicketRepository;
import com.group4.library.repository.ReaderRepository;
import com.group4.library.repository.ReturnRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DashboardService {
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
        long borrowing = ticketRepository.findAll().stream()
                .filter(t -> t.getStatus() == TicketStatus.BORROWING).count();
        long overdue = ticketRepository.findAll().stream()
                .filter(t -> t.getStatus() == TicketStatus.BORROWING)
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today)).count();
        long totalFine = returnRepository.findAll().stream()
                .mapToLong(r -> r.getFineAmount()).sum();
        return new DashboardResponse(readerRepository.findAll().size(),
                bookRepository.findAll().size(), borrowing, overdue, totalFine);
    }
}
