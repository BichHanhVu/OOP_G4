package com.group4.library.model;

import com.group4.library.exception.InvalidBorrowDateException;

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
    private List<BorrowTicketDetail> items;

    // Constructor không tham số (Dùng cho Jackson/JSON)
    public BorrowTicket() {
    }

    // Constructor đầy đủ tham số (Đã bổ sung validation theo yêu cầu mục 7)
    public BorrowTicket(String ticketId, String readerId, LocalDate borrowDate, LocalDate dueDate,
                        LocalDate returnDate, TicketStatus status, List<BorrowTicketDetail> items) {
        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException("Mã phiếu mượn không được để trống");
        }
        if (readerId == null || readerId.isBlank()) {
            throw new IllegalArgumentException("Mã bạn đọc không được để trống");
        }
        if (borrowDate == null) {
            throw new IllegalArgumentException("Ngày mượn không được để trống");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("Hạn trả không được để trống");
        }

        // 1. Kiểm tra dueDate không trước borrowDate
        if (dueDate.isBefore(borrowDate)) {
            throw new InvalidBorrowDateException("Hạn trả không được trước ngày mượn");
        }

        this.ticketId = ticketId.trim();
        this.readerId = readerId.trim();
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;

        // 2. Nếu status == null thì đặt mặc định BORROWING
        this.status = (status != null) ? status : TicketStatus.BORROWING;

        // 3. Sao chép danh sách item để tránh bị sửa từ bên ngoài (defensive copy)
        this.items = (items != null) ? new ArrayList<>(items) : new ArrayList<>();
    }

    // Getters & Setters
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
        if (borrowDate != null && dueDate != null && dueDate.isBefore(borrowDate)) {
            throw new InvalidBorrowDateException("Hạn trả không được trước ngày mượn");
        }
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
        this.status = (status != null) ? status : TicketStatus.BORROWING;
    }

    public List<BorrowTicketDetail> getItems() {
        return items;
    }

    public void setItems(List<BorrowTicketDetail> items) {
        this.items = (items != null) ? new ArrayList<>(items) : new ArrayList<>();
    }
}