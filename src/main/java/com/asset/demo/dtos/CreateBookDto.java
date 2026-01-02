package com.asset.demo.dtos;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request object for creating a new book")
public class CreateBookDto {

    private String title;
    private Long authorId;
    private String isbn;
    private Double price;

}
