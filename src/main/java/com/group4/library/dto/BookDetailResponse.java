package com.group4.library.dto;

import java.util.List;

/** Chi tiết một cuốn sách kèm số liệu tổng hợp và lịch sử mượn — tương tự ReaderDetailResponse. */
public class BookDetailResponse {
    private String bookId;
    private String title;
    private String author;
    private String genre;
    private Integer availableQuantity;
    private Long price;
    private long timesBorrowed;
    private long totalQuantityBorrowed;
    private int currentBorrowingQuantity;
    private List<BookBorrowHistoryItem> history;

    public BookDetailResponse() {
    }

    public BookDetailResponse(String bookId, String title, String author, String genre,
                              Integer availableQuantity, Long price, long timesBorrowed,
                              long totalQuantityBorrowed, int currentBorrowingQuantity,
                              List<BookBorrowHistoryItem> history) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.availableQuantity = availableQuantity;
        this.price = price;
        this.timesBorrowed = timesBorrowed;
        this.totalQuantityBorrowed = totalQuantityBorrowed;
        this.currentBorrowingQuantity = currentBorrowingQuantity;
        this.history = history;
    }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }

    public Long getPrice() { return price; }
    public void setPrice(Long price) { this.price = price; }

    public long getTimesBorrowed() { return timesBorrowed; }
    public void setTimesBorrowed(long timesBorrowed) { this.timesBorrowed = timesBorrowed; }

    public long getTotalQuantityBorrowed() { return totalQuantityBorrowed; }
    public void setTotalQuantityBorrowed(long totalQuantityBorrowed) { this.totalQuantityBorrowed = totalQuantityBorrowed; }

    public int getCurrentBorrowingQuantity() { return currentBorrowingQuantity; }
    public void setCurrentBorrowingQuantity(int currentBorrowingQuantity) { this.currentBorrowingQuantity = currentBorrowingQuantity; }

    public List<BookBorrowHistoryItem> getHistory() { return history; }
    public void setHistory(List<BookBorrowHistoryItem> history) { this.history = history; }
}