// repository/json/JsonReaderRepository.java
package com.group4.library.repository.json;

import com.group4.library.model.LecturerReader;
import com.group4.library.model.PriorityStudentReader;
import com.group4.library.model.Reader;
import com.group4.library.model.StudentReader;
import com.group4.library.repository.ReaderRepository;
import com.group4.library.utils.JsonFileUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JsonReaderRepository implements ReaderRepository {

    private final String filePath;

    public JsonReaderRepository() {
        this("data/readers.json");
    }

    // Constructor phụ dùng cho test, trỏ tới file tạm thay vì data/readers.json thật
    public JsonReaderRepository(String filePath) {
        this.filePath = filePath;
    }

    static class ReaderRecord {
        public String id;
        public String name;
        public String phoneNumber;
        public String type;

        public ReaderRecord() {}
        public ReaderRecord(String id, String name, String phoneNumber, String type) {
            this.id = id;
            this.name = name;
            this.phoneNumber = phoneNumber;
            this.type = type;
        }
    }

    @Override
    public List<Reader> findAll() {
        return JsonFileUtils.readList(filePath, ReaderRecord.class)
                .stream().map(this::toModel).collect(Collectors.toList());
    }

    @Override
    public Optional<Reader> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return findAll().stream()
                .filter(r -> id.equalsIgnoreCase(r.getId()))
                .findFirst();
    }

    @Override
    public Reader save(Reader reader) {
        List<ReaderRecord> records = JsonFileUtils.readList(filePath, ReaderRecord.class);
        records.removeIf(r -> reader.getId().equalsIgnoreCase(r.id));
        records.add(toRecord(reader));
        JsonFileUtils.writeList(filePath, records);
        return reader;
    }

    @Override
    public void deleteById(String id) {
        if (id == null) return;
        List<ReaderRecord> records = JsonFileUtils.readList(filePath, ReaderRecord.class);
        records.removeIf(r -> id.equalsIgnoreCase(r.id));
        JsonFileUtils.writeList(filePath, records);
    }

    @Override
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    private Reader toModel(ReaderRecord r) {
        if (r.id == null || r.id.isBlank()) {
            throw new IllegalStateException("Bản ghi bạn đọc không hợp lệ: thiếu id");
        }
        if (r.type == null || r.type.isBlank()) {
            throw new IllegalStateException(
                    "Bản ghi bạn đọc không hợp lệ (id=" + r.id + "): thiếu type");
        }
        return switch (r.type) {
            case "STUDENT" -> new StudentReader(r.id, r.name, r.phoneNumber);
            case "PRIORITY_STUDENT" -> new PriorityStudentReader(r.id, r.name, r.phoneNumber);
            case "LECTURER" -> new LecturerReader(r.id, r.name, r.phoneNumber);
            default -> throw new IllegalStateException(
                    "Loại bạn đọc không hợp lệ (id=" + r.id + "): " + r.type);
        };
    }

    private ReaderRecord toRecord(Reader reader) {
        return new ReaderRecord(reader.getId(), reader.getName(), reader.getPhoneNumber(),
                reader.getType().name());
    }
}