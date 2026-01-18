package com.asset.demo.repositories;

import com.asset.demo.entities.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    Optional<Author> findByName(String name);
    Optional<Author> findByEmail(String email);

    // Pagination support
    Page<Author> findAll(Pageable pageable);

    // Prevent N+1 problem - fetch author with books in single query
    @EntityGraph(attributePaths = {"books"})
    @Query("SELECT a FROM Author a WHERE a.id = :id")
    Optional<Author> findByIdWithBooks(Long id);
}
