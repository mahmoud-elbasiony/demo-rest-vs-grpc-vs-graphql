package com.asset.demo.repositories;

import com.asset.demo.entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    // Find books by author ID
    List<Book> findByAuthorId(Long authorId);

    // Find books by author name
    List<Book> findByAuthorName(String authorName);

    // Alternative: Find books by author name (case-insensitive)
    List<Book> findByAuthorNameIgnoreCase(String authorName);

    // Alternative: Find books by author name containing
    List<Book> findByAuthorNameContainingIgnoreCase(String authorName);
}
