package com.asset.demo.repositories;

import com.asset.demo.entities.Book;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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

    // Prevent N+1 - fetch books with authors
    @EntityGraph(attributePaths = {"author"})
    @Query("SELECT b FROM Book b")
    List<Book> findAllWithAuthors();

    // Batch load books by author IDs (for DataLoader)
    @Query("SELECT b FROM Book b WHERE b.author.id IN :authorIds")
    List<Book> findAllByAuthorIds(@Param("authorIds") Collection<Long> authorIds);

    boolean existsByIsbn(String isbn);
}
