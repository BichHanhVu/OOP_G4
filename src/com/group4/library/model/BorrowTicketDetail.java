package com.group4.library.model;

public class BorrowTicketDetail {

    private String bookId;
    private int quantity;

    public BorrowTicketDetail() {
    }

    public BorrowTicketDetail(String bookId, int quantity) {
        this.bookId = bookId;
        this.quantity = quantity;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

