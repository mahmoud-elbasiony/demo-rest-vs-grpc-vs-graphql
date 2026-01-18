package com.asset.demo.controllers.graphql;

import com.asset.demo.dtos.AuthorDto;
import com.asset.demo.dtos.BookDto;
import com.asset.demo.dtos.BookPage;
import com.asset.demo.dtos.CreateBookDto;
import com.asset.demo.dtos.PageInfo;
import com.asset.demo.dtos.UpdateBookDto;
import com.asset.demo.entities.Author;
import com.asset.demo.entities.Book;
import com.asset.demo.repositories.AuthorRepository;
import com.asset.demo.repositories.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    public Book createBook(@Argument("input") CreateBookDto createBookDto) {
        return authorRepository.findById(createBookDto.getAuthorId())
                .map(author -> {
                    Book book = Book.builder()
                            .title(createBookDto.getTitle())
                            .isbn(createBookDto.getIsbn())
                            .price(createBookDto.getPrice())
                            .author(author)
                            .build();
                    return bookRepository.save(book);
                })
                .orElse(null);
    }

    @MutationMapping
    public Book updateBook(@Argument Long id, @Argument("input") UpdateBookDto updateBookDto) {
        return bookRepository.findById(id)
                .map(book -> {
                    if (updateBookDto.getTitle() != null) book.setTitle(updateBookDto.getTitle());
                    if (updateBookDto.getIsbn() != null) book.setIsbn(updateBookDto.getIsbn());
                    if (updateBookDto.getPrice() != null) book.setPrice(updateBookDto.getPrice());
                    if (updateBookDto.getAuthorId() != null) {
                        authorRepository.findById(updateBookDto.getAuthorId()).ifPresent(book::setAuthor);
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

    @QueryMapping
    public BookPage booksPaginated(@Argument int page, @Argument int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.findAll(pageable);

        List<BookDto> books = bookPage.getContent().stream()
                .map(this::toBookDto)
                .collect(Collectors.toList());

        PageInfo pageInfo = PageInfo.builder()
                .page(bookPage.getNumber())
                .size(bookPage.getSize())
                .totalElements(bookPage.getTotalElements())
                .totalPages(bookPage.getTotalPages())
                .hasNext(bookPage.hasNext())
                .hasPrevious(bookPage.hasPrevious())
                .build();

        return BookPage.builder()
                .content(books)
                .pageInfo(pageInfo)
                .build();
    }

    @SchemaMapping(typeName = "Book", field = "author")
    public Author author(Book book) {
        return book.getAuthor();
    }

    private BookDto toBookDto(Book book) {
        return BookDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .price(book.getPrice())
                .author(book.getAuthor() != null ? toAuthorDtoSimple(book.getAuthor()) : null)
                .build();
    }

    private AuthorDto toAuthorDtoSimple(Author author) {
        return AuthorDto.builder()
                .id(author.getId())
                .name(author.getName())
                .email(author.getEmail())
                .bio(author.getBio())
                .build();
    }

}
