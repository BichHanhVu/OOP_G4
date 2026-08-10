package repository;

import model.Book;
import java.util.List;
import java.util.Optional;

public interface BookRepository {
    List<Book> findAll();
    Optional<Book> findByCode(String code);
    void save(Book book);
    void update(Book book);
}