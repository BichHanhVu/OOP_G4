package com.group4.library.handler;

import com.group4.library.dto.BorrowRequest;
import com.group4.library.dto.BorrowTicketResponse;
import com.group4.library.model.TicketStatus;
import com.group4.library.service.BorrowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrow-tickets")
public class BorrowHandler {

    private final BorrowService borrowService;

    // Inject BorrowService qua Constructor
    public BorrowHandler(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    /**
     * POST /api/borrow-tickets
     * Tạo phiếu mượn sách mới
     */
    @PostMapping
    public ResponseEntity<BorrowTicketResponse> createBorrowTicket(@RequestBody BorrowRequest request) {
        BorrowTicketResponse response = borrowService.createBorrowTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/borrow-tickets
     * Lấy danh sách tất cả phiếu mượn (Hỗ trợ lọc theo readerId và status)
     * Ví dụ: /api/borrow-tickets?readerId=R001&status=BORROWING
     */
    @GetMapping
    public ResponseEntity<List<BorrowTicketResponse>> getAllTickets(
            @RequestParam(required = false) String readerId,
            @RequestParam(required = false) TicketStatus status) {
        List<BorrowTicketResponse> tickets = borrowService.getAllTickets(readerId, status);
        return ResponseEntity.ok(tickets);
    }

    /**
     * GET /api/borrow-tickets/{id}
     * Tìm/Lấy thông tin chi tiết phiếu mượn theo Mã phiếu (id)
     */
    @GetMapping("/{id}")
    public ResponseEntity<BorrowTicketResponse> getTicketById(@PathVariable("id") String id) {
        BorrowTicketResponse ticket = borrowService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }
}