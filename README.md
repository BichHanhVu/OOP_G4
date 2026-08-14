# LibraryManagement

## Chạy ứng dụng
Mặc định chạy tại http://localhost:8080

## Chạy test

```
mvn test
```

## Dữ liệu
Dữ liệu bạn đọc lưu tại `data/readers.json` (tự tạo nếu chưa tồn tại).

## Loại bạn đọc & giới hạn mượn
| Loại | Giá trị `type` | Giới hạn mượn |
|---|---|---|
| Sinh viên thường | STUDENT | 3 |
| Sinh viên ưu tiên | PRIORITY_STUDENT | 5 |
| Giảng viên | LECTURER | 7 |

## API

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | /api/readers?keyword=&type= | Danh sách bạn đọc, lọc theo tên/mã hoặc loại |
| GET | /api/readers/{id} | Chi tiết 1 bạn đọc |
| POST | /api/readers | Thêm bạn đọc mới |
| PUT | /api/readers/{id} | Cập nhật bạn đọc |
| DELETE | /api/readers/{id} | Xóa bạn đọc |

### Ví dụ request/response

**POST /api/readers**
```json
{
  "name": "Nguyễn Văn A",
  "phoneNumber": "0912345678",
  "type": "STUDENT"
}
```
Response `201 Created`:
```json
{
  "id": "R004",
  "name": "Nguyễn Văn A",
  "phoneNumber": "0912345678",
  "type": "STUDENT",
  "maxBorrowLimit": 3
}
```

### Các trường hợp lỗi

| Tình huống | HTTP status | Ví dụ response |
|---|---|---|
| Không tìm thấy bạn đọc | 404 | `{ "message": "Không tìm thấy bạn đọc: R999" }` |
| Mã bạn đọc trùng | 400 | `{ "message": "Mã bạn đọc đã tồn tại: R001" }` |
| Họ tên rỗng / SĐT sai định dạng | 400 | `{ "message": "Họ tên không được để trống" }` |
| Lỗi hệ thống ngoài dự kiến | 500 | `{ "message": "Đã có lỗi không mong muốn xảy ra" }` |

### Kiểm tra bằng Postman
1. Khởi động app bằng `mvn spring-boot:run`
2. Tạo request `POST http://localhost:8080/api/readers`, Body → raw → JSON, dán ví dụ ở trên
3. Gửi `GET http://localhost:8080/api/readers` để xem danh sách vừa thêm

## Checklist chờ tích hợp
- [ ] Rule "không xóa bạn đọc đang có phiếu BORROWING" — cần `BorrowTicketRepository` từ module Borrow (Duyên), hoàn thiện sau khi module đó merge.
