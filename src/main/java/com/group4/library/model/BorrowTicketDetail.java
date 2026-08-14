package com.group4.library.model;

import com.group4.library.exception.InvalidQuantityException;

public class BorrowTicketDetail {
    private String id;
    private String ticketId;
    private String bookId;
    private int quantity;

    // Constructor không tham số (Dùng cho Jackson/JSON)
    public BorrowTicketDetail() {
    }

    // Constructor đầy đủ tham số
    public BorrowTicketDetail(String id, String ticketId, String bookId, int quantity) {
        setId(id);
        setTicketId(ticketId);
        setBookId(bookId);
        setQuantity(quantity);
    }

    // Getters & Setters có Validation
    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Mã chi tiết không được để trống");
        }
        this.id = id.trim();
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException("Mã phiếu không được để trống");
        }
        this.ticketId = ticketId.trim();
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        if (bookId == null || bookId.isBlank()) {
            throw new IllegalArgumentException("Mã sách không được để trống");
        }
        this.bookId = bookId.trim();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new InvalidQuantityException("Số lượng mượn phải lớn hơn 0");
        }
        this.quantity = quantity;
    }
}