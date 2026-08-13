package com.group4.library.dto;

import java.time.LocalDate;

public class ReturnResponse {
    private final String returnId;
    private final String ticketId;
    private final LocalDate actualReturnDate;
    private final long lateDays;
    private final long fineAmount;

    public ReturnResponse(String returnId, String ticketId, LocalDate actualReturnDate,
                          long lateDays, long fineAmount) {
        this.returnId = returnId;
        this.ticketId = ticketId;
        this.actualReturnDate = actualReturnDate;
        this.lateDays = lateDays;
        this.fineAmount = fineAmount;
    }
    public String getReturnId() { return returnId; }
    public String getTicketId() { return ticketId; }
    public LocalDate getActualReturnDate() { return actualReturnDate; }
    public long getLateDays() { return lateDays; }
    public long getFineAmount() { return fineAmount; }
}
