package repository.json;

import model.Book;
import repository.BookRepository;
import utils.JsonFileUtils;

import java.util.List;

public class JsonBookRepository implements BookRepository {
    private final String filePath;

    public JsonBookRepository(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<Book> findAll() {
        return JsonFileUtils.readListFromFile(filePath, Book.class);
    }
}