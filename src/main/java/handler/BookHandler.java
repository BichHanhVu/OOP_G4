package handler;

import dto.BookRequest;
import dto.BookResponse;
import service.BookService;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BookHandler implements HttpHandler {
    private final BookService bookService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BookHandler(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        try {
            switch (method.toUpperCase()) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "PUT":
                    handlePut(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    sendResponse(exchange, 405, "Phương thức HTTP không được hỗ trợ!");
                    break;
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendResponse(exchange, 400, e.getMessage());
        } catch (Exception e) {
            sendResponse(exchange, 500, "Lỗi Server: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        List<BookResponse> books = bookService.getAllBooks();
        String jsonResponse = objectMapper.writeValueAsString(books);
        sendJsonResponse(exchange, 200, jsonResponse);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        BookRequest request = objectMapper.readValue(is, BookRequest.class);

        BookResponse newBook = bookService.addBook(request);
        String jsonResponse = objectMapper.writeValueAsString(newBook);

        sendJsonResponse(exchange, 201, jsonResponse);
    }

    private void handlePut(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        BookRequest request = objectMapper.readValue(is, BookRequest.class);

        BookResponse updatedBook = bookService.updateBook(request.getCode(), request);
        String jsonResponse = objectMapper.writeValueAsString(updatedBook);

        sendJsonResponse(exchange, 200, jsonResponse);
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        URI requestURI = exchange.getRequestURI();
        String query = requestURI.getQuery();

        String code = null;
        if (query != null && query.startsWith("code=")) {
            code = query.split("=")[1];
        }

        if (code == null || code.trim().isEmpty()) {
            sendResponse(exchange, 400, "Lỗi: Thiếu tham số mã sách 'code'!");
            return;
        }

        bookService.deleteBook(code);
        sendResponse(exchange, 200, "Xóa sách thành công!");
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}