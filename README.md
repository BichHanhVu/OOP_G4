# Hệ thống quản lý thư viện – Nhóm 04

## Giới thiệu

Dự án quản lý thư viện được xây dựng bằng Java và Spring Boot, áp dụng các nguyên lý lập trình hướng đối tượng. Hệ thống hỗ trợ quản lý bạn đọc, quản lý sách, mượn – trả sách, tính tiền phạt và thống kê hoạt động thư viện.

## Chức năng chính

- Quản lý thông tin bạn đọc.
- Quản lý và tìm kiếm sách.
- Lập và theo dõi phiếu mượn.
- Kiểm tra giới hạn mượn của từng loại bạn đọc.
- Kiểm tra số lượng sách hiện có.
- Trả sách và cập nhật số lượng.
- Tính tiền phạt khi trả sách quá hạn.
- Xem lịch sử trả sách.
- Hiển thị số liệu tổng hợp trên Dashboard.
- Không cho xóa sách hoặc bạn đọc đang có phiếu mượn chưa trả.

## Công nghệ sử dụng

- Java 17
- Spring Boot 3.2.5
- Maven
- Jackson JSON
- HTML, CSS và JavaScript
- JUnit 5 và Mockito
- IntelliJ IDEA
- Git và GitHub

## Chạy ứng dụng

### Chạy bằng IntelliJ IDEA

1. Mở thư mục chứa file `pom.xml` bằng IntelliJ IDEA.
2. Cấu hình Project SDK là JDK 17.
3. Chờ IntelliJ tải các thư viện trong `pom.xml`.
4. Mở file:

```text
src/main/java/com/group4/library/main/LibraryApplication.java
```

5. Chạy method `main`.
6. Mở trình duyệt và truy cập:

```text
http://localhost:8080
```

### Chạy bằng Maven

```bash
mvn spring-boot:run
```

Ứng dụng mặc định chạy tại http://localhost:8080.

## Chạy kiểm thử

Có thể chạy toàn bộ test bằng IntelliJ IDEA hoặc sử dụng Maven:

```bash
mvn test
```

Kết quả kiểm thử gần nhất:

```text
Tests run: 271, Passed: 271, Failures: 0, Errors: 0, Skipped: 0
```

## Dữ liệu

Dữ liệu của hệ thống được lưu trong các file JSON:

- `data/readers.json`: thông tin bạn đọc.
- `data/books.json`: thông tin sách.
- `data/borrow-tickets.json`: phiếu mượn sách.
- `data/return-records.json`: lịch sử trả sách và tiền phạt.

Các file dữ liệu sẽ được repository đọc và cập nhật trong quá trình sử dụng ứng dụng.

## Loại bạn đọc và giới hạn mượn

| Loại | Giá trị `type` | Giới hạn mượn |
|---|---|---:|
| Sinh viên thường | `STUDENT` | 3 |
| Sinh viên ưu tiên | `PRIORITY_STUDENT` | 5 |
| Giảng viên | `LECTURER` | 7 |

## Quy tắc nghiệp vụ

- Mỗi loại bạn đọc có giới hạn số lượng sách được mượn khác nhau.
- Mã bạn đọc và mã sách không được để trống.
- Hạn trả không được trước ngày mượn.
- Số lượng sách mượn phải lớn hơn 0.
- Không cho mượn quá số lượng sách hiện có.
- Không cho mượn vượt quá giới hạn của bạn đọc.
- Khi mượn sách, số lượng sách hiện có được giảm.
- Khi trả sách, số lượng sách hiện có được tăng trở lại.
- Phiếu đã trả không được trả lần thứ hai.
- Tiền phạt được tính theo loại bạn đọc và số ngày trả quá hạn.
- Không được xóa bạn đọc đang có phiếu `BORROWING`.
- Không được xóa sách đang nằm trong phiếu `BORROWING`.

