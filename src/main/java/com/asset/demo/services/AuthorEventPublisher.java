package com.asset.demo.services;

import com.asset.demo.entities.Author;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class AuthorEventPublisher {

    private final Sinks.Many<Author> authorCreatedSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Many<Author> authorUpdatedSink = Sinks.many().multicast().onBackpressureBuffer();

    public void publishAuthorCreated(Author author) {
        authorCreatedSink.tryEmitNext(author);
        System.out.println("📢 Published authorCreated event: " + author.getName());
    }

    public void publishAuthorUpdated(Author author) {
        authorUpdatedSink.tryEmitNext(author);
        System.out.println("📢 Published authorUpdated event: " + author.getName());
    }

    public Flux<Author> getAuthorCreatedFlux() {
        return authorCreatedSink.asFlux();
    }

    public Flux<Author> getAuthorUpdatedFlux() {
        return authorUpdatedSink.asFlux();
    }
}
