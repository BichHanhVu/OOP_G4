package com.group4.library.repository.json;

import com.group4.library.model.BorrowTicket;
import com.group4.library.repository.BorrowTicketRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonBorrowTicketRepository implements BorrowTicketRepository {

    private final List<BorrowTicket> borrowTickets;

    public JsonBorrowTicketRepository() {
        this.borrowTickets = new ArrayList<>();
    }

    @Override
    public List<BorrowTicket> findAll() {
        return new ArrayList<>(borrowTickets);
    }

    @Override
    public Optional<BorrowTicket> findById(String ticketId) {
        return borrowTickets.stream()
                .filter(ticket -> ticket.getTicketId().equals(ticketId))
                .findFirst();
    }

    @Override
    public BorrowTicket save(BorrowTicket borrowTicket) {
        Optional<BorrowTicket> existingTicket = findById(borrowTicket.getTicketId());

        if (existingTicket.isPresent()) {
            borrowTickets.remove(existingTicket.get());
        }

        borrowTickets.add(borrowTicket);

        return borrowTicket;
    }
}