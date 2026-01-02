package com.asset.demo.services;

import com.asset.demo.grpc.AuthorIdRequest;
import com.asset.demo.grpc.AuthorListResponse;
import com.asset.demo.grpc.AuthorServiceGrpc;
import com.asset.demo.grpc.CreateAuthorRequest;
import com.asset.demo.grpc.UpdateAuthorRequest;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import com.asset.demo.entities.Author;
import com.asset.demo.entities.Book;
import com.asset.demo.grpc.AuthorMessage;
import com.asset.demo.grpc.BookListResponse;
import com.asset.demo.grpc.BookMessage;
import com.asset.demo.grpc.DeleteResponse;
import com.asset.demo.grpc.EmptyRequest;
import com.asset.demo.repositories.AuthorRepository;
import com.asset.demo.repositories.BookRepository;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@GrpcService
public class AuthorGrpcService extends AuthorServiceGrpc.AuthorServiceImplBase {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;


    @Override
    public void getAllAuthors(EmptyRequest request, StreamObserver<AuthorListResponse> responseObserver) {
        List<AuthorMessage> authors = authorRepository.findAll().stream()
                .map(this::toProto)
                .collect(Collectors.toList());

        AuthorListResponse response = AuthorListResponse.newBuilder()
                .addAllAuthors(authors)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getAuthor(AuthorIdRequest request, StreamObserver<AuthorMessage> responseObserver) {
        authorRepository.findById(request.getId())
                .ifPresentOrElse(
                        author -> {
                            responseObserver.onNext(toProto(author));
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
    public void createAuthor(CreateAuthorRequest request, StreamObserver<AuthorMessage> responseObserver) {
        Author author = Author.builder()
                .name(request.getName())
                .email(request.getEmail())
                .bio(request.getBio())
                .build();

        Author saved = authorRepository.save(author);
        responseObserver.onNext(toProto(saved));
        responseObserver.onCompleted();
    }

    @Override
    public void updateAuthor(UpdateAuthorRequest request, StreamObserver<AuthorMessage> responseObserver) {
        authorRepository.findById(request.getId())
                .ifPresentOrElse(
                        author -> {
                            if (!request.getName().isEmpty()) author.setName(request.getName());
                            if (!request.getEmail().isEmpty()) author.setEmail(request.getEmail());
                            if (!request.getBio().isEmpty()) author.setBio(request.getBio());

                            Author updated = authorRepository.save(author);
                            responseObserver.onNext(toProto(updated));
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
    public void deleteAuthor(AuthorIdRequest request, StreamObserver<DeleteResponse> responseObserver) {
        boolean exists = authorRepository.existsById(request.getId());
        if (exists) {
            authorRepository.deleteById(request.getId());
        }

        DeleteResponse response = DeleteResponse.newBuilder()
                .setSuccess(exists)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getAuthorBooks(AuthorIdRequest request, StreamObserver<BookListResponse> responseObserver) {
        List<BookMessage> books = bookRepository.findByAuthorId(request.getId()).stream()
                .map(this::bookToProto)
                .collect(Collectors.toList());

        BookListResponse response = BookListResponse.newBuilder()
                .addAllBooks(books)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private AuthorMessage toProto(Author author) {
        return AuthorMessage.newBuilder()
                .setId(author.getId())
                .setName(author.getName())
                .setEmail(author.getEmail() != null ? author.getEmail() : "")
                .setBio(author.getBio() != null ? author.getBio() : "")
                .build();
    }

    private BookMessage bookToProto(Book book) {
        return BookMessage.newBuilder()
                .setId(book.getId())
                .setTitle(book.getTitle())
                .setIsbn(book.getIsbn())
                .setPrice(book.getPrice())
                .setAuthor(toProto(book.getAuthor()))
                .build();
    }
}
