package com.asset.demo.configs;

import graphql.analysis.MaxQueryDepthInstrumentation;
import graphql.analysis.MaxQueryComplexityInstrumentation;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class GraphQLConfig {

    /**
     * Limit query depth to prevent deeply nested queries
     * Example: book { author { books { author { books { ... }}}}}
     */
    @Bean
    public MaxQueryDepthInstrumentation maxQueryDepthInstrumentation() {
        return new MaxQueryDepthInstrumentation(5); // Max 5 levels deep
    }

    /**
     * Limit query complexity to prevent expensive queries
     */
    @Bean
    public MaxQueryComplexityInstrumentation maxQueryComplexityInstrumentation() {
        return new MaxQueryComplexityInstrumentation(100); // Max complexity score
    }
}
