package com.daniellaera.blog_app.service.impl;

import com.daniellaera.blog_app.dto.CommentDTO;
import com.daniellaera.blog_app.model.Comment;
import com.daniellaera.blog_app.model.Post;
import com.daniellaera.blog_app.repository.CommentRepository;
import com.daniellaera.blog_app.repository.PostRepository;
import com.daniellaera.blog_app.service.CommentService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    @Override
    public CommentDTO addComment(Long postId, CommentDTO commentDTO) {
        Optional<Post> post = postRepository.findById(postId);
        if (post.isEmpty()) {
            throw new EntityNotFoundException("Post not found");
        }
        Post postEntity = post.get();

        Comment comment = convertToEntity(commentDTO);
        comment.setPost(postEntity);

        Comment saved = commentRepository.save(comment);
        return convertToDto(saved);
    }

    @Override
    public List<CommentDTO> getAllCommentsByPostId(Long postId) {
        //Optional<Post> post = postRepository.findById(postId);
        //if (post.isEmpty()) {
        //    throw new RuntimeException("Post not found");
        //}
        //List<Comment> comments = post.get().getComments();
        List<Comment> comments = commentRepository.findAllByPostId(postId);

        return comments
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDTO updateComment(Long commentId, CommentDTO commentDTO) {
        Optional<Comment> comment = commentRepository.findById(commentId);
        if (comment.isEmpty()) {
            throw new RuntimeException("Comment not found");
        }
        Comment entity = comment.get();
        entity.setText(commentDTO.getText());

        Comment saved = commentRepository.save(entity);
        return convertToDto(saved);
    }

    private Comment convertToEntity(CommentDTO commentDTO) {
        Comment comment = new Comment();
        comment.setText(commentDTO.getText());
        return comment;
    }

    private CommentDTO convertToDto(Comment comment) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setText(comment.getText());
        return commentDTO;
    }
}
