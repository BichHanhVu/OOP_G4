package com.group4.library.dto;

public class DashboardResponse {
    private final long totalReaders;
    private final long totalBooks;
    private final long borrowingTickets;
    private final long overdueTickets;
    private final long totalFineAmount;

    public DashboardResponse(long totalReaders, long totalBooks, long borrowingTickets,
                             long overdueTickets, long totalFineAmount) {
        this.totalReaders = totalReaders;
        this.totalBooks = totalBooks;
        this.borrowingTickets = borrowingTickets;
        this.overdueTickets = overdueTickets;
        this.totalFineAmount = totalFineAmount;
    }
    public long getTotalReaders() { return totalReaders; }
    public long getTotalBooks() { return totalBooks; }
    public long getBorrowingTickets() { return borrowingTickets; }
    public long getOverdueTickets() { return overdueTickets; }
    public long getTotalFineAmount() { return totalFineAmount; }
}
