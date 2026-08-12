package repository.json;

import model.Book;
import repository.BookRepository;
import utils.JsonFileUtils;

import java.util.List;
import java.util.Optional;

public class JsonBookRepository implements BookRepository {
    private final String filePath;

    public JsonBookRepository(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<Book> findAll() {
        return JsonFileUtils.readListFromFile(filePath, Book.class);
    }

    @Override
    public Optional<Book> findByCode(String code) {
        return findAll().stream()
                .filter(book -> book.getCode().equalsIgnoreCase(code))
                .findFirst();
    }

    @Override
    public void save(Book book) {
        List<Book> books = findAll();
        books.add(book);
        JsonFileUtils.writeListToFile(filePath, books);
    }

    @Override
    public void update(Book updatedBook) {
        List<Book> books = findAll();
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getCode().equalsIgnoreCase(updatedBook.getCode())) {
                books.set(i, updatedBook);
                break;
            }
        }
        JsonFileUtils.writeListToFile(filePath, books);
    }

    @Override
    public void deleteByCode(String code) {
        List<Book> books = findAll();
        books.removeIf(book -> book.getCode().equalsIgnoreCase(code));
        JsonFileUtils.writeListToFile(filePath, books);
    }
}