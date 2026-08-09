package handler;

import dto.BookResponse;
import service.BookService;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
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
        
        if ("GET".equalsIgnoreCase(method)) {
            List<BookResponse> books = bookService.getAllBooks();
            String jsonResponse = objectMapper.writeValueAsString(books);

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            
            exchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }
}