package com.group4.library.dto;

import com.group4.library.model.TicketStatus;

import java.time.LocalDate;

/** Một dòng lịch sử mượn của một cuốn sách cụ thể (dùng trong BookDetailResponse). */
public class BookBorrowHistoryItem {
    private String ticketId;
    private String readerId;
    private String readerName;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private TicketStatus status;
    private int quantity;

    public BookBorrowHistoryItem() {
    }

    public BookBorrowHistoryItem(String ticketId, String readerId, String readerName,
                                 LocalDate borrowDate, LocalDate dueDate, LocalDate returnDate,
                                 TicketStatus status, int quantity) {
        this.ticketId = ticketId;
        this.readerId = readerId;
        this.readerName = readerName;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
        this.quantity = quantity;
    }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getReaderId() { return readerId; }
    public void setReaderId(String readerId) { this.readerId = readerId; }

    public String getReaderName() { return readerName; }
    public void setReaderName(String readerName) { this.readerName = readerName; }

    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}