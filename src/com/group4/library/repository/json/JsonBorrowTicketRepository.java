package com.group4.library.repository.json;
import com.group4.library.repository.BorrowTicketRepository;


import com.group4.library.model.BorrowTicket;
import com.group4.library.model.TicketStatus;
// // Code của Tiệp: Import class tiện ích đọc/ghi JSON dùng chung
// import com.group4.library.util.JsonFileUtils;

import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JsonBorrowTicketRepository implements BorrowTicketRepository {

    // Đường dẫn chuẩn thống nhất theo dự án
    private static final String FILE_PATH = "data/borrow-tickets.json";
    private final List<BorrowTicket> tickets = new ArrayList<>();

    public JsonBorrowTicketRepository() {
        // Tự động kiểm tra/tạo thư mục 'data' nếu chưa có
        File dir = new File("data");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Load dữ liệu cũ từ JSON ngay khi khởi tạo (không để danh sách rỗng)
        loadDataFromFile();
    }

    /**
     * Đọc toàn bộ dữ liệu từ file data/borrow-tickets.json
     */
    private synchronized void loadDataFromFile() {
        tickets.clear();
        /* // Code của Tiệp: Đọc danh sách từ file JSON dùng chung
        List<BorrowTicket> loaded = JsonFileUtils.readList(FILE_PATH, BorrowTicket.class);
        if (loaded != null) {
            tickets.addAll(loaded);
        }
        */
    }

    /**
     * Ghi toàn bộ danh sách trở lại file data/borrow-tickets.json sau khi thay đổi
     */
    private synchronized void saveDataToFile() {
        /* // Code của Tiệp: Ghi danh sách xuống file JSON dùng chung
        JsonFileUtils.writeList(FILE_PATH, tickets);
        */
    }

    @Override
    public synchronized BorrowTicket save(BorrowTicket ticket) {
        if (ticket == null || ticket.getTicketId() == null || ticket.getTicketId().trim().isEmpty()) {
            throw new IllegalArgumentException("Phiếu mượn và Mã phiếu (ticketId) không được để trống!");
        }

        // So sánh Null-safe và không phân biệt hoa/thường (IgnoreCase)
        int existingIndex = -1;
        for (int i = 0; i < tickets.size(); i++) {
            BorrowTicket current = tickets.get(i);
            if (current != null && current.getTicketId() != null
                    && current.getTicketId().trim().equalsIgnoreCase(ticket.getTicketId().trim())) {
                existingIndex = i;
                break;
            }
        }

        if (existingIndex >= 0) {
            tickets.set(existingIndex, ticket); // Cập nhật
        } else {
            tickets.add(ticket); // Thêm mới
        }

        // Ghi lại toàn bộ danh sách xuống file JSON sau khi lưu/cập nhật
        saveDataToFile();

        return ticket;
    }

    @Override
    public synchronized Optional<BorrowTicket> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }

        // Nạp lại dữ liệu để đảm bảo tính đồng bộ
        loadDataFromFile();

        // So sánh Null-safe & không phân biệt chữ hoa/thường
        return tickets.stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getTicketId() != null && t.getTicketId().trim().equalsIgnoreCase(id.trim()))
                .findFirst();
    }

    @Override
    public synchronized List<BorrowTicket> findAll() {
        loadDataFromFile();
        return new ArrayList<>(tickets);
    }

    @Override
    public synchronized List<BorrowTicket> findByReaderId(String readerId) {
        if (readerId == null || readerId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        loadDataFromFile();

        // So sánh Null-safe & không phân biệt chữ hoa/thường
        return tickets.stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getReaderId() != null && t.getReaderId().trim().equalsIgnoreCase(readerId.trim()))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized List<BorrowTicket> findByStatus(TicketStatus status) {
        if (status == null) {
            return new ArrayList<>();
        }
        loadDataFromFile();

        return tickets.stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public synchronized List<BorrowTicket> findByReaderIdAndStatus(String readerId, TicketStatus status) {
        if (readerId == null || readerId.trim().isEmpty() || status == null) {
            return new ArrayList<>();
        }
        loadDataFromFile();

        // So sánh Null-safe & không phân biệt chữ hoa/thường
        return tickets.stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getReaderId() != null && t.getReaderId().trim().equalsIgnoreCase(readerId.trim()))
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());
    }
}