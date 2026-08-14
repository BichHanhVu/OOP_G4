package com.group4.library.repository.json;

import com.group4.library.model.BorrowTicket;
import com.group4.library.model.TicketStatus;
import com.group4.library.repository.BorrowTicketRepository;
import com.group4.library.utils.JsonFileUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JsonBorrowTicketRepository implements BorrowTicketRepository {

    private static final String FILE_PATH = "data/borrow-tickets.json";
    private final List<BorrowTicket> tickets = new ArrayList<>();

    public JsonBorrowTicketRepository() {
        loadDataFromFile();
    }

    private synchronized void loadDataFromFile() {
        tickets.clear();
        tickets.addAll(
                JsonFileUtils.readList(FILE_PATH, BorrowTicket.class)
        );
    }

    private synchronized void saveDataToFile() {
        JsonFileUtils.writeList(FILE_PATH, tickets);
    }

    @Override
    public synchronized BorrowTicket save(BorrowTicket ticket) {
        if (ticket == null
                || ticket.getTicketId() == null
                || ticket.getTicketId().isBlank()) {
            throw new IllegalArgumentException("Phiếu mượn và mã phiếu không được để trống");
        }

        loadDataFromFile();

        int existingIndex = -1;

        for (int i = 0; i < tickets.size(); i++) {
            BorrowTicket current = tickets.get(i);

            if (current != null
                    && current.getTicketId() != null
                    && current.getTicketId().equalsIgnoreCase(ticket.getTicketId())) {
                existingIndex = i;
                break;
            }
        }

        if (existingIndex >= 0) {
            tickets.set(existingIndex, ticket);
        } else {
            tickets.add(ticket);
        }

        saveDataToFile();
        return ticket;
    }

    @Override
    public Optional<BorrowTicket> findById(String ticketId) {
        loadDataFromFile();
        return tickets.stream()
                .filter(t -> t.getTicketId().equals(ticketId))
                .findFirst();
    }

    @Override
    public List<BorrowTicket> findAll() {
        loadDataFromFile();
        return new ArrayList<>(tickets);
    }

    @Override
    public List<BorrowTicket> findByReaderId(String readerId) {
        loadDataFromFile();
        return tickets.stream()
                .filter(t -> t.getReaderId().equals(readerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowTicket> findByStatus(TicketStatus status) {
        loadDataFromFile();
        return tickets.stream()
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowTicket> findByReaderIdAndStatus(String readerId, TicketStatus status) {
        loadDataFromFile();
        return tickets.stream()
                .filter(t -> t.getReaderId().equals(readerId) && t.getStatus() == status)
                .collect(Collectors.toList());
    }
}