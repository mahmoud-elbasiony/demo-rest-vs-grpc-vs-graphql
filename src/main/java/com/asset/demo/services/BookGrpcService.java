package com.asset.demo.services;

import com.asset.demo.entities.Author;
import com.asset.demo.entities.Book;
import com.asset.demo.grpc.AuthorIdRequest;
import com.asset.demo.grpc.AuthorMessage;
import com.asset.demo.grpc.AuthorSearchRequest;
import com.asset.demo.grpc.BookCreationResult;
import com.asset.demo.grpc.BookIdRequest;
import com.asset.demo.grpc.BookListResponse;
import com.asset.demo.grpc.BookMessage;
import com.asset.demo.grpc.BookServiceGrpc;
import com.asset.demo.grpc.CreateBookRequest;
import com.asset.demo.grpc.DeleteResponse;
import com.asset.demo.grpc.EmptyRequest;
import com.asset.demo.grpc.UpdateBookRequest;
import com.asset.demo.repositories.AuthorRepository;
import com.asset.demo.repositories.BookRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@GrpcService
public class BookGrpcService extends BookServiceGrpc.BookServiceImplBase {
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    @Override
    public void getAllBooks(EmptyRequest request, StreamObserver<BookListResponse> responseObserver) {
        List<BookMessage> books = bookRepository.findAll().stream()
                .map(this::toProto)
                .collect(Collectors.toList());

        BookListResponse response = BookListResponse.newBuilder()
                .addAllBooks(books)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getBook(BookIdRequest request, StreamObserver<BookMessage> responseObserver) {
        bookRepository.findById(request.getId())
                .ifPresentOrElse(
                        book -> {
                            responseObserver.onNext(toProto(book));
                            responseObserver.onCompleted();
                        },
                        () -> responseObserver.onError(
                                io.grpc.Status.NOT_FOUND
                                        .withDescription("Book not found")
                                        .asRuntimeException()
                        )
                );
    }

    @Override
    public void createBook(CreateBookRequest request, StreamObserver<BookMessage> responseObserver) {
        authorRepository.findById(request.getAuthorId())
                .ifPresentOrElse(
                        author -> {
                            Book book = Book.builder()
                                    .title(request.getTitle())
                                    .isbn(request.getIsbn())
                                    .price(request.getPrice())
                                    .author(author)
                                    .build();

                            Book saved = bookRepository.save(book);
                            responseObserver.onNext(toProto(saved));
                            responseObserver.onCompleted();
                        },
                        () -> responseObserver.onError(
                                io.grpc.Status.NOT_FOUND
                                        .withDescription("Author not found")
                                        .asRuntimeException()
                        )
                );
    }

    @Override
    public void updateBook(UpdateBookRequest request, StreamObserver<BookMessage> responseObserver) {
        bookRepository.findById(request.getId())
                .ifPresentOrElse(
                        book -> {
                            if (!request.getTitle().isEmpty()) book.setTitle(request.getTitle());
                            if (!request.getIsbn().isEmpty()) book.setIsbn(request.getIsbn());
                            if (request.getPrice() > 0) book.setPrice(request.getPrice());
                            if (request.getAuthorId() > 0) {
                                authorRepository.findById(request.getAuthorId())
                                        .ifPresent(book::setAuthor);
                            }

                            Book updated = bookRepository.save(book);
                            responseObserver.onNext(toProto(updated));
                            responseObserver.onCompleted();
                        },
                        () -> responseObserver.onError(
                                io.grpc.Status.NOT_FOUND
                                        .withDescription("Book not found")
                                        .asRuntimeException()
                        )
                );
    }

    @Override
    public void deleteBook(BookIdRequest request, StreamObserver<DeleteResponse> responseObserver) {
        boolean exists = bookRepository.existsById(request.getId());
        if (exists) {
            bookRepository.deleteById(request.getId());
        }

        DeleteResponse response = DeleteResponse.newBuilder()
                .setSuccess(exists)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void searchByAuthor(AuthorSearchRequest request, StreamObserver<BookListResponse> responseObserver) {
        List<BookMessage> books = bookRepository.findByAuthorName(request.getAuthorName()).stream()
                .map(this::toProto)
                .collect(Collectors.toList());

        BookListResponse response = BookListResponse.newBuilder()
                .addAllBooks(books)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void streamBooksByAuthor(AuthorIdRequest request, StreamObserver<BookMessage> responseObserver) {
        // Find books (you can also use paging or reactive flux under the hood)
        List<Book> books = bookRepository.findByAuthorId(request.getId());

        // Stream each book one by one
        for (Book book : books) {
            responseObserver.onNext(toProto(book));

            // Optional: simulate delay or add back-pressure handling
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException e) {}
        }

        // Important: signal end of stream
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<CreateBookRequest> bulkCreateBooks(StreamObserver<BookListResponse> responseObserver) {
        List<Book> created = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        return new StreamObserver<>() {
            @Override
            public void onNext(CreateBookRequest req) {
                try {
                    // create logic
                    Author author = authorRepository.findById(req.getAuthorId()).orElseThrow();
                    Book book = Book.builder()
                            .title(req.getTitle())
                            .isbn(req.getIsbn())
                            .price(req.getPrice())
                            .author(author)
                            .build();
                    bookRepository.save(book);
                    created.add(book);
                } catch (Exception e) {
                    errors.add("Failed for title: " + req.getTitle() + " → " + e.getMessage());
                }
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                BookListResponse resp = BookListResponse.newBuilder()
                        .addAllBooks(created.stream().map(BookGrpcService.this::toProto).collect(Collectors.toList()))
                        .build();
                responseObserver.onNext(resp);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<CreateBookRequest> bulkCreateBooksStream(
            StreamObserver<BookCreationResult> responseObserver) {

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        return new StreamObserver<CreateBookRequest>() {

            @Override
            public void onNext(CreateBookRequest request) {
                BookCreationResult.Builder resultBuilder = BookCreationResult.newBuilder();

                try {
                    // Validate & create
                    Author author = authorRepository.findById(request.getAuthorId())
                            .orElseThrow(() -> new IllegalArgumentException("Author not found"));

                    // Optional: check ISBN uniqueness, etc.
                    if (bookRepository.existsByIsbn(request.getIsbn())) {
                        throw new IllegalArgumentException("ISBN already exists");
                    }

                    Book book = Book.builder()
                            .title(request.getTitle())
                            .isbn(request.getIsbn())
                            .price(request.getPrice())
                            .author(author)
                            .build();

                    Book saved = bookRepository.save(book);

                    // Send success response immediately
                    responseObserver.onNext(resultBuilder
                            .setSuccess(true)
                            .setCreatedBook(toProto(saved))
                            .build());

                    successCount.incrementAndGet();

                } catch (Exception e) {
                    // Send error response immediately
                    responseObserver.onNext(resultBuilder
                            .setSuccess(false)
                            .setErrorMessage(e.getMessage())
                            .build());

                    failCount.incrementAndGet();
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("Client error during bulk stream", t);
                // You can send final error summary if desired
                sendFinalSummary(responseObserver, successCount.get(), failCount.get());
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                log.info("Bulk create stream completed - success: {}, failed: {}",
                        successCount.get(), failCount.get());

                // Send final summary
                sendFinalSummary(responseObserver, successCount.get(), failCount.get());

                responseObserver.onCompleted();
            }

            private void sendFinalSummary(StreamObserver<BookCreationResult> responseObserver,
                                          int success, int failed) {
                responseObserver.onNext(BookCreationResult.newBuilder()
                        .setSuccess(true)
                        .setErrorMessage("Bulk operation completed")
                        .setErrorMessage(String.format("Bulk operation completed with Success: %d, Failed: %d", success, failed))
                        .build());
            }
        };
    }

    private BookMessage toProto(Book book) {
        return BookMessage.newBuilder()
                .setId(book.getId())
                .setTitle(book.getTitle())
                .setIsbn(book.getIsbn())
                .setPrice(book.getPrice())
                .setAuthor(authorToProto(book.getAuthor()))
                .build();
    }

    private AuthorMessage authorToProto(Author author) {
        return AuthorMessage.newBuilder()
                .setId(author.getId())
                .setName(author.getName())
                .setEmail(author.getEmail() != null ? author.getEmail() : "")
                .setBio(author.getBio() != null ? author.getBio() : "")
                .build();
    }
}
