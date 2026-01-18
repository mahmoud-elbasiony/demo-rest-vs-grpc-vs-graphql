package com.asset.demo.dtos;

import com.asset.demo.entities.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookPage {
    private List<Book> content;
    private PageInfo pageInfo;
}
