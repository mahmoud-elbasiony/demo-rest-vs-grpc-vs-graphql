package com.asset.demo.controllers;

import com.asset.demo.entities.Book;
import com.asset.demo.repositories.BookRepository;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;
import java.util.List;

@Controller
public class BookGraphQLController {

    private final BookRepository repository;

    public BookGraphQLController(BookRepository repository) {
        this.repository = repository;
    }

    // Query: { allBooks { title author } }
    // Client can specify EXACTLY which fields they want
    @QueryMapping
    public List<Book> allBooks() {
        return repository.findAll();
    }

    // Query: { book(id: "1") { title price } }
    @QueryMapping
    public Book book(@Argument Long id) {
        return repository.findById(id).orElse(null);
    }

    // Query: { booksByAuthor(author: "Shakespeare") { title } }
    @QueryMapping
    public List<Book> booksByAuthor(@Argument String author) {
        return repository.findByAuthor(author);
    }

    // Mutation: { createBook(title: "1984", author: "Orwell", isbn: "123", price: 15.99) { id title } }
    @MutationMapping
    public Book createBook(@Argument String title, @Argument String author,
                           @Argument String isbn, @Argument Double price) {
        Book book = Book.builder()
                .title(title)
                .author(author)
                .isbn(isbn)
                .price(price)
                .build();
        return repository.save(book);
    }

    // Mutation: { updateBook(id: "1", price: 19.99) { id price } }
    @MutationMapping
    public Book updateBook(@Argument Long id, @Argument String title,
                           @Argument String author, @Argument String isbn, @Argument Double price) {
        return repository.findById(id)
                .map(book -> {
                    if (title != null) book.setTitle(title);
                    if (author != null) book.setAuthor(author);
                    if (isbn != null) book.setIsbn(isbn);
                    if (price != null) book.setPrice(price);
                    return repository.save(book);
                })
                .orElse(null);
    }

    // Mutation: { deleteBook(id: "1") }
    @MutationMapping
    public Boolean deleteBook(@Argument Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
