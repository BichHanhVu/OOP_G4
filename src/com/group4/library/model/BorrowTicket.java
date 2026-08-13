package com.group4.library.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowTicket {

    private String ticketId;
    private String readerId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private TicketStatus status;
    private List<BorrowTicketDetail> items = new ArrayList<>();

    public BorrowTicket() {
    }

    // Constructor đã được bổ sung kiểm tra chặt chẽ theo cmt của Hạnh
    public BorrowTicket(String ticketId, String readerId, LocalDate borrowDate,
                        LocalDate dueDate, LocalDate returnDate, TicketStatus status,
                        List<BorrowTicketDetail> items) {

        // 1. Kiểm tra các trường bắt buộc không được null hoặc rỗng
        if (ticketId == null || ticketId.trim().isEmpty()) {
            throw new IllegalArgumentException("ticketId không được để trống!");
        }
        if (readerId == null || readerId.trim().isEmpty()) {
            throw new IllegalArgumentException("readerId không được để trống!");
        }
        if (borrowDate == null) {
            throw new IllegalArgumentException("borrowDate không được để trống!");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("dueDate không được để trống!");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Danh sách sách mượn (items) không được null hoặc rỗng!");
        }

        this.ticketId = ticketId.trim();
        this.readerId = readerId.trim();
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status != null ? status : TicketStatus.BORROWING;

        // 2. Lọc các item null trước khi gán
        this.items = new ArrayList<>();
        for (BorrowTicketDetail item : items) {
            if (item != null) {
                this.items.add(item);
            }
        }

        if (this.items.isEmpty()) {
            throw new IllegalArgumentException("Danh sách chi tiết phiếu mượn không hợp lệ!");
        }
    }

    /**
     * Tính tổng số lượng sách trong phiếu.
     * Đã bổ sung check item khác null theo cmt của Hạnh để tránh NullPointerException.
     */
    public int getTotalQuantity() {
        if (items == null) {
            return 0;
        }
        int total = 0;
        for (BorrowTicketDetail item : items) {
            if (item != null) { // Check item khác null
                total += item.getQuantity();
            }
        }
        return total;
    }

    // ================= GETTER & SETTER =================
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

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
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
        this.items = items;
    }
}