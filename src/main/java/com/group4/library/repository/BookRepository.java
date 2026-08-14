package com.group4.library.repository;

import com.group4.library.model.Book;
import java.util.List;
import java.util.Optional;

public interface BookRepository {
    List<Book> findAll();
    Optional<Book> findById(String bookId);
    List<Book> findByTitleContaining(String titleKeyword);
    List<Book> searchByIdOrTitle(String keyword);

    void save(Book book);

    boolean update(Book updatedBook);

    boolean deleteByCode(String code);
}