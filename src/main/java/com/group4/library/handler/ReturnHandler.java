package com.group4.library.handler;

import com.group4.library.dto.ReturnRequest;
import com.group4.library.dto.ReturnResponse;
import com.group4.library.service.ReturnService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/returns")
public class ReturnHandler {
    private final ReturnService returnService;

    public ReturnHandler(ReturnService returnService) { this.returnService = returnService; }

    @GetMapping
    public ResponseEntity<List<ReturnResponse>> getAll() {
        return ResponseEntity.ok(returnService.getAll());
    }

    @GetMapping("/unpaid")
    public ResponseEntity<List<ReturnResponse>> getUnpaidFines() {
        return ResponseEntity.ok(returnService.getUnpaidFines());
    }

    @PostMapping
    public ResponseEntity<ReturnResponse> returnBooks(@RequestBody ReturnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(returnService.returnBooks(request));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<ReturnResponse> payFine(@PathVariable("id") String id) {
        return ResponseEntity.ok(returnService.payFine(id));
    }
}