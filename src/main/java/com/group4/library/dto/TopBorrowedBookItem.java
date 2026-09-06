package com.group4.library.dto;

/** Một dòng trong bảng xếp hạng "sách được mượn nhiều nhất" (dùng cho BookStatistics & Dashboard). */
public class TopBorrowedBookItem {
    private String bookId;
    private String title;
    private long timesBorrowed;
    private long totalQuantityBorrowed;

    public TopBorrowedBookItem() {
    }

    public TopBorrowedBookItem(String bookId, String title, long timesBorrowed, long totalQuantityBorrowed) {
        this.bookId = bookId;
        this.title = title;
        this.timesBorrowed = timesBorrowed;
        this.totalQuantityBorrowed = totalQuantityBorrowed;
    }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public long getTimesBorrowed() { return timesBorrowed; }
    public void setTimesBorrowed(long timesBorrowed) { this.timesBorrowed = timesBorrowed; }

    public long getTotalQuantityBorrowed() { return totalQuantityBorrowed; }
    public void setTotalQuantityBorrowed(long totalQuantityBorrowed) { this.totalQuantityBorrowed = totalQuantityBorrowed; }
}