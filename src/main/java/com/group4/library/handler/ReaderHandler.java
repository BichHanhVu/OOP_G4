package com.group4.library.handler;

import com.group4.library.dto.ReaderRequest;
import com.group4.library.dto.ReaderResponse;
import com.group4.library.service.ReaderService;
import org.springframework.http.HttpStatus;
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

import java.util.List;

@RestController
@RequestMapping("/api/readers")
public class ReaderHandler {

    private final ReaderService readerService;

    public ReaderHandler(ReaderService readerService) {
        this.readerService = readerService;
    }

    @GetMapping
    public ResponseEntity<List<ReaderResponse>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(readerService.getAll(keyword, type));
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
}
