package com.group4.library.handler;

import com.group4.library.dto.PagedReaderResponse;
import com.group4.library.dto.ReaderImportSummary;
import com.group4.library.dto.ReaderRequest;
import com.group4.library.dto.ReaderResponse;
import com.group4.library.dto.ReaderSearchRequest;
import com.group4.library.exception.BusinessException;
import com.group4.library.service.ReaderCsvService;
import com.group4.library.service.ReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/readers")
public class ReaderHandler {

    private static final Logger log = LoggerFactory.getLogger(ReaderHandler.class);

    private final ReaderService readerService;
    private final ReaderCsvService readerCsvService;

    public ReaderHandler(ReaderService readerService, ReaderCsvService readerCsvService) {
        this.readerService = readerService;
        this.readerCsvService = readerCsvService;
    }

    @GetMapping
    public ResponseEntity<PagedReaderResponse<ReaderResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        ReaderSearchRequest request = new ReaderSearchRequest(keyword, type, sortBy, sortDirection, page, size);
        return ResponseEntity.ok(readerService.search(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReaderResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(readerService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ReaderResponse> create(@RequestBody ReaderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(readerService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReaderResponse> update(@PathVariable String id, @RequestBody ReaderRequest request) {
        return ResponseEntity.ok(readerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        readerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ResponseEntity<ReaderImportSummary> importCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("Vui lòng chọn file CSV để import");
        }
        try {
            log.info("Nhận file import CSV: {}", file.getOriginalFilename());
            return ResponseEntity.ok(readerCsvService.importFromCsv(file.getInputStream()));
        } catch (IOException e) {
            throw new BusinessException("Không đọc được file: " + e.getMessage());
        }
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportCsv() {
        String csv = readerCsvService.exportToCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=readers.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}