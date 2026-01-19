package com.asset.demo.events;

import com.asset.demo.entities.Book;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Log4j2
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
        log.debug("\uD83D\uDCE2 Published bookCreated event: {}", book.getTitle());
    }

    /**
     * Emit book updated event
     */
    public void publishBookUpdated(Book book) {
        bookUpdatedSink.tryEmitNext(book);
        log.debug("\uD83D\uDCE2 Published bookUpdated event: {}", book.getTitle());
    }

    /**
     * Emit book deleted event
     */
    public void publishBookDeleted(Long bookId) {
        bookDeletedSink.tryEmitNext(bookId);
        log.debug("\uD83D\uDCE2 Published bookDeleted event: {}", bookId);
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
