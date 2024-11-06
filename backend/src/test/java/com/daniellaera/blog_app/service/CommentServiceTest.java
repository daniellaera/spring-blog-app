package com.daniellaera.blog_app.service;

import com.daniellaera.blog_app.dto.CommentDTO;
import com.daniellaera.blog_app.model.Comment;
import com.daniellaera.blog_app.model.Post;
import com.daniellaera.blog_app.repository.CommentRepository;
import com.daniellaera.blog_app.repository.PostRepository;
import com.daniellaera.blog_app.service.impl.CommentServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @InjectMocks
    private CommentServiceImpl commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Test
    void getAllComments() {
        Comment comment = new Comment();
        comment.setText("test comment");

        Comment comment2 = new Comment();
        comment2.setText("test comment2");

        List<Comment> comments = Arrays.asList(comment, comment2);

        given(commentRepository.findAllByPostId(anyLong())).willReturn(comments);

        List<CommentDTO> commentDTOList = commentService.getAllCommentsByPostId(anyLong());

        assertNotNull(commentDTOList);
    }

    @Test
    void addComment() {
        Post post1 = new Post();
        post1.setTitle("title");
        post1.setContent("content");

        Comment comment = new Comment();
        comment.setText("test comment");

        CommentDTO commentDTO = convertToDTO(comment);

        given(postRepository.findById(anyLong())).willReturn(Optional.of(post1));

        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        CommentDTO saved = commentService.addComment(anyLong(), commentDTO);

        assertNotNull(saved);

    }

    @Test
    void addComment_postEmptyException() {
        // Arrange
        Comment comment = new Comment();
        comment.setText("test comment");

        CommentDTO commentDTO = convertToDTO(comment);

        // Mock postRepository to return an empty Optional
        given(postRepository.findById(anyLong())).willReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> commentService.addComment(anyLong(), commentDTO)
        );

        // Verify the exception message
        assertEquals("Post not found", exception.getMessage());

        // Ensure that commentRepository.save was not called
        verify(commentRepository, never()).save(any(Comment.class));
    }


    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setText(comment.getText());
        return commentDTO;
    }

}
