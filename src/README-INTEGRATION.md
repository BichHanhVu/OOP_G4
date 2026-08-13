# Module Return + Dashboard (Vũ Bích Hạnh)

Copy các thư mục `src` và `data` vào project Spring Boot trên nhánh `main`.

## Hợp đồng tích hợp

- Package chung: `com.group4.library`.
- `BookRepository` cần có `findByCode(String)` và `update(Book)`.
- `Book` cần có `returnItem()`; service gọi lặp theo `quantity`.
- `BorrowTicketRepository` cần có `findAll()`, `findById(String)` và `save(BorrowTicket)`.
- `ReaderRepository` cần có `findById(String)`.
- Phiếu mượn chỉ có `BORROWING` và `RETURNED`.
- Tiền dùng `long`; ngày dùng `LocalDate`.

## API

- `POST /api/returns`
- `GET /api/returns`
- `GET /api/dashboard`

Ví dụ trả sách:

```json
{
  "ticketId": "BT001",
  "actualReturnDate": "2026-08-15"
}
```

Chạy kiểm thử bằng `mvn test` sau khi các module Reader, Book và Borrow đã được merge.
