package com.group4.library.service;

import com.group4.library.dto.ReaderImportRowResult;
import com.group4.library.dto.ReaderImportSummary;
import com.group4.library.dto.ReaderRequest;
import com.group4.library.dto.ReaderResponse;
import com.group4.library.exception.BusinessException;
import com.group4.library.utils.CsvUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReaderCsvService {

    private static final Logger log = LoggerFactory.getLogger(ReaderCsvService.class);
    private static final List<String> EXPECTED_HEADER = List.of("id", "name", "phoneNumber", "type");

    private final ReaderService readerService;

    public ReaderCsvService(ReaderService readerService) {
        this.readerService = readerService;
    }

    public ReaderImportSummary importFromCsv(InputStream inputStream) {
        List<ReaderImportRowResult> results = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            validateHeader(headerLine);

            String line;
            int rowNumber = 1;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                rowNumber++;
                results.add(importRow(rowNumber, line));
            }
        } catch (IOException e) {
            throw new BusinessException("Không đọc được file CSV: " + e.getMessage());
        }

        long successCount = results.stream().filter(ReaderImportRowResult::isSuccess).count();
        log.info("Import CSV hoàn tất: {} dòng, {} thành công, {} lỗi",
                results.size(), successCount, results.size() - successCount);

        return new ReaderImportSummary(results);
    }

    public String exportToCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("id,name,phoneNumber,type,maxBorrowLimit\n");

        for (ReaderResponse r : readerService.getAllForExport()) {
            csv.append(String.join(",",
                            CsvUtils.escapeField(r.getId()),
                            CsvUtils.escapeField(r.getName()),
                            CsvUtils.escapeField(r.getPhoneNumber()),
                            CsvUtils.escapeField(r.getType()),
                            String.valueOf(r.getMaxBorrowLimit())))
                    .append("\n");
        }

        return csv.toString();
    }

    private ReaderImportRowResult importRow(int rowNumber, String line) {
        try {
            List<String> fields = CsvUtils.parseLine(line);
            if (fields.size() < 4) {
                return ReaderImportRowResult.failure(rowNumber,
                        "Dòng thiếu cột, cần đủ 4 cột: id,name,phoneNumber,type");
            }

            ReaderRequest request = new ReaderRequest();
            request.setId(blankToNull(fields.get(0)));
            request.setName(fields.get(1));
            request.setPhoneNumber(fields.get(2));
            request.setType(fields.get(3).trim());

            ReaderResponse created = readerService.create(request);
            return ReaderImportRowResult.success(rowNumber, created);
        } catch (BusinessException e) {
            return ReaderImportRowResult.failure(rowNumber, e.getMessage());
        } catch (Exception e) {
            return ReaderImportRowResult.failure(rowNumber, "Lỗi không xác định: " + e.getMessage());
        }
    }

    private void validateHeader(String headerLine) {
        if (headerLine == null) {
            throw new BusinessException("File CSV rỗng, thiếu dòng tiêu đề");
        }
        List<String> header = CsvUtils.parseLine(headerLine).stream().map(String::trim).toList();
        if (header.size() < 4 || !header.subList(0, 4).equals(EXPECTED_HEADER)) {
            throw new BusinessException("Tiêu đề CSV không đúng, cần đúng thứ tự: id,name,phoneNumber,type");
        }
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}