package com.group4.library.dto;

public class ReaderResponse {
    private final String id;
    private final String name;
    private final String phoneNumber;
    private final String type;
    private final int maxBorrowLimit;

    public ReaderResponse(String id, String name, String phoneNumber, String type, int maxBorrowLimit) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.type = type;
        this.maxBorrowLimit = maxBorrowLimit;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getType() { return type; }
    public int getMaxBorrowLimit() { return maxBorrowLimit; }
}
