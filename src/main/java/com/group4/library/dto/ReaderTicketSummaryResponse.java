// dto/ReaderTicketSummaryResponse.java
package com.group4.library.dto;

import java.time.LocalDate;
import java.util.List;

public class ReaderTicketSummaryResponse {
    private final String ticketId;
    private final LocalDate borrowDate;
    private final LocalDate dueDate;
    private final String status;
    private final boolean overdue;
    private final List<BookItem> books;

    public ReaderTicketSummaryResponse(String ticketId, LocalDate borrowDate, LocalDate dueDate,
                                       String status, boolean overdue, List<BookItem> books) {
        this.ticketId = ticketId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = status;
        this.overdue = overdue;
        this.books = books;
    }

    public String getTicketId() { return ticketId; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public String getStatus() { return status; }
    public boolean isOverdue() { return overdue; }
    public List<BookItem> getBooks() { return books; }

    public static class BookItem {
        private final String bookId;
        private final String title;
        private final int quantity;

        public BookItem(String bookId, String title, int quantity) {
            this.bookId = bookId;
            this.title = title;
            this.quantity = quantity;
        }

        public String getBookId() { return bookId; }
        public String getTitle() { return title; }
        public int getQuantity() { return quantity; }
    }
}