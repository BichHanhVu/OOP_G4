package com.group4.library.dto;

import java.time.LocalDate;

public class ReturnRequest {
    private String ticketId;
    private LocalDate actualReturnDate;

    public ReturnRequest() {}
    public ReturnRequest(String ticketId, LocalDate actualReturnDate) {
        this.ticketId = ticketId;
        this.actualReturnDate = actualReturnDate;
    }
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }
    public LocalDate getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(LocalDate actualReturnDate) { this.actualReturnDate = actualReturnDate; }
}
