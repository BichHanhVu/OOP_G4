package com.group4.library.repository;

import com.group4.library.model.BorrowTicket;

import java.util.List;
import java.util.Optional;

public interface BorrowTicketRepository {

    List<BorrowTicket> findAll();

    Optional<BorrowTicket> findById(String ticketId);

    BorrowTicket save(BorrowTicket borrowTicket);
}