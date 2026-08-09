package com.group4.library.dto;

public class BorrowItemRequest {

    private String bookId;
    private int quantity;

    public BorrowItemRequest() {
    }

    public BorrowItemRequest(String bookId, int quantity) {
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