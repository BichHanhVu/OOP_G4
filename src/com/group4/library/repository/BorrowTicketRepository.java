package com.group4.library.repository;

import com.group4.library.model.BorrowTicket;
import com.group4.library.model.TicketStatus;

import java.util.List;
import java.util.Optional;

public interface BorrowTicketRepository {
    List<BorrowTicket> findAll();
    Optional<BorrowTicket> findById(String ticketId);
    List<BorrowTicket> findByReaderId(String readerId);
    List<BorrowTicket> findByStatus(TicketStatus status);
    List<BorrowTicket> findByReaderIdAndStatus(String readerId, TicketStatus status);
    BorrowTicket save(BorrowTicket borrowTicket);
}