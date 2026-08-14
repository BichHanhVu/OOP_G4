package com.group4.library.repository;

import com.group4.library.model.BorrowTicket;
import com.group4.library.model.TicketStatus;
import org.springframework.stereotype.Repository; // 1. Import dòng này

import java.util.List;
import java.util.Optional;

@Repository // 2. Thêm Annotation này ở đây!
public interface BorrowTicketRepository {
    BorrowTicket save(BorrowTicket ticket);
    Optional<BorrowTicket> findById(String id);
    List<BorrowTicket> findByReaderIdAndStatus(String readerId, TicketStatus status);
    List<BorrowTicket> findByReaderId(String readerId);
    List<BorrowTicket> findByStatus(TicketStatus status);
    List<BorrowTicket> findAll();
}