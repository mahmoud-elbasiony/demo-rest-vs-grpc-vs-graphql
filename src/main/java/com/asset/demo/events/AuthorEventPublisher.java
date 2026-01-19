package com.asset.demo.events;

import com.asset.demo.entities.Author;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
@Log4j2
public class AuthorEventPublisher {

    private final Sinks.Many<Author> authorCreatedSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Many<Author> authorUpdatedSink = Sinks.many().multicast().onBackpressureBuffer();

    public void publishAuthorCreated(Author author) {
        authorCreatedSink.tryEmitNext(author);
        log.debug("\uD83D\uDCE2 Published authorCreated event: {}", author.getName());
    }

    public void publishAuthorUpdated(Author author) {
        authorUpdatedSink.tryEmitNext(author);
        log.debug("\uD83D\uDCE2 Published authorUpdated event: {}", author.getName());
    }

    public Flux<Author> getAuthorCreatedFlux() {
        return authorCreatedSink.asFlux();
    }

    public Flux<Author> getAuthorUpdatedFlux() {
        return authorUpdatedSink.asFlux();
    }
}
