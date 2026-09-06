package com.group4.library.service;

import com.group4.library.dto.BookBorrowHistoryItem;
import com.group4.library.dto.BookDetailResponse;
import com.group4.library.dto.BookRequest;
import com.group4.library.dto.BookResponse;
import com.group4.library.dto.BookStatisticsResponse;
import com.group4.library.dto.TopBorrowedBookItem;
import com.group4.library.exception.BookNotFoundException;
import com.group4.library.model.Book;
import com.group4.library.model.BorrowTicket;
import com.group4.library.model.BorrowTicketDetail;
import com.group4.library.model.Reader;
import com.group4.library.repository.BookRepository;
import org.springframework.stereotype.Service;
import com.group4.library.model.TicketStatus;
import com.group4.library.repository.BorrowTicketRepository;
import com.group4.library.repository.ReaderRepository;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookService {
    // Ngưỡng tồn kho thấp: sách còn <= ngưỡng này sẽ xuất hiện trong danh sách cảnh báo tồn kho thấp
    private static final int LOW_STOCK_THRESHOLD = 2;
    // Số lượng sách hiển thị trong bảng xếp hạng "mượn nhiều nhất"
    private static final int TOP_BORROWED_LIMIT = 5;

    private final BookRepository bookRepository;
    private final BorrowTicketRepository borrowTicketRepository;
    private final ReaderRepository readerRepository;

    public BookService(
            BookRepository bookRepository,
            BorrowTicketRepository borrowTicketRepository,
            ReaderRepository readerRepository
    ) {
        this.bookRepository = bookRepository;
        this.borrowTicketRepository = borrowTicketRepository;
        this.readerRepository = readerRepository;
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
        return borrowTicketRepository.findByStatus(TicketStatus.BORROWING)
                .stream()
                .filter(ticket -> ticket.getItems() != null)
                .flatMap(ticket -> ticket.getItems().stream())
                .anyMatch(item -> bookCode.equalsIgnoreCase(item.getBookId()));
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

    // ===================== Chi tiết & lịch sử mượn của một cuốn sách =====================

    public BookDetailResponse getDetail(String bookId) {
        if (bookId == null || bookId.trim().isEmpty()) {
            throw new IllegalArgumentException("Lỗi: Mã sách tìm kiếm không được để trống!");
        }
        String cleanId = bookId.trim();
        Book book = bookRepository.findById(cleanId)
                .orElseThrow(() -> new BookNotFoundException("Không tìm thấy sách với mã '" + cleanId + "'!"));

        List<BorrowTicket> ticketsWithThisBook = borrowTicketRepository.findAll().stream()
                .filter(t -> t.getItems() != null && t.getItems().stream()
                        .anyMatch(d -> d != null && cleanId.equalsIgnoreCase(d.getBookId())))
                .collect(Collectors.toList());

        long timesBorrowed = ticketsWithThisBook.size();

        long totalQuantityBorrowed = ticketsWithThisBook.stream()
                .flatMap(t -> t.getItems().stream())
                .filter(d -> cleanId.equalsIgnoreCase(d.getBookId()))
                .mapToLong(BorrowTicketDetail::getQuantity)
                .sum();

        int currentBorrowingQuantity = (int) ticketsWithThisBook.stream()
                .filter(t -> t.getStatus() == TicketStatus.BORROWING)
                .flatMap(t -> t.getItems().stream())
                .filter(d -> cleanId.equalsIgnoreCase(d.getBookId()))
                .mapToLong(BorrowTicketDetail::getQuantity)
                .sum();

        List<BookBorrowHistoryItem> history = ticketsWithThisBook.stream()
                .sorted(Comparator.comparing(BorrowTicket::getBorrowDate).reversed())
                .map(t -> toHistoryItem(t, cleanId))
                .collect(Collectors.toList());

        return new BookDetailResponse(
                book.getBookId(), book.getTitle(), book.getAuthor(), book.getGenre(),
                book.getAvailableQuantity(), book.getPrice(),
                timesBorrowed, totalQuantityBorrowed, currentBorrowingQuantity, history);
    }

    private BookBorrowHistoryItem toHistoryItem(BorrowTicket ticket, String bookId) {
        int quantity = ticket.getItems().stream()
                .filter(d -> d != null && bookId.equalsIgnoreCase(d.getBookId()))
                .mapToInt(BorrowTicketDetail::getQuantity)
                .sum();

        String readerName = readerRepository.findById(ticket.getReaderId())
                .map(Reader::getName)
                .orElse("(Bạn đọc không còn tồn tại)");

        return new BookBorrowHistoryItem(
                ticket.getTicketId(), ticket.getReaderId(), readerName,
                ticket.getBorrowDate(), ticket.getDueDate(), ticket.getReturnDate(),
                ticket.getStatus(), quantity);
    }

    // ===================== Thống kê tổng quan kho sách =====================

    public BookStatisticsResponse getStatistics() {
        List<Book> allBooks = bookRepository.findAll();
        List<BorrowTicket> allTickets = borrowTicketRepository.findAll();

        long totalTitles = allBooks.size();

        long totalAvailableCopies = allBooks.stream()
                .mapToLong(b -> b.getAvailableQuantity() != null ? b.getAvailableQuantity() : 0L)
                .sum();

        long totalBorrowedCopies = allTickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.BORROWING)
                .filter(t -> t.getItems() != null)
                .flatMap(t -> t.getItems().stream())
                .mapToLong(BorrowTicketDetail::getQuantity)
                .sum();

        long totalCopies = totalAvailableCopies + totalBorrowedCopies;

        Map<String, Long> countByGenre = allBooks.stream()
                .collect(Collectors.groupingBy(
                        b -> (b.getGenre() == null || b.getGenre().isBlank()) ? "Chưa phân loại" : b.getGenre(),
                        Collectors.counting()));

        List<BookResponse> lowStockBooks = allBooks.stream()
                .filter(b -> b.getAvailableQuantity() != null && b.getAvailableQuantity() <= LOW_STOCK_THRESHOLD)
                .sorted(Comparator.comparing(Book::getAvailableQuantity))
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        List<TopBorrowedBookItem> topBorrowedBooks = buildTopBorrowedBooks(allTickets, allBooks);

        return new BookStatisticsResponse(totalTitles, totalCopies, totalAvailableCopies,
                totalBorrowedCopies, countByGenre, lowStockBooks, topBorrowedBooks);
    }

    private List<TopBorrowedBookItem> buildTopBorrowedBooks(List<BorrowTicket> tickets, List<Book> books) {
        Map<String, Long> quantityByBook = new HashMap<>();
        Map<String, Long> timesByBook = new HashMap<>();

        for (BorrowTicket ticket : tickets) {
            if (ticket.getItems() == null) continue;
            for (BorrowTicketDetail detail : ticket.getItems()) {
                if (detail == null || detail.getBookId() == null) continue;
                quantityByBook.merge(detail.getBookId(), (long) detail.getQuantity(), Long::sum);
                timesByBook.merge(detail.getBookId(), 1L, Long::sum);
            }
        }

        Map<String, String> titleById = books.stream()
                .collect(Collectors.toMap(Book::getBookId, Book::getTitle, (a, b) -> a));

        return quantityByBook.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(TOP_BORROWED_LIMIT)
                .map(e -> new TopBorrowedBookItem(
                        e.getKey(),
                        titleById.getOrDefault(e.getKey(), "(Sách không còn tồn tại)"),
                        timesByBook.getOrDefault(e.getKey(), 0L),
                        e.getValue()))
                .collect(Collectors.toList());
    }
}