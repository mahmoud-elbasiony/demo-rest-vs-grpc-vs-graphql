package com.asset.demo.controllers;

import com.asset.demo.entities.Book;
import com.asset.demo.repositories.BookRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/api/rest/books")
public class BookRestController {

    private final BookRepository repository;

    public BookRestController(BookRepository repository) {
        this.repository = repository;
    }

    // GET /api/rest/books - Returns ALL books with ALL fields
    @GetMapping
    public List<Book> getAllBooks() {
        return repository.findAll();
    }

    // GET /api/rest/books/1 - Returns ONE book with ALL fields
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/rest/books - Create new book
    @PostMapping
    public Book createBook(@RequestBody Book book) {
        return repository.save(book);
    }

    // PUT /api/rest/books/1 - Update existing book
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book book) {
        return repository.findById(id)
                .map(existing -> {
                    book.setId(id);
                    return ResponseEntity.ok(repository.save(book));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/rest/books/1 - Delete book
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // GET /api/rest/books/search?author=Shakespeare
    @GetMapping("/search")
    public List<Book> searchByAuthor(@RequestParam String author) {
        return repository.findByAuthor(author);
    }
}
