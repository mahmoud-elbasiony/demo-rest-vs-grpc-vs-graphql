package com.asset.demo.controllers.rest;

import com.asset.demo.dtos.CreateBookDto;
import com.asset.demo.dtos.UpdateBookDto;
import com.asset.demo.entities.Author;
import com.asset.demo.entities.Book;
import com.asset.demo.grpc.CreateBookRequest;
import com.asset.demo.repositories.AuthorRepository;
import com.asset.demo.repositories.BookRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/rest/books")
public class BookRestController {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    @Operation(summary = "Get all books", description = "Retrieve a list of all books")
    @GetMapping
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Operation(summary = "Get book by ID", description = "Retrieve a single book by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return bookRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/rest/books - Create book with authorId
    @Operation(summary = "Create new book", description = "Add a new book with author reference")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Book created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid author ID")
    })
    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody CreateBookDto request) {
        return authorRepository.findById(request.getAuthorId())
                .map(author -> {
                    Book book = Book.builder()
                            .title(request.getTitle())
                            .isbn(request.getIsbn())
                            .price(request.getPrice())
                            .author(author)
                            .build();
                    return ResponseEntity.ok(bookRepository.save(book));
                })
                .orElse(ResponseEntity.badRequest().build());
    }

    @Operation(summary = "Update book", description = "Update an existing book")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody UpdateBookDto request) {
        return bookRepository.findById(id)
                .map(book -> {
                    if (request.getTitle() != null) book.setTitle(request.getTitle());
                    if (request.getIsbn() != null) book.setIsbn(request.getIsbn());
                    if (request.getPrice() != null) book.setPrice(request.getPrice());

                    if (request.getAuthorId() != null) {
                        Author author = authorRepository.findById(request.getAuthorId())
                                .orElseThrow(() -> new RuntimeException("Author not found"));
                        book.setAuthor(author);
                    }

                    return ResponseEntity.ok(bookRepository.save(book));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete book", description = "Delete a book by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Search books by author name",
            description = "Find all books by a specific author name"
    )
    @GetMapping("/search")
    public List<Book> searchByAuthorName(@RequestParam String authorName) {
        return bookRepository.findByAuthorName(authorName);
    }

    @GetMapping("/search/ignore-case")
    public List<Book> searchByAuthorNameIgnoreCase(@RequestParam String authorName) {
        return bookRepository.findByAuthorNameIgnoreCase(authorName);
    }
}
