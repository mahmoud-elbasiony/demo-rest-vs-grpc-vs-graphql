package com.asset.demo.controllers;

import com.asset.demo.dtos.BookDto;
import com.asset.demo.dtos.CreateBookDto;
import com.asset.demo.dtos.DeleteResultDto;
import com.asset.demo.dtos.UpdateBookDto;
import com.asset.demo.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grpc-gateway/books")
public class BookGrpcGatewayController {

    // Inject gRPC client stub
    @GrpcClient("bookService")
    private BookServiceGrpc.BookServiceBlockingStub bookServiceStub;

    // GET /api/grpc-gateway/books
    @GetMapping
    public ResponseEntity<List<BookDto>> getAllBooks() {
        EmptyRequest request = EmptyRequest.newBuilder().build();
        BookListResponse response = bookServiceStub.getAllBooks(request);

        List<BookDto> books = response.getBooksList().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(books);
    }

    // GET /api/grpc-gateway/books/1
    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBookById(@PathVariable Long id) {
        try {
            BookIdRequest request = BookIdRequest.newBuilder()
                    .setId(id)
                    .build();

            BookMessage response = bookServiceStub.getBook(request);
            return ResponseEntity.ok(toDTO(response));
        } catch (io.grpc.StatusRuntimeException e) {
            if (e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    // POST /api/grpc-gateway/books
    @PostMapping
    public ResponseEntity<BookDto> createBook(@RequestBody CreateBookDto dto) {
        CreateBookRequest request = CreateBookRequest.newBuilder()
                .setTitle(dto.getTitle())
                .setAuthor(dto.getAuthor())
                .setIsbn(dto.getIsbn())
                .setPrice(dto.getPrice())
                .build();

        BookMessage response = bookServiceStub.createBook(request);
        return ResponseEntity.ok(toDTO(response));
    }

    // PUT /api/grpc-gateway/books/1
    @PutMapping("/{id}")
    public ResponseEntity<BookDto> updateBook(@PathVariable Long id, @RequestBody UpdateBookDto dto) {
        try {
            UpdateBookRequest request = UpdateBookRequest.newBuilder()
                    .setId(id)
                    .setTitle(dto.getTitle() != null ? dto.getTitle() : "")
                    .setAuthor(dto.getAuthor() != null ? dto.getAuthor() : "")
                    .setIsbn(dto.getIsbn() != null ? dto.getIsbn() : "")
                    .setPrice(dto.getPrice() != null ? dto.getPrice() : 0.0)
                    .build();

            BookMessage response = bookServiceStub.updateBook(request);
            return ResponseEntity.ok(toDTO(response));
        } catch (io.grpc.StatusRuntimeException e) {
            if (e.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    // DELETE /api/grpc-gateway/books/1
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResultDto> deleteBook(@PathVariable Long id) {
        BookIdRequest request = BookIdRequest.newBuilder()
                .setId(id)
                .build();

        DeleteResponse response = bookServiceStub.deleteBook(request);
        return ResponseEntity.ok(new DeleteResultDto(response.getSuccess()));
    }

    // GET /api/grpc-gateway/books/search?author=Shakespeare
    @GetMapping("/search")
    public ResponseEntity<List<BookDto>> searchByAuthor(@RequestParam String author) {
        AuthorRequest request = AuthorRequest.newBuilder()
                .setAuthor(author)
                .build();

        BookListResponse response = bookServiceStub.searchByAuthor(request);

        List<BookDto> books = response.getBooksList().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(books);
    }

    private BookDto toDTO(BookMessage message) {
        return new BookDto(
                message.getId(),
                message.getTitle(),
                message.getAuthor(),
                message.getIsbn(),
                message.getPrice()
        );
    }
}
