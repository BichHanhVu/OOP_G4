package com.group4.library.service;

import com.group4.library.model.Reader;
import com.group4.library.repository.ReaderRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryReaderRepository implements ReaderRepository {

    private final Map<String, Reader> store = new LinkedHashMap<>();

    @Override
    public List<Reader> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Reader> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Reader save(Reader reader) {
        store.put(reader.getId(), reader);
        return reader;
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }

    @Override
    public boolean existsById(String id) {
        return store.containsKey(id);
    }
}
