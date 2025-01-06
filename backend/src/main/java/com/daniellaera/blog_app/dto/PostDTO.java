package com.daniellaera.blog_app.dto;

import java.util.List;

public record PostDTO(Long id, String title, String content, String author, List<CommentDTO> comments) {
}
