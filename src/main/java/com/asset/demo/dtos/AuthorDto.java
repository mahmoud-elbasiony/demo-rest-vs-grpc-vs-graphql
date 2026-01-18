package com.asset.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AuthorDto {
    private Long id;
    private String name;
    private String email;
    private String bio;
    private List<BookDto> books;
}
