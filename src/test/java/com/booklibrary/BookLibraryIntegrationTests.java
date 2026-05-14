package com.booklibrary;
import com.booklibrary.dto.BookRequest;
import com.booklibrary.model.Book;
import com.booklibrary.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests that use a real MongoDB database via Testcontainers.
 * Similar to the JavaScript/Mocha tests that connected to a real MongoDB instance.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookLibraryIntegrationTests {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Clear and seed the database before each test (like JavaScript beforeEach)
        bookRepository.deleteAll();
        
        // Seed test data - similar to mongo-init.js
        bookRepository.save(new Book(
            "Monkey with a Tool Belt",
            0,
            "Whether you need a beebersaw or a chisel",
            "https://i.ibb.co/b5yH8THj/image.jpg",
            "Chris Monroe",
            "2008"
        ));
        
        bookRepository.save(new Book(
            "Monkey with a Tool Belt and the Noisy Problem",
            1,
            "Chico Bon Bon has a problem",
            "https://i.ibb.co/zW2VKwFh/1.jpg",
            "Chris Monroe",
            "2009"
        ));
        
        bookRepository.save(new Book(
            "Monkey with a Tool Belt and the Seaside Shenanigans",
            2,
            "Clark has an enormous problem",
            "https://i.ibb.co/0VjpSyPM/2.jpg",
            "Chris Monroe",
            "2011"
        ));
    }

    @Test
    @Order(1)
    @DisplayName("GET / - should return index page")
    void testIndexPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    @DisplayName("GET /os - should return OS info")
    void testOsEndpoint() throws Exception {
        mockMvc.perform(get("/os"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.os").exists())
                .andExpect(jsonPath("$.env").exists());
    }

    @Test
    @Order(3)
    @DisplayName("GET /live - should return live status")
    void testLivenessEndpoint() throws Exception {
        mockMvc.perform(get("/live"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("live"));
    }

    @Test
    @Order(4)
    @DisplayName("GET /ready - should return ready status")
    void testReadinessEndpoint() throws Exception {
        mockMvc.perform(get("/ready"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ready"));
    }

    @Test
    @Order(5)
    @DisplayName("POST /book - should return book with id 0")
    void testGetBookId0() throws Exception {
        BookRequest request = new BookRequest(0);

        mockMvc.perform(post("/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(0))
                .andExpect(jsonPath("$.name").value("Monkey with a Tool Belt"))
                .andExpect(jsonPath("$.author").value("Chris Monroe"));
    }

    @Test
    @Order(6)
    @DisplayName("POST /book - should return book with id 1")
    void testGetBookId1() throws Exception {
        BookRequest request = new BookRequest(1);

        mockMvc.perform(post("/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Monkey with a Tool Belt and the Noisy Problem"))
                .andExpect(jsonPath("$.author").value("Chris Monroe"));
    }

    @Test
    @Order(7)
    @DisplayName("POST /book - should return book with id 2")
    void testGetBookId2() throws Exception {
        BookRequest request = new BookRequest(2);

        mockMvc.perform(post("/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Monkey with a Tool Belt and the Seaside Shenanigans"))
                .andExpect(jsonPath("$.author").value("Chris Monroe"));
    }

    @Test
    @Order(8)
    @DisplayName("POST /book - should return 404 for non-existent book")
    void testGetBookNotFound() throws Exception {
        BookRequest request = new BookRequest(999);

        mockMvc.perform(post("/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(9)
    @DisplayName("Database should contain seeded books")
    void testDatabaseContainsBooks() {
        long count = bookRepository.count();
        Assertions.assertEquals(3, count, "Database should contain 3 books");
    }

    @Test
    @Order(10)
    @DisplayName("Repository should find book by custom id field")
    void testRepositoryFindById() {
        var book = bookRepository.findById(1);
        Assertions.assertTrue(book.isPresent(), "Book with id 1 should exist");
        Assertions.assertEquals("Monkey with a Tool Belt and the Noisy Problem", book.get().getName());
    }
}
