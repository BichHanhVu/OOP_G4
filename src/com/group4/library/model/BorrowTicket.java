
package com.group4.library.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowTicket {

    private String ticketId;
    private String readerId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private TicketStatus status;
    private List<BorrowTicketDetail> items;

    public BorrowTicket() {
        this.items = new ArrayList<>();
    }

    public BorrowTicket(
            String ticketId,
            String readerId,
            LocalDate borrowDate,
            LocalDate dueDate,
            TicketStatus status,
            List<BorrowTicketDetail> items) {

        this.ticketId = ticketId;
        this.readerId = readerId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = status;
        this.items = items != null ? items : new ArrayList<>();
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

    public List<BorrowTicketDetail> getItems() {
        return items;
    }

    public void setItems(List<BorrowTicketDetail> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void addItem(BorrowTicketDetail item) {
        this.items.add(item);
    }

    public int getTotalQuantity() {
        return items.stream()
                .mapToInt(BorrowTicketDetail::getQuantity)
                .sum();
    }
}

