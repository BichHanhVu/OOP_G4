package main;

import com.group4.library.dto.BorrowItemRequest;
import com.group4.library.dto.BorrowRequest;
import com.group4.library.dto.BorrowTicketResponse;
import main.java.com.group4.library.repository.BorrowTicketRepository;
import main.java.com.group4.library.repository.json.JsonBorrowTicketRepository;
import main.java.com.group4.library.service.BorrowService;

import java.time.LocalDate;
import java.util.List;

public class MainTest {
    public static void main(String[] args) {
        // 1. Khởi tạo Repository & Service
        BorrowTicketRepository borrowTicketRepository = new JsonBorrowTicketRepository();
        BorrowService borrowService = new BorrowService(borrowTicketRepository);

        System.out.println("==================================================");
        System.out.println(">>> ĐANG CHẠY THỬ MODULE BORROW (TEST SERVICE) <<<");
        System.out.println("==================================================");

        try {
            // 2. Test tạo một request mượn sách
            BorrowRequest request = new BorrowRequest();
            request.setReaderId("R001");
            request.setBorrowDate(LocalDate.now());
            request.setDueDate(LocalDate.now().plusDays(14));

            // Thêm danh sách sách mượn
            BorrowItemRequest item = new BorrowItemRequest("B001", 2);
            request.setItems(List.of(item));

            // Gọi service tạo phiếu
            BorrowTicketResponse response = borrowService.createBorrowTicket(request);

            System.out.println("-> Tạo phiếu mượn thành công!");
            System.out.println("Mã phiếu: " + response.getTicketId());
            System.out.println("Mã độc giả: " + response.getReaderId());
            System.out.println("Trạng thái: " + response.getStatus());

        } catch (Exception e) {
            System.err.println("Lỗi khi test: " + e.getMessage());
        }
    }
}