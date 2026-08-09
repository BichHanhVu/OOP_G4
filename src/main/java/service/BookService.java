package service;

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