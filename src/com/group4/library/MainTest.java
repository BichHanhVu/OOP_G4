package com.group4.library;

import com.group4.library.handler.BorrowHandler;
import com.group4.library.repository.BorrowTicketRepository;
import com.group4.library.repository.json.JsonBorrowTicketRepository;
import com.group4.library.service.BorrowService;

public class MainTest {
    public static void main(String[] args) {
        // 1. Khởi tạo Repository
        BorrowTicketRepository borrowTicketRepository = new JsonBorrowTicketRepository();

        // 2. Khởi tạo Service
        BorrowService borrowService = new BorrowService(borrowTicketRepository);

        // 3. Khởi tạo Handler
        BorrowHandler borrowHandler = new BorrowHandler(borrowService);

        // 4. Chạy Menu Mượn Trả Sách
        System.out.println("==================================================");
        System.out.println(">>> ĐANG CHẠY THỬ MODULE BORROW (DẠNG CONSOLE) <<<");
        System.out.println("==================================================");

        borrowHandler.displayBorrowMenu();
    }
}