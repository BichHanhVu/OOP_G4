package com.group4.library.dto;

import com.group4.library.model.TicketStatus;

import java.time.LocalDate;
import java.util.List;

public class BorrowTicketResponse {

    private String ticketId;
    private String readerId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private TicketStatus status;
    private List<BorrowItemRequest> items;

    public BorrowTicketResponse() {
    }

    public BorrowTicketResponse(
            String ticketId,
            String readerId,
            LocalDate borrowDate,
            LocalDate dueDate,
            TicketStatus status,
            List<BorrowItemRequest> items) {
        this.ticketId = ticketId;
        this.readerId = readerId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = status;
        this.items = items;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getReaderId() {
        return readerId;
    }

    public void setReaderId(String readerId) {
        this.readerId = readerId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public List<BorrowItemRequest> getItems() {
        return items;
    }

    public void setItems(List<BorrowItemRequest> items) {
        this.items = items;
    }
}