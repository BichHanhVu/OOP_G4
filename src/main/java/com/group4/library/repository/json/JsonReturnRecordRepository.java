package com.group4.library.repository.json;

import com.group4.library.model.ReturnRecord;
import com.group4.library.repository.ReturnRecordRepository;
import com.group4.library.utils.JsonFileUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class JsonReturnRecordRepository implements ReturnRecordRepository {
    private static final String FILE_PATH = "data/return-records.json";

    @Override
    public List<ReturnRecord> findAll() {
        return JsonFileUtils.readList(FILE_PATH, ReturnRecord.class);
    }

    @Override
    public Optional<ReturnRecord> findById(String returnId) {
        return findAll().stream()
                .filter(r -> Objects.equals(r.getReturnId(), returnId))
                .findFirst();
    }

    @Override
    public ReturnRecord save(ReturnRecord record) {
        List<ReturnRecord> records = findAll();
        records.removeIf(r -> Objects.equals(r.getReturnId(), record.getReturnId()));
        records.add(record);
        JsonFileUtils.writeList(FILE_PATH, records);
        return record;
    }
}
