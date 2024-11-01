package com.daniellaera.blog_app.controller;

import com.daniellaera.blog_app.dto.CommentDTO;
import com.daniellaera.blog_app.service.CommentService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @InjectMocks
    private CommentController commentController;

    private MockMvc mockMvc;

    @Mock
    private CommentService commentService;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build();
    }

    @Test
    void createComment() throws Exception {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setText("This is a comment");

        given(commentService.addComment(anyLong(), any(CommentDTO.class))).willReturn(commentDTO);
        String reqBody = new ObjectMapper().writeValueAsString(commentDTO);

        mockMvc.perform(post("/api/v3/comment/{postId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqBody))
                .andExpect(status().isCreated())
                .andExpect(content().json(reqBody));
    }

    @Test
    void getCommentsByPost() throws Exception {
        CommentDTO comment1 = new CommentDTO();
        comment1.setText("Comment 1");

        CommentDTO comment2 = new CommentDTO();
        comment2.setText("Comment 2");

        List<CommentDTO> comments = Arrays.asList(comment1, comment2);
        given(commentService.getAllCommentsByPostId(anyLong())).willReturn(comments);

        mockMvc.perform(get("/api/v3/comment/{postId}/comments", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].text").value("Comment 1"))
                .andExpect(jsonPath("$[1].text").value("Comment 2"));

        verify(commentService, times(1)).getAllCommentsByPostId(anyLong());
    }

    @Test
    void getCommentsByPostNotFound() throws Exception {
        given(commentService.getAllCommentsByPostId(anyLong())).willReturn(List.of());

        mockMvc.perform(get("/api/v3/comment/{postId}/comments", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateComment() throws Exception {
        CommentDTO updatedCommentDTO = new CommentDTO();
        updatedCommentDTO.setText("Updated comment");

        given(commentService.updateComment(anyLong(), any(CommentDTO.class))).willReturn(updatedCommentDTO);
        String reqBody = new ObjectMapper().writeValueAsString(updatedCommentDTO);

        mockMvc.perform(patch("/api/v3/comment/{commentId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqBody))
                .andExpect(status().isOk())
                .andExpect(content().json(reqBody));
    }
}