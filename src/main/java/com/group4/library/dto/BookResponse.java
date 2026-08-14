package com.group4.library.dto;

public class BookResponse {
    private String bookId;
    private String title;
    private String author;
    private String genre;
    private Integer availableQuantity;
    private Long price;

    public BookResponse() {
    }

    public BookResponse(String bookId, String title, String author, String genre, Integer availableQuantity, Long price) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.availableQuantity = availableQuantity;
        this.price = price;
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
}