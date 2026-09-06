// dto/ReaderBorrowSummaryResponse.java
package com.group4.library.dto;

import java.util.List;

public class ReaderBorrowSummaryResponse {
    private final int currentlyBorrowedCount;
    private final long activeTicketCount;
    private final long overdueTicketCount;
    private final boolean reachedLimit;
    private final List<ReaderTicketSummaryResponse> tickets;

    public ReaderBorrowSummaryResponse(int currentlyBorrowedCount, long activeTicketCount,
                                       long overdueTicketCount, boolean reachedLimit,
                                       List<ReaderTicketSummaryResponse> tickets) {
        this.currentlyBorrowedCount = currentlyBorrowedCount;
        this.activeTicketCount = activeTicketCount;
        this.overdueTicketCount = overdueTicketCount;
        this.reachedLimit = reachedLimit;
        this.tickets = tickets;
    }

    public int getCurrentlyBorrowedCount() { return currentlyBorrowedCount; }
    public long getActiveTicketCount() { return activeTicketCount; }
    public long getOverdueTicketCount() { return overdueTicketCount; }
    public boolean isReachedLimit() { return reachedLimit; }
    public List<ReaderTicketSummaryResponse> getTickets() { return tickets; }
}