package com.asset.demo.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookDto {
    private Long id;
    private String title;
    private AuthorDto author;
    private String isbn;
    private Double price;

}