## API bạn đọc

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/readers?keyword=&type=` | Lấy danh sách bạn đọc, lọc theo tên, mã hoặc loại |
| GET | `/api/readers/{id}` | Xem chi tiết một bạn đọc |
| POST | `/api/readers` | Thêm bạn đọc mới |
| PUT | `/api/readers/{id}` | Cập nhật thông tin bạn đọc |
| DELETE | `/api/readers/{id}` | Xóa bạn đọc nếu không có phiếu mượn chưa trả |

### Ví dụ thêm bạn đọc

**POST `/api/readers`**

Request:

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

### Các trường hợp lỗi của bạn đọc

| Tình huống | HTTP status | Ví dụ response |
|---|---:|---|
| Không tìm thấy bạn đọc | 404 | `{ "message": "Không tìm thấy bạn đọc: R999" }` |
| Mã bạn đọc bị trùng | 400 | `{ "message": "Mã bạn đọc đã tồn tại: R001" }` |
| Họ tên để trống | 400 | `{ "message": "Họ tên không được để trống" }` |
| Số điện thoại sai định dạng | 400 | `{ "message": "Số điện thoại không hợp lệ" }` |
| Xóa bạn đọc đang mượn sách | 400 | `{ "message": "Không thể xóa bạn đọc đang có phiếu mượn chưa trả" }` |
| Lỗi hệ thống ngoài dự kiến | 500 | `{ "message": "Đã có lỗi không mong muốn xảy ra" }` |

## API sách

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/books` | Lấy danh sách hoặc tìm kiếm sách |
| GET | `/api/books/{id}` | Xem chi tiết một cuốn sách |
| POST | `/api/books` | Thêm sách mới |
| PUT | `/api/books/{id}` | Cập nhật thông tin sách |
| DELETE | `/api/books/{id}` | Xóa sách nếu không có phiếu mượn chưa trả |
| GET | `/api/books/export` | Xuất danh sách sách |
| POST | `/api/books/import` | Nhập danh sách sách |

## API mượn sách

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/borrow-tickets` | Lấy danh sách phiếu mượn |
| GET | `/api/borrow-tickets/{id}` | Xem chi tiết một phiếu mượn |
| POST | `/api/borrow-tickets` | Tạo phiếu mượn mới và giảm tồn kho |
| POST | `/api/borrow-tickets/{id}/cancel` | Hủy phiếu đang mượn và hoàn lại tồn kho |
| PATCH | `/api/borrow-tickets/{id}/renew` | Gia hạn phiếu mượn |

## API trả sách

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/returns` | Lấy lịch sử trả sách |
| POST | `/api/returns` | Thực hiện trả sách và tính tiền phạt |

## API Dashboard

| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/dashboard` | Lấy số liệu tổng hợp của hệ thống |

## Kiểm tra API bằng Postman

1. Khởi động ứng dụng bằng IntelliJ IDEA hoặc lệnh:

```bash
mvn spring-boot:run
```

2. Tạo request `POST http://localhost:8080/api/readers`.
3. Chọn **Body → raw → JSON**.
4. Dán nội dung request mẫu ở trên.
5. Gửi `GET http://localhost:8080/api/readers` để kiểm tra danh sách bạn đọc.
6. Có thể thực hiện tương tự với API sách, mượn sách và trả sách.

## Cấu trúc dự án

```text
data
├── books.json
├── readers.json
├── borrow-tickets.json
└── return-records.json

src/main/java/com/group4/library
├── dto
├── exception
├── handler
├── main
├── model
├── policy
├── repository
│   └── json
├── service
└── utils

src/main/resources
├── application.properties
└── static
    ├── css
    ├── js
    ├── pages
    └── index.html

src/test/java/com/group4/library
├── policy
├── repository
└── service
```

## Phân công thành viên

| Thành viên | Phần phụ trách |
|---|---|
| Trần Văn Tiệp | Quản lý bạn đọc và xây dựng cấu trúc Spring Boot |
| Đinh Xuân Bách | Quản lý sách |
| Nguyễn Thị Thảo Duyên | Quản lý mượn sách |
| Bích Hạnh | Trả sách, tính tiền phạt, Dashboard và tích hợp hệ thống |

## Trạng thái tích hợp

- [x] Tích hợp module quản lý bạn đọc.
- [x] Tích hợp module quản lý sách.
- [x] Tích hợp module mượn sách.
- [x] Tích hợp module trả sách và tính tiền phạt.
- [x] Tích hợp Dashboard.
- [x] Lưu trữ dữ liệu bằng JSON.
- [x] Không cho xóa bạn đọc đang có phiếu mượn chưa trả.
- [x] Không cho xóa sách đang nằm trong phiếu mượn chưa trả.
- [x] Chạy thành công 271 bài kiểm thử.