package com.booklibrary.controller;

import com.booklibrary.dto.BookRequest;
import com.booklibrary.model.Book;
import com.booklibrary.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @PostMapping("/book")
    public ResponseEntity<Book> getBook(@RequestBody BookRequest request) {
        return bookRepository.findById(request.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
