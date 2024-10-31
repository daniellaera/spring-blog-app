package com.daniellaera.blog_app.service;

import com.daniellaera.blog_app.dto.CommentDTO;

import java.util.List;

public interface CommentService {

    CommentDTO addComment(Long postId, CommentDTO commentDTO);

    List<CommentDTO> getAllCommentsByPostId(Long postId);

    CommentDTO updateComment(Long commentId, CommentDTO commentDTO);
}
