// dto/ReaderDetailResponse.java
package com.group4.library.dto;

public class ReaderDetailResponse {
    private final String id;
    private final String name;
    private final String phoneNumber;
    private final String type;
    private final int maxBorrowLimit;
    private final ReaderBorrowSummaryResponse borrowSummary;

    public ReaderDetailResponse(String id, String name, String phoneNumber, String type,
                                int maxBorrowLimit, ReaderBorrowSummaryResponse borrowSummary) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.type = type;
        this.maxBorrowLimit = maxBorrowLimit;
        this.borrowSummary = borrowSummary;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getType() { return type; }
    public int getMaxBorrowLimit() { return maxBorrowLimit; }
    public ReaderBorrowSummaryResponse getBorrowSummary() { return borrowSummary; }
}