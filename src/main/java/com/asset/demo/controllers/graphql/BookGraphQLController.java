package com.asset.demo.controllers.graphql;

import com.asset.demo.entities.Author;
import com.asset.demo.entities.Book;
import com.asset.demo.repositories.AuthorRepository;
import com.asset.demo.repositories.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class BookGraphQLController {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    @QueryMapping
    public List<Book> allBooks() {
        return bookRepository.findAll();
    }

    @QueryMapping
    public Book book(@Argument Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public List<Book> booksByAuthor(@Argument Long authorId) {
        return bookRepository.findByAuthorId(authorId);
    }

    @QueryMapping
    public List<Book> booksByAuthorName(@Argument String authorName) {
        return bookRepository.findByAuthorName(authorName);
    }

    @MutationMapping
    public Book createBook(@Argument String title, @Argument String isbn,
                           @Argument Double price, @Argument Long authorId) {
        return authorRepository.findById(authorId)
                .map(author -> {
                    Book book = Book.builder()
                            .title(title)
                            .isbn(isbn)
                            .price(price)
                            .author(author)
                            .build();
                    return bookRepository.save(book);
                })
                .orElse(null);
    }

    @MutationMapping
    public Book updateBook(@Argument Long id, @Argument String title,
                           @Argument String isbn, @Argument Double price, @Argument Long authorId) {
        return bookRepository.findById(id)
                .map(book -> {
                    if (title != null) book.setTitle(title);
                    if (isbn != null) book.setIsbn(isbn);
                    if (price != null) book.setPrice(price);
                    if (authorId != null) {
                        authorRepository.findById(authorId).ifPresent(book::setAuthor);
                    }
                    return bookRepository.save(book);
                })
                .orElse(null);
    }

    @MutationMapping
    public Boolean deleteBook(@Argument Long id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @SchemaMapping(typeName = "Book", field = "author")
    public Author author(Book book) {
        return book.getAuthor();
    }
}
