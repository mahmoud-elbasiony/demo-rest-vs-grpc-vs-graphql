package com.asset.demo.services;

import com.asset.demo.entities.Book;
import com.asset.demo.grpc.AuthorRequest;
import com.asset.demo.grpc.BookIdRequest;
import com.asset.demo.grpc.BookListResponse;
import com.asset.demo.grpc.BookMessage;
import com.asset.demo.grpc.BookServiceGrpc;
import com.asset.demo.grpc.CreateBookRequest;
import com.asset.demo.grpc.DeleteResponse;
import com.asset.demo.grpc.EmptyRequest;
import com.asset.demo.grpc.UpdateBookRequest;
import com.asset.demo.repositories.BookRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class BookGrpcService extends BookServiceGrpc.BookServiceImplBase {

    private final BookRepository repository;

    public BookGrpcService(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public void getAllBooks(EmptyRequest request, StreamObserver<BookListResponse> responseObserver) {
        List<BookMessage> books = repository.findAll().stream()
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
        repository.findById(request.getId())
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
        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .price(request.getPrice())
                .build();

        Book saved = repository.save(book);
        responseObserver.onNext(toProto(saved));
        responseObserver.onCompleted();
    }

    @Override
    public void updateBook(UpdateBookRequest request, StreamObserver<BookMessage> responseObserver) {
        repository.findById(request.getId())
                .ifPresentOrElse(
                        book -> {
                            if (!request.getTitle().isEmpty()) book.setTitle(request.getTitle());
                            if (!request.getAuthor().isEmpty()) book.setAuthor(request.getAuthor());
                            if (!request.getIsbn().isEmpty()) book.setIsbn(request.getIsbn());
                            if (request.getPrice() > 0) book.setPrice(request.getPrice());

                            Book updated = repository.save(book);
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
        boolean exists = repository.existsById(request.getId());
        if (exists) {
            repository.deleteById(request.getId());
        }

        DeleteResponse response = DeleteResponse.newBuilder()
                .setSuccess(exists)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void searchByAuthor(AuthorRequest request, StreamObserver<BookListResponse> responseObserver) {
        List<BookMessage> books = repository.findByAuthor(request.getAuthor()).stream()
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
                .setAuthor(book.getAuthor())
                .setIsbn(book.getIsbn())
                .setPrice(book.getPrice())
                .build();
    }
}
