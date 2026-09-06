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
    // Số lần phiếu đã được gia hạn (mặc định 0, tăng dần mỗi lần gọi renewTicket)
    private int renewalCount = 0;


    // Constructor không tham số - dùng cho Jackson/JSON
    public BorrowTicket() {
        this.renewalCount = 0;
    }

    // Constructor đầy đủ tham số
    public BorrowTicket(
            String ticketId,
            String readerId,
            LocalDate borrowDate,
            LocalDate dueDate,
            LocalDate returnDate,
            TicketStatus status,
            List<BorrowTicketDetail> items) {

        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException(
                    "Mã phiếu mượn không được để trống"
            );
        }

        if (readerId == null || readerId.isBlank()) {
            throw new IllegalArgumentException(
                    "Mã bạn đọc không được để trống"
            );
        }

        if (borrowDate == null) {
            throw new IllegalArgumentException(
                    "Ngày mượn không được để trống"
            );
        }

        if (dueDate == null) {
            throw new IllegalArgumentException(
                    "Hạn trả không được để trống"
            );
        }

        if (dueDate.isBefore(borrowDate)) {
            throw new InvalidBorrowDateException(
                    "Hạn trả không được trước ngày mượn"
            );
        }

        this.ticketId = ticketId.trim();
        this.readerId = readerId.trim();
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;

        this.status = status != null
                ? status
                : TicketStatus.BORROWING;

        this.items = items != null
                ? new ArrayList<>(items)
                : new ArrayList<>();

        // Phiếu mới chưa từng gia hạn
        this.renewalCount = 0;
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

        if (borrowDate != null
                && dueDate != null
                && dueDate.isBefore(borrowDate)) {

            throw new InvalidBorrowDateException(
                    "Hạn trả không được trước ngày mượn"
            );
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
        this.status = status != null
                ? status
                : TicketStatus.BORROWING;
    }

    public List<BorrowTicketDetail> getItems() {
        return items;
    }

    public void setItems(List<BorrowTicketDetail> items) {
        this.items = items != null
                ? new ArrayList<>(items)
                : new ArrayList<>();
    }

    public int getRenewalCount() {
        return renewalCount;
    }

    public void setRenewalCount(int renewalCount) {
        if (renewalCount < 0) {
            throw new IllegalArgumentException(
                    "Số lần gia hạn không được âm"
            );
        }

        this.renewalCount = renewalCount;
    }
}