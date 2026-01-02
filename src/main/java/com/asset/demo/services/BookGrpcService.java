package com.asset.demo.services;

import com.asset.demo.entities.Author;
import com.asset.demo.entities.Book;
import com.asset.demo.grpc.AuthorMessage;
import com.asset.demo.grpc.AuthorSearchRequest;
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
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.stream.Collectors;

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
