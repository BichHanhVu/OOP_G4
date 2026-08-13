package com.group4.library.model;

public class BorrowTicketDetail {
    private String id;
    private String ticketId;
    private String bookId;
    private int quantity;

    public BorrowTicketDetail() {}

    public BorrowTicketDetail(String id, String ticketId, String bookId, int quantity) {
        // Kiểm tra số lượng hợp lệ ngay khi khởi tạo Model
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng mượn (quantity) trong chi tiết phiếu phải lớn hơn 0!");
        }
        this.id = id;
        this.ticketId = ticketId;
        this.bookId = bookId;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
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

    // Chặn số lượng <= 0 ngay tại Setter theo đúng cmt của Hạnh
    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng mượn (quantity) phải lớn hơn 0!");
        }
        this.quantity = quantity;
    }
}