package com.group4.library.repository;

import com.group4.library.model.BorrowTicket;
import com.group4.library.model.TicketStatus;

import java.util.List;
import java.util.Optional;

public interface BorrowTicketRepository {

    BorrowTicket save(BorrowTicket ticket);

    Optional<BorrowTicket> findById(String id);

    List<BorrowTicket> findByReaderIdAndStatus(
            String readerId,
            TicketStatus status
    );

    List<BorrowTicket> findByReaderId(String readerId);

    List<BorrowTicket> findByStatus(TicketStatus status);

    List<BorrowTicket> findAll();
}