package service;

import dto.BookRequest;
import dto.BookResponse;
import model.Book;
import repository.BookRepository;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public BookResponse getBookByCode(String code) {
        Book book = bookRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách với mã: " + code));
        return convertToResponse(book);
    }

    public BookResponse addBook(BookRequest request) {
        if (bookRepository.findByCode(request.getCode()).isPresent()) {
            throw new IllegalArgumentException("Lỗi: Mã sách '" + request.getCode() + "' đã tồn tại!");
        }

        validateBookData(request.getAvailableQuantity(), request.getPrice());

        Book book = new Book(
                request.getCode(),
                request.getTitle(),
                request.getAuthor(),
                request.getGenre(),
                request.getAvailableQuantity(),
                request.getPrice()
        );

        bookRepository.save(book);
        return convertToResponse(book);
    }

    public BookResponse updateBook(String code, BookRequest request) {
        Book existingBook = bookRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Lỗi: Không tìm thấy sách để cập nhật!"));

        validateBookData(request.getAvailableQuantity(), request.getPrice());

        existingBook.setTitle(request.getTitle());
        existingBook.setAuthor(request.getAuthor());
        existingBook.setGenre(request.getGenre());
        existingBook.setAvailableQuantity(request.getAvailableQuantity());
        existingBook.setPrice(request.getPrice());

        bookRepository.update(existingBook);
        return convertToResponse(existingBook);
    }

    private void validateBookData(int quantity, double price) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Lỗi: Số lượng sách không được nhỏ hơn 0!");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Lỗi: Giá trị sách không được nhỏ hơn 0!");
        }
    }

    private BookResponse convertToResponse(Book book) {
        return new BookResponse(
                book.getCode(),
                book.getTitle(),
                book.getAuthor(),
                book.getGenre(),
                book.getAvailableQuantity(),
                book.getPrice()
        );
    }

    public void deleteBook(String code) {
        Book book = bookRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Lỗi: Không tìm thấy sách để xóa!"));

        if (isBookCurrentlyBorrowed(code)) {
            throw new IllegalStateException("Lỗi: Không thể xóa sách đang nằm trong phiếu mượn chưa trả!");
        }

        bookRepository.deleteByCode(code);
    }

    private boolean isBookCurrentlyBorrowed(String bookCode) {
        return false;
    }

    public String exportBooks() {
        List<BookResponse> books = getAllBooks();
        StringBuilder csv = new StringBuilder();
        csv.append("\uFEFF");
        csv.append("Mã sách,Tên sách,Tác giả,Thể loại,Số lượng,Giá\n");

        for (BookResponse b : books) {
            csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%d,%.2f\n",
                    b.getCode(), b.getTitle(), b.getAuthor(), b.getGenre(),
                    b.getAvailableQuantity(), b.getPrice()));
        }
        return csv.toString();
    }

    public int importBooks(InputStream inputStream) throws Exception {
        int importedCount = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    if (line.startsWith("\uFEFF")) line = line.substring(1);
                    isHeader = false;
                    continue;
                }

                if (line.trim().isEmpty()) continue;

                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (values.length < 6) continue;

                String code = values[0].replace("\"", "").trim();
                String title = values[1].replace("\"", "").trim();
                String author = values[2].replace("\"", "").trim();
                String genre = values[3].replace("\"", "").trim();
                int quantity = Integer.parseInt(values[4].trim());
                double price = Double.parseDouble(values[5].trim());

                BookRequest request = new BookRequest();
                request.setCode(code);
                request.setTitle(title);
                request.setAuthor(author);
                request.setGenre(genre);
                request.setAvailableQuantity(quantity);
                request.setPrice(price);

                if (bookRepository.findByCode(code).isPresent()) {
                    updateBook(code, request);
                } else {
                    addBook(request);
                }
                importedCount++;
            }
        }
        return importedCount;
    }
}