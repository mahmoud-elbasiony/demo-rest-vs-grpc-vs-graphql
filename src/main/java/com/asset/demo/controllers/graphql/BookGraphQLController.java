package com.asset.demo.controllers.graphql;

import com.asset.demo.dtos.BookPage;
import com.asset.demo.dtos.CreateBookDto;
import com.asset.demo.dtos.PageInfo;
import com.asset.demo.dtos.UpdateBookDto;
import com.asset.demo.entities.Author;
import com.asset.demo.entities.Book;
import com.asset.demo.repositories.AuthorRepository;
import com.asset.demo.repositories.BookRepository;
import com.asset.demo.services.BookEventPublisher;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@Controller
public class BookGraphQLController {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final BookEventPublisher bookEventPublisher;

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
                    Book savedBook = bookRepository.save(book);

                    // 📢 Publish event for subscriptions
                    bookEventPublisher.publishBookCreated(savedBook);

                    return savedBook;
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
                    Book saved = bookRepository.save(book);                    // 📢 Publish event for subscriptions
                    bookEventPublisher.publishBookUpdated(saved);
                    return saved;
                })
                .orElse(null);
    }

    @MutationMapping
    public Boolean deleteBook(@Argument Long id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            // 📢 Publish event for subscriptions
            bookEventPublisher.publishBookDeleted(id);
            return true;
        }
        return false;
    }

    @QueryMapping
    public BookPage booksPaginated(@Argument int page, @Argument int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.findAll(pageable);

        List<Book> books = bookPage.getContent();

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

    /**
     * Subscribe to newly created books
     * Usage: subscription { bookCreated { id title price } }
     */
    @SubscriptionMapping
    public Flux<Book> bookCreatedSubscription() {
        return bookEventPublisher.getBookCreatedFlux();
    }

    /**
     * Subscribe to book updates
     * Usage: subscription { bookUpdated { id title price } }
     */
    @SubscriptionMapping
    public Flux<Book> bookUpdatedSubscription() {
        return bookEventPublisher.getBookUpdatedFlux();
    }

    /**
     * Subscribe to book deletions
     * Usage: subscription { bookDeleted }
     */
    @SubscriptionMapping
    public Flux<Long> bookDeletedSubscription() {
        return bookEventPublisher.getBookDeletedFlux();
    }

    /**
     * Subscribe to specific book updates by ID
     * Usage: subscription { bookById(id: 1) { id title price } }
     */
    @SubscriptionMapping
    public Flux<Book> bookByIdSubscription(@Argument Long id) {
        return bookEventPublisher.getBookUpdatedFlux()
                .filter(book -> book.getId().equals(id));
    }

    /**
     * Subscribe to books by specific author
     * Usage: subscription { booksByAuthor(authorId: 1) { id title } }
     */
    @SubscriptionMapping
    public Flux<Book> booksByAuthorSubscription(@Argument Long authorId) {
        return bookEventPublisher.getBookCreatedFlux()
                .filter(book -> book.getAuthor() != null &&
                                book.getAuthor().getId().equals(authorId));
    }

    /**
     * Subscribe to price changes above threshold
     * Usage: subscription { priceChanges(minPrice: 20.0) { id title price } }
     */
    @SubscriptionMapping
    public Flux<Book> priceChangesSubscription(@Argument Double minPrice) {
        return bookEventPublisher.getBookUpdatedFlux()
                .filter(book -> book.getPrice() >= minPrice);
    }

    /**
     * Subscribe to all book events (created, updated)
     * Usage: subscription { bookEvents { id title price } }
     */
    @SubscriptionMapping
    public Flux<Book> bookEventsSubscription() {
        return Flux.merge(
                bookEventPublisher.getBookCreatedFlux(),
                bookEventPublisher.getBookUpdatedFlux()
        );
    }
}
