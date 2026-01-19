package com.asset.demo.controllers.graphql;

import com.asset.demo.dtos.AuthorPage;
import com.asset.demo.dtos.CreateAuthorDto;
import com.asset.demo.dtos.PageInfo;
import com.asset.demo.entities.Author;
import com.asset.demo.entities.Book;
import com.asset.demo.repositories.AuthorRepository;
import com.asset.demo.events.AuthorEventPublisher;
import com.asset.demo.repositories.BookRepository;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.dataloader.DataLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Controller
public class AuthorGraphQLController {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final AuthorEventPublisher authorEventPublisher;

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
        Author savedAuthor = authorRepository.save(author);

        // 📢 Publish event for subscriptions
        authorEventPublisher.publishAuthorCreated(savedAuthor);

        return savedAuthor;
    }

    @MutationMapping
    public Author updateAuthor(@Argument Long id, @Argument("input") CreateAuthorDto createAuthorDto) {
        return authorRepository.findById(id)
                .map(author -> {
                    if (createAuthorDto.getName() != null) author.setName(createAuthorDto.getName());
                    if (createAuthorDto.getEmail() != null) author.setEmail(createAuthorDto.getEmail());
                    if (createAuthorDto.getBio() != null) author.setBio(createAuthorDto.getBio());
                    Author saved = authorRepository.save(author);

                    // 📢 Publish event for subscriptions
                    authorEventPublisher.publishAuthorUpdated(saved);

                    return saved;
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

        List<Author> authors = authorPage.getContent();

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

    /**
     * N+1 problem solution without DataLoader
     *
     * @param author
     * @return
     */
//    @SchemaMapping(typeName = "Author", field = "books")
//    public List<Book> books(Author author) {
//        return bookRepository.findByAuthorId(author.getId());
//    }

    /**
     * N+1 problem solution with DataLoader
     *
     * @param author
     * @param env
     * @return
     */
    @SchemaMapping(typeName = "Author", field = "books")
    public CompletableFuture<List<Book>> books(
            Author author,
            DataFetchingEnvironment env) {

        DataLoader<Long, List<Book>> booksLoader =
                env.getDataLoader("booksByAuthorIds");

        return booksLoader.load(author.getId());
    }

    /**
     * Subscribe to new authors
     * Usage: subscription { authorCreated { id name } }
     */
    @SubscriptionMapping
    public Flux<Author> authorCreatedSubscription() {
        return authorEventPublisher.getAuthorCreatedFlux();
    }

    /**
     * Subscribe to author updates
     * Usage: subscription { authorUpdated { id name } }
     */
    @SubscriptionMapping
    public Flux<Author> authorUpdatedSubscription() {
        return authorEventPublisher.getAuthorUpdatedFlux();
    }

}
