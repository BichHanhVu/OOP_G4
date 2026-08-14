package com.group4.library.model;

public class Book implements Borrowable {
    private String bookId;
    private String title;
    private String author;
    private String genre;
    private Integer availableQuantity;
    private Long price;

    public Book() {
    }

    public Book(String bookId, String title, String author, String genre, Integer availableQuantity, Long price) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.genre = genre;
        setAvailableQuantity(availableQuantity);
        setPrice(price);
    }

    @Override
    public boolean canBorrow() {
        return canBorrow(1);
    }

    @Override
    public boolean canBorrow(int quantity) {
        return quantity > 0 && this.availableQuantity != null && this.availableQuantity >= quantity;
    }

    @Override
    public void borrow() {
        borrow(1);
    }

    @Override
    public void borrow(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Lỗi: Số lượng mượn phải lớn hơn 0!");
        }
        if (!canBorrow(quantity)) {
            throw new IllegalStateException(
                    String.format("Lỗi: Không đủ sách để mượn! (Yêu cầu: %d, Hiện có: %d)", quantity, this.availableQuantity)
            );
        }
        this.availableQuantity -= quantity;
    }

    @Override
    public void returnItem() {
        returnItem(1);
    }

    @Override
    public void returnItem(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Lỗi: Số lượng trả phải lớn hơn 0!");
        }
        this.availableQuantity += quantity;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        if (availableQuantity == null || availableQuantity < 0) {
            throw new IllegalArgumentException("Lỗi: Số lượng sách hiện có không được âm hoặc null!");
        }
        this.availableQuantity = availableQuantity;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Lỗi: Giá trị sách không được âm!");
        }
        this.price = price;
    }
}