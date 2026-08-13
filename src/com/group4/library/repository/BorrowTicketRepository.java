package com.group4.library.repository;

import com.group4.library.model.BorrowTicket;
import com.group4.library.model.TicketStatus;

import java.util.List;
import java.util.Optional;

public interface BorrowTicketRepository {

    // 1. Lưu hoặc cập nhật phiếu mượn
    BorrowTicket save(BorrowTicket ticket);

    // 2. Tìm phiếu mượn theo mã phiếu (ticketId)
    Optional<BorrowTicket> findById(String id);

    // 3. Lấy tất cả danh sách phiếu mượn
    List<BorrowTicket> findAll();

    // 4. Tìm danh sách phiếu mượn theo Mã bạn đọc (readerId)
    // Phục vụ rule: Kiểm tra bạn đọc có còn phiếu mượn không trước khi xóa bạn đọc
    List<BorrowTicket> findByReaderId(String readerId);

    // 5. Tìm danh sách phiếu mượn theo Trạng thái (TicketStatus)
    List<BorrowTicket> findByStatus(TicketStatus status);

    // 6. Tìm các phiếu mượn có trạng thái BORROWING của một bạn đọc
    // Phục vụ tính tổng số sách đang mượn để kiểm tra giới hạn mượn (Borrow Limit)
    List<BorrowTicket> findByReaderIdAndStatus(String readerId, TicketStatus status);
}