package com.asset.demo.services;

import com.asset.demo.entities.Book;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class BookEventPublisher {

    // Sinks for different event types
    private final Sinks.Many<Book> bookCreatedSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Many<Book> bookUpdatedSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Many<Long> bookDeletedSink = Sinks.many().multicast().onBackpressureBuffer();

    /**
     * Emit book created event
     */
    public void publishBookCreated(Book book) {
        bookCreatedSink.tryEmitNext(book);
        System.out.println("📢 Published bookCreated event: " + book.getTitle());
    }

    /**
     * Emit book updated event
     */
    public void publishBookUpdated(Book book) {
        bookUpdatedSink.tryEmitNext(book);
        System.out.println("📢 Published bookUpdated event: " + book.getTitle());
    }

    /**
     * Emit book deleted event
     */
    public void publishBookDeleted(Long bookId) {
        bookDeletedSink.tryEmitNext(bookId);
        System.out.println("📢 Published bookDeleted event: " + bookId);
    }

    /**
     * Get flux for book created events
     */
    public Flux<Book> getBookCreatedFlux() {
        return bookCreatedSink.asFlux();
    }

    /**
     * Get flux for book updated events
     */
    public Flux<Book> getBookUpdatedFlux() {
        return bookUpdatedSink.asFlux();
    }

    /**
     * Get flux for book deleted events
     */
    public Flux<Long> getBookDeletedFlux() {
        return bookDeletedSink.asFlux();
    }
}
