package com.group4.library.dto;

import java.time.LocalDate;
import java.util.List;

public class BorrowRequest {

    private String readerId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private List<BorrowItemRequest> items;

    public BorrowRequest() {
    }

    public BorrowRequest(
            String readerId,
            LocalDate borrowDate,
            LocalDate dueDate,
            List<BorrowItemRequest> items) {
        this.readerId = readerId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.items = items;
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

    public List<BorrowItemRequest> getItems() {
        return items;
    }

    public void setItems(List<BorrowItemRequest> items) {
        this.items = items;
    }
}