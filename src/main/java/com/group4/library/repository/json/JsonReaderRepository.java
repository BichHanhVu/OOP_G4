package com.group4.library.repository.json;

import com.group4.library.model.LecturerReader;
import com.group4.library.model.PriorityStudentReader;
import com.group4.library.model.Reader;
import com.group4.library.model.StudentReader;
import com.group4.library.repository.ReaderRepository;
import com.group4.library.utils.JsonFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JsonReaderRepository implements ReaderRepository {

    private static final Logger log = LoggerFactory.getLogger(JsonReaderRepository.class);

    @Value("${reader.data.file:data/readers.json}")
    private String filePath = "data/readers.json";

    public void setFilePathForTest(String filePath) {
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
        List<Reader> readers = JsonFileUtils.readList(filePath, ReaderRecord.class)
                .stream().map(this::toModel).collect(Collectors.toList());
        log.debug("Đọc {} bạn đọc từ {}", readers.size(), filePath);
        return readers;
    }

    @Override
    public Optional<Reader> findById(String id) {
        return findAll().stream().filter(r -> r.getId().equals(id)).findFirst();
    }

    @Override
    public Reader save(Reader reader) {
        List<ReaderRecord> records = JsonFileUtils.readList(filePath, ReaderRecord.class);
        records.removeIf(r -> r.id.equals(reader.getId()));
        records.add(toRecord(reader));
        JsonFileUtils.writeList(filePath, records);
        log.info("Đã lưu bạn đọc {}", reader.getId());
        return reader;
    }

    @Override
    public void deleteById(String id) {
        List<ReaderRecord> records = JsonFileUtils.readList(filePath, ReaderRecord.class);
        records.removeIf(r -> r.id.equals(id));
        JsonFileUtils.writeList(filePath, records);
        log.info("Đã xóa bạn đọc {}", id);
    }

    @Override
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    private Reader toModel(ReaderRecord r) {
        return switch (r.type) {
            case "STUDENT" -> new StudentReader(r.id, r.name, r.phoneNumber);
            case "PRIORITY_STUDENT" -> new PriorityStudentReader(r.id, r.name, r.phoneNumber);
            case "LECTURER" -> new LecturerReader(r.id, r.name, r.phoneNumber);
            default -> throw new IllegalStateException("Loại bạn đọc không hợp lệ: " + r.type);
        };
    }

    private ReaderRecord toRecord(Reader reader) {
        return new ReaderRecord(reader.getId(), reader.getName(), reader.getPhoneNumber(), reader.getType().name());
    }
}