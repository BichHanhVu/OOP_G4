package com.group4.library.repository.json;

import com.group4.library.model.BorrowTicket;
import com.group4.library.model.TicketStatus;
import com.group4.library.repository.BorrowTicketRepository;
import com.group4.library.utils.JsonFileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonBorrowTicketRepository implements BorrowTicketRepository {

    // Đường dẫn file JSON được chốt trong cấu trúc dự án
    private static final String FILE_PATH = "data/borrow-tickets.json";

    private final List<BorrowTicket> borrowTickets;

    public JsonBorrowTicketRepository() {
        // Đọc dữ liệu từ file JSON thông qua JsonFileUtils thay vì khởi tạo danh sách rỗng
        List<BorrowTicket> loadedTickets = JsonFileUtils.readListFromFile(FILE_PATH, BorrowTicket.class);
        this.borrowTickets = loadedTickets != null ? loadedTickets : new ArrayList<>();
    }

    @Override
    public List<BorrowTicket> findAll() {
        return new ArrayList<>(borrowTickets);
    }

    @Override
    public Optional<BorrowTicket> findById(String ticketId) {
        if (ticketId == null || ticketId.trim().isEmpty()) {
            return Optional.empty();
        }

        // So sánh null-safe và không phân biệt chữ hoa/thường theo góp ý
        return borrowTickets.stream()
                .filter(ticket -> ticket != null && ticket.getTicketId() != null)
                .filter(ticket -> ticket.getTicketId().equalsIgnoreCase(ticketId.trim()))
                .findFirst();
    }

    @Override
    public List<BorrowTicket> findByReaderId(String readerId) {
        if (readerId == null || readerId.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return borrowTickets.stream()
                .filter(ticket -> ticket != null && ticket.getReaderId() != null)
                .filter(ticket -> ticket.getReaderId().equalsIgnoreCase(readerId.trim()))
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowTicket> findByStatus(TicketStatus status) {
        if (status == null) {
            return new ArrayList<>();
        }

        return borrowTickets.stream()
                .filter(ticket -> ticket != null && ticket.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowTicket> findByReaderIdAndStatus(String readerId, TicketStatus status) {
        if (readerId == null || status == null) {
            return new ArrayList<>();
        }

        return borrowTickets.stream()
                .filter(ticket -> ticket != null
                        && ticket.getReaderId() != null
                        && ticket.getReaderId().equalsIgnoreCase(readerId.trim())
                        && ticket.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public synchronized BorrowTicket save(BorrowTicket borrowTicket) {
        if (borrowTicket == null || borrowTicket.getTicketId() == null) {
            throw new IllegalArgumentException("Không thể lưu phiếu mượn không hợp lệ hoặc thiếu mã ticketId");
        }

        // Kiểm tra xem phiếu mượn đã tồn tại chưa để cập nhật hoặc thêm mới
        Optional<BorrowTicket> existingTicket = findById(borrowTicket.getTicketId());
        existingTicket.ifPresent(borrowTickets::remove);

        borrowTickets.add(borrowTicket);

        // Lưu toàn bộ danh sách xuống file JSON để đảm bảo tính lưu trữ dữ liệu (File IO)
        saveToFile();

        return borrowTicket;
    }

    /**
     * Phương thức hỗ trợ ghi danh sách phiếu mượn hiện tại ra file JSON
     */
    private void saveToFile() {
        JsonFileUtils.writeListToFile(FILE_PATH, this.borrowTickets);
    }
}