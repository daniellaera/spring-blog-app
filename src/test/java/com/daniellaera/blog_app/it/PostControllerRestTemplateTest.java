package com.daniellaera.blog_app.it;

import com.daniellaera.blog_app.model.Post;
import com.daniellaera.blog_app.repository.PostRepository;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test-containers-flyway")
@FlywayTest
@Testcontainers
public class PostControllerRestTemplateTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PostRepository postRepository;

    private String baseUrl;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setup() {
        postRepository.deleteAll();

        Post post1 = new Post();
        post1.setTitle("Title 1");
        post1.setContent("Content 1");

        Post post2 = new Post();
        post2.setTitle("Title 2");
        post2.setContent("Content 2");

        postRepository.saveAll(Arrays.asList(post1, post2));

        baseUrl = "http://localhost:" + port + "/api/v3/post";
    }

    @Test
    void getPosts_ReturnsAllPosts() {
        ResponseEntity<List<Post>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Post>>() {}
        );

        assertEquals(200, response.getStatusCodeValue());
        List<Post> posts = response.getBody();
        assertNotNull(posts);
        assertEquals(2, posts.size());
        assertEquals("Title 1", posts.get(0).getTitle());
        assertEquals("Title 2", posts.get(1).getTitle());
    }

    @Test
    void getPosts_WhenNoPosts_ReturnsEmptyArray() {
        postRepository.deleteAll();
        ResponseEntity<List<Post>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Post>>() {}
        );

        assertEquals(200, response.getStatusCodeValue());
        List<Post> posts = response.getBody();
        assertNotNull(posts);
        assertEquals(0, posts.size());
    }

    @Test
    void getPosts_CheckSingleFieldContent() {
        ResponseEntity<List<Post>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Post>>() {}
        );

        assertEquals(200, response.getStatusCodeValue());
        List<Post> posts = response.getBody();
        assertNotNull(posts);
        assertEquals("Content 1", posts.get(0).getContent());
        assertEquals("Content 2", posts.get(1).getContent());
    }
}
