package com.group4.library.dto;

import java.util.List;
import java.util.Map;

/** Thống kê tổng quan kho sách — tương tự ReaderStatisticsResponse nhưng cho Book. */
public class BookStatisticsResponse {
    private long totalTitles;
    private long totalCopies;
    private long totalAvailableCopies;
    private long totalBorrowedCopies;
    private Map<String, Long> countByGenre;
    private List<BookResponse> lowStockBooks;
    private List<TopBorrowedBookItem> topBorrowedBooks;

    public BookStatisticsResponse() {
    }

    public BookStatisticsResponse(long totalTitles, long totalCopies, long totalAvailableCopies,
                                  long totalBorrowedCopies, Map<String, Long> countByGenre,
                                  List<BookResponse> lowStockBooks, List<TopBorrowedBookItem> topBorrowedBooks) {
        this.totalTitles = totalTitles;
        this.totalCopies = totalCopies;
        this.totalAvailableCopies = totalAvailableCopies;
        this.totalBorrowedCopies = totalBorrowedCopies;
        this.countByGenre = countByGenre;
        this.lowStockBooks = lowStockBooks;
        this.topBorrowedBooks = topBorrowedBooks;
    }

    public long getTotalTitles() { return totalTitles; }
    public void setTotalTitles(long totalTitles) { this.totalTitles = totalTitles; }

    public long getTotalCopies() { return totalCopies; }
    public void setTotalCopies(long totalCopies) { this.totalCopies = totalCopies; }

    public long getTotalAvailableCopies() { return totalAvailableCopies; }
    public void setTotalAvailableCopies(long totalAvailableCopies) { this.totalAvailableCopies = totalAvailableCopies; }

    public long getTotalBorrowedCopies() { return totalBorrowedCopies; }
    public void setTotalBorrowedCopies(long totalBorrowedCopies) { this.totalBorrowedCopies = totalBorrowedCopies; }

    public Map<String, Long> getCountByGenre() { return countByGenre; }
    public void setCountByGenre(Map<String, Long> countByGenre) { this.countByGenre = countByGenre; }

    public List<BookResponse> getLowStockBooks() { return lowStockBooks; }
    public void setLowStockBooks(List<BookResponse> lowStockBooks) { this.lowStockBooks = lowStockBooks; }

    public List<TopBorrowedBookItem> getTopBorrowedBooks() { return topBorrowedBooks; }
    public void setTopBorrowedBooks(List<TopBorrowedBookItem> topBorrowedBooks) { this.topBorrowedBooks = topBorrowedBooks; }
}