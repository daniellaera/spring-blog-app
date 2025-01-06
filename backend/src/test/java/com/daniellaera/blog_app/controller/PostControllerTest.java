package com.daniellaera.blog_app.controller;

import com.daniellaera.blog_app.dto.CommentDTO;
import com.daniellaera.blog_app.dto.PostDTO;
import com.daniellaera.blog_app.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @InjectMocks
    private PostController postController;

    private MockMvc mockMvc;

    @Mock
    private PostService postService;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(postController).build();
    }

    @Test
    void getPosts() throws Exception {
        PostDTO post1 = new PostDTO(1L, "title", "content", "Jean marc", List.of());
        PostDTO post2 = new PostDTO(2L, "title2", "content2", "Christophe", List.of());
        PostDTO post3 = new PostDTO(3L, "title3", "content3", "Denny", List.of());

        List<PostDTO> posts = Arrays.asList(post1, post2, post3);
        given(postService.getAllPosts()).willReturn(posts);
        String reqBody = new ObjectMapper().writeValueAsString(posts);

        mockMvc.perform(get("/api/v3/post")
                .accept(MediaType.APPLICATION_JSON)
                .content(reqBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].title").value("title"))
                .andExpect(jsonPath("$[0].content").value("content"));

        verify(postService, times(1)).getAllPosts();
    }

    @Test
    void getPostsWithComments() throws Exception {
        CommentDTO comment1 = new CommentDTO("First comment");
        CommentDTO comment2 = new CommentDTO("Second comment");

        PostDTO post1 = new PostDTO(1L, "title", "content","Jean marc", List.of(comment1, comment2));
        PostDTO post2 = new PostDTO(2L, "title2", "content2","Christophe", List.of(new CommentDTO("Another comment")));

        List<PostDTO> posts = Arrays.asList(post1, post2);
        given(postService.getAllPosts()).willReturn(posts);

        String reqBody = new ObjectMapper().writeValueAsString(posts);

        mockMvc.perform(get("/api/v3/post")
                        .accept(MediaType.APPLICATION_JSON)
                        .content(reqBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("title"))
                .andExpect(jsonPath("$[0].content").value("content"))
                .andExpect(jsonPath("$[0].comments.length()").value(2))
                .andExpect(jsonPath("$[0].comments[0].text").value("First comment"))
                .andExpect(jsonPath("$[0].comments[1].text").value("Second comment"))
                .andExpect(jsonPath("$[1].title").value("title2"))
                .andExpect(jsonPath("$[1].content").value("content2"))
                .andExpect(jsonPath("$[1].comments.length()").value(1))
                .andExpect(jsonPath("$[1].comments[0].text").value("Another comment"));

        verify(postService, times(1)).getAllPosts();
    }

    @Test
    void getPostById() throws Exception {
        PostDTO post = new PostDTO(1L, "title", "content","Rubert", List.of());

        given(postService.getPostById(any(Long.class))).willReturn(Optional.of(post));
        mockMvc.perform(get("/api/v3/post/" + 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("title"))
                .andExpect(jsonPath("$.content").value("content"));

        verify(postService, times(1)).getPostById(any(Long.class));
    }

    @Test
    void getPostByIdNotFound() throws Exception {
        given(postService.getPostById(any(Long.class))).willReturn(Optional.empty());

        mockMvc.perform(get("/api/v3/post/" + 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPost() throws Exception {
        PostDTO post = new PostDTO(1L, "title", "content", "Lucas", List.of());

        given(postService.createPost(refEq(post))).willReturn(post);
        String reqBody = new ObjectMapper().writeValueAsString(post);

        mockMvc.perform(post("/api/v3/post")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(reqBody))
                .andExpect(status().isCreated())
                .andExpect(content().json(reqBody));

    }

    @Test
    void deletePost() throws Exception {
        willDoNothing().given(postService).deleteById(any(Long.class));
        mockMvc.perform(delete("/api/v3/post/{id}", + 1L)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(postService, times(1)).deleteById(any(Long.class));
    }
}