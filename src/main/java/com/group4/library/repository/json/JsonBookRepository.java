package com.group4.library.repository.json;

import com.group4.library.model.Book;
import com.group4.library.repository.BookRepository;
import com.group4.library.utils.JsonFileUtils;

import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class JsonBookRepository implements BookRepository {
    private static final String FILE_PATH = "data/books.json";

    public JsonBookRepository() {
    }

    @Override
    public List<Book> findAll() {
        return JsonFileUtils.readList(FILE_PATH, Book.class);
    }

    @Override
    public Optional<Book> findById(String bookId) {
        if (bookId == null || bookId.trim().isEmpty()) {
            return Optional.empty();
        }
        return findAll().stream()
                .filter(book -> book != null && bookId.equalsIgnoreCase(book.getBookId()))
                .findFirst();
    }

    @Override
    public List<Book> findByTitleContaining(String titleKeyword) {
        if (titleKeyword == null || titleKeyword.trim().isEmpty()) {
            return findAll();
        }
        String lowerKeyword = titleKeyword.trim().toLowerCase();
        return findAll().stream()
                .filter(book -> book != null && book.getTitle() != null && book.getTitle().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> searchByIdOrTitle(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        String lowerKeyword = keyword.trim().toLowerCase();
        return findAll().stream()
                .filter(book -> book != null && (
                        (book.getBookId() != null && book.getBookId().toLowerCase().contains(lowerKeyword)) ||
                                (book.getTitle() != null && book.getTitle().toLowerCase().contains(lowerKeyword))
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void save(Book book) {
        Objects.requireNonNull(book, "Lỗi: Dữ liệu sách không được null!");
        if (book.getBookId() == null || book.getBookId().trim().isEmpty()) {
            throw new IllegalArgumentException("Lỗi: Mã sách không được để trống!");
        }

        List<Book> books = findAll();
        books.add(book);
        JsonFileUtils.writeList(FILE_PATH, books);
    }

    @Override
    public boolean update(Book updatedBook) {
        if (updatedBook == null || updatedBook.getBookId() == null || updatedBook.getBookId().trim().isEmpty()) {
            throw new IllegalArgumentException("Lỗi: Mã sách cập nhật không hợp lệ!");
        }

        List<Book> books = findAll();
        boolean found = false;

        for (int i = 0; i < books.size(); i++) {
            Book currentBook = books.get(i);
            if (currentBook != null && updatedBook.getBookId().equalsIgnoreCase(currentBook.getBookId())) {
                books.set(i, updatedBook);
                found = true;
                break;
            }
        }

        if (found) {
            JsonFileUtils.writeList(FILE_PATH, books);
        }

        return found;
    }

    @Override
    public boolean deleteByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }

        List<Book> books = findAll();
        boolean removed = books.removeIf(book -> book != null && code.equalsIgnoreCase(book.getBookId()));

        if (removed) {
            JsonFileUtils.writeList(FILE_PATH, books);
        }

        return removed;
    }
}