package com.group4.library.dto;

import java.util.Collections;
import java.util.List;

public class DashboardResponse {
    private final long totalReaders;
    private final long totalBooks;
    private final long borrowingTickets;
    private final long overdueTickets;
    private final long totalFineAmount;
    private final long totalUnpaidFineAmount;
    private final long totalPaidFineAmount;
    private final List<TopBorrowedBookItem> topBorrowedBooks;

    // Constructor cũ (5 tham số) được giữ lại để tương thích ngược
    public DashboardResponse(long totalReaders, long totalBooks, long borrowingTickets,
                             long overdueTickets, long totalFineAmount) {
        this(totalReaders, totalBooks, borrowingTickets, overdueTickets, totalFineAmount,
                0L, 0L, Collections.emptyList());
    }

    public DashboardResponse(long totalReaders, long totalBooks, long borrowingTickets,
                             long overdueTickets, long totalFineAmount,
                             long totalUnpaidFineAmount, long totalPaidFineAmount,
                             List<TopBorrowedBookItem> topBorrowedBooks) {
        this.totalReaders = totalReaders;
        this.totalBooks = totalBooks;
        this.borrowingTickets = borrowingTickets;
        this.overdueTickets = overdueTickets;
        this.totalFineAmount = totalFineAmount;
        this.totalUnpaidFineAmount = totalUnpaidFineAmount;
        this.totalPaidFineAmount = totalPaidFineAmount;
        this.topBorrowedBooks = (topBorrowedBooks != null) ? topBorrowedBooks : Collections.emptyList();
    }

    public long getTotalReaders() { return totalReaders; }
    public long getTotalBooks() { return totalBooks; }
    public long getBorrowingTickets() { return borrowingTickets; }
    public long getOverdueTickets() { return overdueTickets; }
    public long getTotalFineAmount() { return totalFineAmount; }
    public long getTotalUnpaidFineAmount() { return totalUnpaidFineAmount; }
    public long getTotalPaidFineAmount() { return totalPaidFineAmount; }
    public List<TopBorrowedBookItem> getTopBorrowedBooks() { return topBorrowedBooks; }
}