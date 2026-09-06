package com.group4.library.model;

import java.time.LocalDate;

public class ReturnRecord {
    private String returnId;
    private String ticketId;
    private LocalDate actualReturnDate;
    private long lateDays;
    private long fineAmount;
    // Đánh dấu tiền phạt đã được thu hay chưa, và ngày thu (nếu có)
    private boolean paid = false;
    private LocalDate paidDate;

    public ReturnRecord() {}

    public ReturnRecord(String returnId, String ticketId, LocalDate actualReturnDate,
                        long lateDays, long fineAmount) {
        this.returnId = returnId;
        this.ticketId = ticketId;
        this.actualReturnDate = actualReturnDate;
        this.lateDays = lateDays;
        this.fineAmount = fineAmount;
    }

    public String getReturnId() { return returnId; }
    public void setReturnId(String returnId) { this.returnId = returnId; }
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }
    public LocalDate getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(LocalDate actualReturnDate) { this.actualReturnDate = actualReturnDate; }
    public long getLateDays() { return lateDays; }
    public void setLateDays(long lateDays) { this.lateDays = lateDays; }
    public long getFineAmount() { return fineAmount; }
    public void setFineAmount(long fineAmount) { this.fineAmount = fineAmount; }
    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
    public LocalDate getPaidDate() { return paidDate; }
    public void setPaidDate(LocalDate paidDate) { this.paidDate = paidDate; }
}