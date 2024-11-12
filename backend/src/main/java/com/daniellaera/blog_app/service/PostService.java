package com.daniellaera.blog_app.service;

import com.daniellaera.blog_app.dto.PostDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface PostService {

    List<PostDTO> getAllPosts();

    Optional<PostDTO> getPostById(Long id);

    PostDTO createPost(PostDTO postDTO);

    void deleteById(Long id);

    PostDTO updatePost(Long postId, PostDTO postDto);
}
