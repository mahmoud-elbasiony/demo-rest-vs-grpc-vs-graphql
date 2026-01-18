package com.asset.demo.dtos;

import com.asset.demo.entities.Author;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorPage {
    private List<Author> content;
    private PageInfo pageInfo;
}
