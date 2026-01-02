package com.asset.demo.services;

import com.asset.demo.repositories.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BookGraphQLService {

    private final BookRepository repository;
}
