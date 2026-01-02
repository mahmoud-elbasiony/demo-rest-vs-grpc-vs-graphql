package com.asset.demo.controllers.rest;

import com.asset.demo.entities.Author;
import com.asset.demo.entities.Book;
import com.asset.demo.repositories.AuthorRepository;
import com.asset.demo.repositories.BookRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/rest/authors")
public class AuthorRestController {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;


    // GET /api/rest/authors - Get all authors
    @Operation(
            summary = "Get all authors",
            description = "Retrieve a list of all authors in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    // GET /api/rest/authors/1 - Get author by ID
    @Operation(
            summary = "Get author by ID",
            description = "Get a single author by their ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Author found"),
            @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Author> getAuthorById(@PathVariable Long id) {
        return authorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/rest/authors - Create new author
    @Operation(
            summary = "Create new author",
            description = "Add a new author to the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Author created successfully",
                    content = @Content(schema = @Schema(implementation = Author.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public Author createAuthor(@RequestBody Author author) {
        return authorRepository.save(author);
    }

    // PUT /api/rest/authors/1 - Update existing author
    @Operation(
            summary = "Update author",
            description = "Update an existing author by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Author updated successfully"),
            @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Author> updateAuthor(@PathVariable Long id, @RequestBody Author author) {
        return authorRepository.findById(id)
                .map(existing -> {
                    author.setId(id);
                    return ResponseEntity.ok(authorRepository.save(author));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/rest/authors/1 - Delete author
    @Operation(
            summary = "Delete author",
            description = "Delete an author by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Author deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        if (authorRepository.existsById(id)) {
            authorRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // GET /api/rest/authors/1/books - Get all books by author
    @Operation(
            summary = "Get author's books",
            description = "Retrieve all books written by a specific author"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Books retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @GetMapping("/{id}/books")
    public ResponseEntity<List<Book>> getBooksByAuthor(@PathVariable Long id) {
        if (!authorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bookRepository.findByAuthorId(id));
    }

    // GET /api/rest/authors/search?name=Shakespeare
    @Operation(
            summary = "Search author by name",
            description = "Find an author by their exact name"
    )
    @GetMapping("/search")
    public ResponseEntity<Author> searchByName(@RequestParam String name) {
        return authorRepository.findByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
