package com.asset.demo.configs;

import com.asset.demo.entities.Book;
import com.asset.demo.repositories.BookRepository;
import lombok.RequiredArgsConstructor;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderFactory;
import org.dataloader.DataLoaderRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Configuration
public class DataLoaderConfig {

    private final BookRepository bookRepository;

    @Bean
    public WebGraphQlInterceptor dataLoaderInterceptor() {
        return new WebGraphQlInterceptor() {
            @Override
            public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
                System.out.println("=== WebGraphQlInterceptor invoked ===");

                DataLoaderRegistry registry = new DataLoaderRegistry();

                DataLoader<Long, List<Book>> booksLoader = DataLoaderFactory.newDataLoader(
                        authorIds -> CompletableFuture.supplyAsync(() -> {
                            System.out.println(">>> BatchLoader executing for IDs: " + authorIds);

                            List<Book> books = bookRepository.findAllByAuthorIds(authorIds);

                            Map<Long, List<Book>> grouped = books.stream()
                                    .collect(Collectors.groupingBy(b -> b.getAuthor().getId()));

                            return authorIds.stream()
                                    .map(id -> grouped.getOrDefault(id, Collections.emptyList()))
                                    .collect(Collectors.toList());
                        })
                );

                registry.register("booksByAuthorIds", booksLoader);
                System.out.println(">>> Registered booksByAuthorIds in interceptor");

                request.configureExecutionInput((executionInput, builder) ->
                        builder.dataLoaderRegistry(registry).build()
                );

                return chain.next(request);
            }
        };
    }
}
