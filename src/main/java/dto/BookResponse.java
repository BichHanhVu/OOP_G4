package dto;

public class BookResponse {
    private String code;
    private String title;
    private String author;
    private String genre;
    private int availableQuantity;
    private double price;

    public BookResponse() {
    }

    public BookResponse(String code, String title, String author, String genre, int availableQuantity, double price) {
        this.code = code;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.availableQuantity = availableQuantity;
        this.price = price;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}