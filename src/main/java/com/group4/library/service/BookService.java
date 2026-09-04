package com.group4.library.service;

import com.group4.library.dto.BookRequest;
import com.group4.library.dto.BookResponse;
import com.group4.library.exception.BookNotFoundException;
import com.group4.library.model.Book;
import com.group4.library.repository.BookRepository;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public BookResponse getBookById(String bookId) {
        if (bookId == null || bookId.trim().isEmpty()) {
            throw new IllegalArgumentException("Lỗi: Mã sách tìm kiếm không được để trống!");
        }
        return bookRepository.findById(bookId.trim())
                .map(this::convertToResponse)
                .orElseThrow(() -> new BookNotFoundException("Không tìm thấy sách với mã '" + bookId + "'!"));
    }

    public List<BookResponse> searchBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBooks();
        }
        return bookRepository.searchByIdOrTitle(keyword.trim()).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public BookResponse addBook(BookRequest request) {
        validateBookData(request);

        String cleanBookId = request.getBookId().trim();
        if (bookRepository.findById(cleanBookId).isPresent()) {
            throw new IllegalArgumentException("Lỗi: Mã sách '" + cleanBookId + "' đã tồn tại trong hệ thống!");
        }

        Book book = new Book(
                cleanBookId,
                request.getTitle().trim(),
                request.getAuthor() != null ? request.getAuthor().trim() : "",
                request.getGenre() != null ? request.getGenre().trim() : "",
                request.getAvailableQuantity(),
                request.getPrice()
        );

        bookRepository.save(book);
        return convertToResponse(book);
    }

    public BookResponse updateBook(String code, BookRequest request) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Lỗi: Mã sách cần cập nhật không được để trống!");
        }

        validateBookData(request);

        Book existingBook = bookRepository.findById(code.trim())
                .orElseThrow(() -> new BookNotFoundException("Không tìm thấy sách với mã '" + code + "' để cập nhật!"));

        existingBook.setTitle(request.getTitle().trim());
        existingBook.setAuthor(request.getAuthor() != null ? request.getAuthor().trim() : "");
        existingBook.setGenre(request.getGenre() != null ? request.getGenre().trim() : "");
        existingBook.setAvailableQuantity(request.getAvailableQuantity());
        existingBook.setPrice(request.getPrice());

        boolean updated = bookRepository.update(existingBook);
        if (!updated) {
            throw new IllegalStateException("Cập nhật thất bại. Không thể ghi dữ liệu sách!");
        }

        return convertToResponse(existingBook);
    }

    public BookResponse adjustQuantity(String code, int deltaQuantity) {
        Book existingBook = bookRepository.findById(code)
                .orElseThrow(() -> new BookNotFoundException("Không tìm thấy sách với mã '" + code + "'!"));

        int newQuantity = existingBook.getAvailableQuantity() + deltaQuantity;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Lỗi: Số lượng tồn kho không thể âm!");
        }

        existingBook.setAvailableQuantity(newQuantity);
        bookRepository.update(existingBook);
        return convertToResponse(existingBook);
    }

    public void deleteBook(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Lỗi: Mã sách cần xóa không được để trống!");
        }

        String cleanCode = code.trim();
        Book book = bookRepository.findById(cleanCode)
                .orElseThrow(() -> new BookNotFoundException("Không tìm thấy sách với mã '" + cleanCode + "' để xóa!"));

        if (isBookCurrentlyBorrowed(cleanCode)) {
            throw new IllegalStateException("Không thể xóa sách đang nằm trong phiếu mượn chưa trả!");
        }

        boolean deleted = bookRepository.deleteByCode(cleanCode);
        if (!deleted) {
            throw new IllegalStateException("Thao tác xóa thất bại. Mã sách không tồn tại hoặc đã bị xóa!");
        }
    }

    private void validateBookData(BookRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Lỗi: Yêu cầu dữ liệu sách không được để trống!");
        }
        if (request.getBookId() == null || request.getBookId().trim().isEmpty()) {
            throw new IllegalArgumentException("Lỗi: Mã sách không được để trống!");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Lỗi: Tên sách không được để trống!");
        }
        if (request.getAvailableQuantity() == null || request.getAvailableQuantity() < 0) {
            throw new IllegalArgumentException("Lỗi: Số lượng sách không được âm hoặc để trống!");
        }
        if (request.getPrice() == null || request.getPrice() < 0) {
            throw new IllegalArgumentException("Lỗi: Giá trị sách không được âm hoặc để trống!");
        }
    }

    private BookResponse convertToResponse(Book book) {
        return new BookResponse(
                book.getBookId(),
                book.getTitle(),
                book.getAuthor(),
                book.getGenre(),
                book.getAvailableQuantity(),
                book.getPrice()
        );
    }

    private boolean isBookCurrentlyBorrowed(String bookCode) {
        throw new UnsupportedOperationException("Lỗi: Tính năng kiểm tra chưa được code. Để đảm bảo, tạm thời chưa hỗ trợ xóa!");
    }

    public String exportBooks() {
        List<BookResponse> books = getAllBooks();
        StringBuilder csv = new StringBuilder();
        csv.append("\uFEFF");
        csv.append("Mã sách,Tên sách,Tác giả,Thể loại,Số lượng,Giá\n");

        for (BookResponse b : books) {
            csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%d,%d\n",
                    b.getBookId(), b.getTitle(), b.getAuthor(), b.getGenre(),
                    b.getAvailableQuantity(), b.getPrice()));
        }
        return csv.toString();
    }

    public int importBooks(InputStream inputStream) throws Exception {
        List<BookRequest> pendingBooks = new ArrayList<>();
        Set<String> csvBookIds = new HashSet<>();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (isHeader) {
                    if (line.startsWith("\uFEFF")) line = line.substring(1);
                    isHeader = false;
                    continue;
                }

                if (line.trim().isEmpty()) continue;

                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (values.length < 6) {
                    throw new IllegalArgumentException("Dòng " + lineNumber + ": Không đủ cấu trúc dữ liệu (Cần 6 cột)!");
                }

                String code = values[0].replace("\"", "").trim();
                String title = values[1].replace("\"", "").trim();
                String author = values[2].replace("\"", "").trim();
                String genre = values[3].replace("\"", "").trim();

                int quantity;
                long price;
                try {
                    quantity = Integer.parseInt(values[4].trim());
                    price = Long.parseLong(values[5].trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Dòng " + lineNumber + ": Số lượng hoặc Giá sách không đúng định dạng số!");
                }

                BookRequest request = new BookRequest();
                request.setBookId(code);
                request.setTitle(title);
                request.setAuthor(author);
                request.setGenre(genre);
                request.setAvailableQuantity(quantity);
                request.setPrice(price);

                validateBookData(request);

                if (!csvBookIds.add(code)) {
                    throw new IllegalArgumentException("Dòng " + lineNumber + ": Mã sách '" + code + "' bị trùng lặp trong file import!");
                }

                if (bookRepository.findById(code).isPresent()) {
                    throw new IllegalArgumentException("Dòng " + lineNumber + ": Mã sách '" + code + "' đã tồn tại trong cơ sở dữ liệu!");
                }

                pendingBooks.add(request);
            }
        }

        for (BookRequest req : pendingBooks) {
            Book book = new Book(
                    req.getBookId().trim(),
                    req.getTitle().trim(),
                    req.getAuthor() != null ? req.getAuthor().trim() : "",
                    req.getGenre() != null ? req.getGenre().trim() : "",
                    req.getAvailableQuantity(),
                    req.getPrice()
            );
            bookRepository.save(book);
        }

        return pendingBooks.size();
    }
}