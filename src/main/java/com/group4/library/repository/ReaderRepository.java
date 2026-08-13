package com.group4.library.repository;

import com.group4.library.model.Reader;

import java.util.List;
import java.util.Optional;

public interface ReaderRepository {
    List<Reader> findAll();
    Optional<Reader> findById(String id);
    Reader save(Reader reader);
    void deleteById(String id);
    boolean existsById(String id);
}
