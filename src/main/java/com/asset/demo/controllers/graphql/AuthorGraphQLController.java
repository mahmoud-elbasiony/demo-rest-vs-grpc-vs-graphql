package com.asset.demo.controllers.graphql;

import com.asset.demo.dtos.AuthorDto;
import com.asset.demo.dtos.AuthorPage;
import com.asset.demo.dtos.BookDto;
import com.asset.demo.dtos.CreateAuthorDto;
import com.asset.demo.dtos.PageInfo;
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
public class AuthorGraphQLController {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    @QueryMapping
    public List<Author> allAuthors() {
        return authorRepository.findAll();
    }

    @QueryMapping
    public Author author(@Argument Long id) {
        return authorRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public Author authorByName(@Argument String name) {
        return authorRepository.findByName(name).orElse(null);
    }

    @MutationMapping
    public Author createAuthor(@Argument("input") CreateAuthorDto createAuthorDto) {
        Author author = Author.builder()
                .name(createAuthorDto.getName())
                .email(createAuthorDto.getEmail())
                .bio(createAuthorDto.getBio())
                .build();
        return authorRepository.save(author);
    }

    @MutationMapping
    public Author updateAuthor(@Argument Long id, @Argument("input") CreateAuthorDto createAuthorDto) {
        return authorRepository.findById(id)
                .map(author -> {
                    if (createAuthorDto.getName() != null) author.setName(createAuthorDto.getName());
                    if (createAuthorDto.getEmail() != null) author.setEmail(createAuthorDto.getEmail());
                    if (createAuthorDto.getBio() != null) author.setBio(createAuthorDto.getBio());
                    return authorRepository.save(author);
                })
                .orElse(null);
    }

    @MutationMapping
    public Boolean deleteAuthor(@Argument Long id) {
        if (authorRepository.existsById(id)) {
            authorRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @QueryMapping
    public AuthorPage authorsPaginated(@Argument int page, @Argument int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Author> authorPage = authorRepository.findAll(pageable);

        List<AuthorDto> authors = authorPage.getContent().stream()
                .map(this::toAuthorDto)
                .collect(Collectors.toList());

        PageInfo pageInfo = PageInfo.builder()
                .page(authorPage.getNumber())
                .size(authorPage.getSize())
                .totalElements(authorPage.getTotalElements())
                .totalPages(authorPage.getTotalPages())
                .hasNext(authorPage.hasNext())
                .hasPrevious(authorPage.hasPrevious())
                .build();

        return AuthorPage.builder()
                .content(authors)
                .pageInfo(pageInfo)
                .build();
    }

    @SchemaMapping(typeName = "Author", field = "books")
    public List<BookDto> books(AuthorDto author) {
        return bookRepository.findByAuthorId(author.getId()).stream().map(this::toBookDtoSimple).collect(Collectors.toList());
    }

    private AuthorDto toAuthorDto(Author author) {
        return AuthorDto.builder()
                .id(author.getId())
                .name(author.getName())
                .email(author.getEmail())
                .bio(author.getBio())
                .books(author.getBooks() != null ?
                        author.getBooks().stream().map(this::toBookDtoSimple).collect(Collectors.toList()) :
                        Collections.emptyList())
                .build();
    }

    private BookDto toBookDtoSimple(Book book) {
        return BookDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .price(book.getPrice())
                .build();
    }
}
