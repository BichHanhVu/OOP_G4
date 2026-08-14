package com.group4.library.dto;

public class BorrowItemRequest {

    // Sử dụng 'code' để đồng bộ với module Book
    private String code;
    private int quantity;

    public BorrowItemRequest() {
    }

    public BorrowItemRequest(String code, int quantity) {
        this.code = code;
        this.quantity = quantity;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}