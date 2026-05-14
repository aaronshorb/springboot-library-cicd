package com.booklibrary;

import com.booklibrary.controller.SystemController;
import com.booklibrary.controller.BookController;
import com.booklibrary.dto.BookRequest;
import com.booklibrary.model.Book;
import com.booklibrary.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests using mocked MongoDB repository.
 * These tests are fast and don't require a real database.
 */
@WebMvcTest({BookController.class, SystemController.class})
class BookLibraryApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookRepository bookRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Context loads")
    void contextLoads() {
    }

    @Test
    @DisplayName("POST /book - should return book (mocked)")
    void testGetBook_Success() throws Exception {
        Book book = new Book(
            "Monkey with a Tool Belt",
            1,
            "Whether you need a beebersaw or a chisel",
            "http://example.com/image.jpg",
            "Chris Monroe",
            "2008"
        );

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        BookRequest request = new BookRequest(1);

        mockMvc.perform(post("/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Monkey with a Tool Belt"))
                .andExpect(jsonPath("$.author").value("Chris Monroe"));
    }

    @Test
    @DisplayName("POST /book - should return 404 when not found (mocked)")
    void testGetBook_NotFound() throws Exception {
        when(bookRepository.findById(any(Integer.class))).thenReturn(Optional.empty());

        BookRequest request = new BookRequest(999);

        mockMvc.perform(post("/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /live - should return live status")
    void testLivenessEndpoint() throws Exception {
        mockMvc.perform(get("/live"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("live"));
    }

    @Test
    @DisplayName("GET /ready - should return ready status")
    void testReadinessEndpoint() throws Exception {
        mockMvc.perform(get("/ready"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ready"));
    }

    @Test
    @DisplayName("GET /os - should return OS info")
    void testOsEndpoint() throws Exception {
        mockMvc.perform(get("/os"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.os").exists())
                .andExpect(jsonPath("$.env").exists());
    }
}
