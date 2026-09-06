package com.group4.library.dto;

import java.time.LocalDate;

public class ReturnResponse {
    private final String returnId;
    private final String ticketId;
    private final LocalDate actualReturnDate;
    private final long lateDays;
    private final long fineAmount;
    private final boolean paid;
    private final LocalDate paidDate;

    // Constructor cũ (5 tham số) được giữ nguyên để tương thích ngược, mặc định chưa thanh toán
    public ReturnResponse(String returnId, String ticketId, LocalDate actualReturnDate,
                          long lateDays, long fineAmount) {
        this(returnId, ticketId, actualReturnDate, lateDays, fineAmount, false, null);
    }

    public ReturnResponse(String returnId, String ticketId, LocalDate actualReturnDate,
                          long lateDays, long fineAmount, boolean paid, LocalDate paidDate) {
        this.returnId = returnId;
        this.ticketId = ticketId;
        this.actualReturnDate = actualReturnDate;
        this.lateDays = lateDays;
        this.fineAmount = fineAmount;
        this.paid = paid;
        this.paidDate = paidDate;
    }
    public String getReturnId() { return returnId; }
    public String getTicketId() { return ticketId; }
    public LocalDate getActualReturnDate() { return actualReturnDate; }
    public long getLateDays() { return lateDays; }
    public long getFineAmount() { return fineAmount; }
    public boolean isPaid() { return paid; }
    public LocalDate getPaidDate() { return paidDate; }
}