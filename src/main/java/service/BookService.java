package service;

import dto.BookRequest;
import dto.BookResponse;
import model.Book;
import repository.BookRepository;

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
}