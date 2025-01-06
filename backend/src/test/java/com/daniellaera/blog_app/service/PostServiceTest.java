package com.daniellaera.blog_app.service;

import com.daniellaera.blog_app.dto.PostDTO;
import com.daniellaera.blog_app.model.Post;
import com.daniellaera.blog_app.repository.PostRepository;
import com.daniellaera.blog_app.service.impl.PostServiceImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostServiceImpl postService;

    @Mock
    private PostRepository postRepository;

    @Test
    void getAllPosts() {
        Post post1 = new Post();
        post1.setTitle("title");
        post1.setContent("content");

        Post post2 = new Post();
        post2.setTitle("title2");
        post2.setContent("content2");

        List<Post> posts = Arrays.asList(post1, post2);

        given(postRepository.findAll()).willReturn(posts);

        List<PostDTO> postDTOList = postService.getAllPosts();

        assertNotNull(postDTOList);
        assertEquals(2, postDTOList.size());
        assertEquals(post1.getTitle(), postDTOList.getFirst().title());
        assertEquals(post1.getContent(), postDTOList.getFirst().content());

        verify(postRepository, times(1)).findAll();
    }

    @Test
    void createPost() {
        Post post = new Post();
        post.setTitle("title");
        post.setContent("content");

        PostDTO postDTO = convertToDto(post);

        given(postRepository.save(any(Post.class))).willReturn(post);

        PostDTO saved = postService.createPost(postDTO);

        assertNotNull(saved, "Saved post should not be null");
        assertEquals(saved.title(), postDTO.title(), "Title should be the same");
        assertEquals(saved.content(), postDTO.content(), "Content should be the same");
        assertTrue(saved.comments().isEmpty());

        verify(postRepository).save(any(Post.class));
    }

    @Test
    void getPostById() {
        Post post = new Post();
        post.setTitle("title");
        post.setContent("content");

        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Optional<PostDTO> postDTO = postService.getPostById(1L);
        assertTrue(postDTO.isPresent(), "Post should be present");
        assertEquals("title", postDTO.get().title(), "Title should match");
        assertEquals("content", postDTO.get().content(), "Content should match");
    }

    @Test
    void deletePost() {
        doNothing().when(postRepository).deleteById(anyLong());

        postService.deleteById(anyLong());

        verify(postRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void getPostDTOToString() {
        PostDTO post = new PostDTO(1L, "title", "content", "Jean-Marie", List.of());

        String expected = "PostDTO[id=1, title=title, content=content, author=Jean-Marie, comments=[]]";
        assertEquals(expected, post.toString());
    }

    @Test
    void getPostToString() {
        Post post = new Post();
        post.setId(1L);
        post.setTitle("title");
        post.setContent("content");

        String expected = "Post{id=1, title='title', content='content'}";
        assertEquals(expected, post.toString());
    }

    private PostDTO convertToDto(Post post) {
        return new PostDTO(1L, "title", "content", "Eddy", List.of());
    }
}