import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import handler.BookHandler;
import repository.BookRepository;
import repository.json.JsonBookRepository;
import service.BookService;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        BookRepository bookRepository = new JsonBookRepository("data/books.json");
        BookService bookService = new BookService(bookRepository);
        
        server.createContext("/api/books", new BookHandler(bookService));

        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        server.start();
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            if (path.equals("/")) {
                path = "/pages/books.html";
            }

            File file = new File("frontend" + path);
            if (file.exists() && !file.isDirectory()) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                
                if (path.endsWith(".html")) {
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                } else if (path.endsWith(".js")) {
                    exchange.getResponseHeaders().set("Content-Type", "application/javascript; charset=UTF-8");
                } else if (path.endsWith(".css")) {
                    exchange.getResponseHeaders().set("Content-Type", "text/css; charset=UTF-8");
                }

                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } else {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
}