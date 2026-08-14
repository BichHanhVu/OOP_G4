package com.group4.library.repository;

import com.group4.library.model.ReturnRecord;
import java.util.List;
import java.util.Optional;

public interface ReturnRecordRepository {
    List<ReturnRecord> findAll();
    Optional<ReturnRecord> findById(String returnId);
    ReturnRecord save(ReturnRecord record);
}
