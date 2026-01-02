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
    public Author createAuthor(@Argument String name, @Argument String email, @Argument String bio) {
        Author author = Author.builder()
                .name(name)
                .email(email)
                .bio(bio)
                .build();
        return authorRepository.save(author);
    }

    @MutationMapping
    public Author updateAuthor(@Argument Long id, @Argument String name,
                               @Argument String email, @Argument String bio) {
        return authorRepository.findById(id)
                .map(author -> {
                    if (name != null) author.setName(name);
                    if (email != null) author.setEmail(email);
                    if (bio != null) author.setBio(bio);
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

    @SchemaMapping(typeName = "Author", field = "books")
    public List<Book> books(Author author) {
        return bookRepository.findByAuthorId(author.getId());
    }
}
