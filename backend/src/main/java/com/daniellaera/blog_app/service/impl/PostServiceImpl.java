package com.daniellaera.blog_app.service.impl;

import com.daniellaera.blog_app.dto.CommentDTO;
import com.daniellaera.blog_app.dto.PostDTO;
import com.daniellaera.blog_app.model.Comment;
import com.daniellaera.blog_app.model.Post;
import com.daniellaera.blog_app.repository.PostRepository;
import com.daniellaera.blog_app.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    @Autowired
    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public List<PostDTO> getAllPosts() {
        return postRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PostDTO> getPostById(Long id) {
        return postRepository.findById(id).map(this::convertToDTO);
    }

    @Override
    public PostDTO createPost(PostDTO postDTO) {
        Post post = convertToEntity(postDTO);
        Post saved = postRepository.save(post);
        return convertToDTO(saved);
    }

    @Override
    public void deleteById(Long id) {
        postRepository.deleteById(id);
    }

    private PostDTO convertToDTO(Post post) {
        PostDTO postDTO = new PostDTO();
        postDTO.setContent(post.getContent());
        postDTO.setTitle(post.getTitle());

        List<CommentDTO> commentDTOList =
                (post.getComments() != null) ? post.getComments()
                .stream()
                .map(this::convertCommentToCommentDto)
                .toList() : List.of();
        postDTO.setComments(commentDTOList);
        return postDTO;
    }

    private CommentDTO convertCommentToCommentDto(Comment comment) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setText(comment.getText());
        return commentDTO;
    }

    private Comment convertCommentDtoToCommentEntity(CommentDTO commentDTO) {
        Comment comment = new Comment();
        comment.setText(commentDTO.getText());
        return comment;
    }

    private Post convertToEntity(PostDTO postDTO) {
        Post post = new Post();

        List<Comment> comments = Optional.ofNullable(postDTO.getComments())
                .orElse(List.of())
                .stream()
                .map(commentDTO -> {
                    Comment comment = convertCommentDtoToCommentEntity(commentDTO);
                    comment.setPost(post);
                    return comment;
                })
                .toList();
        post.setComments(comments);

        post.setContent(postDTO.getContent());
        post.setTitle(postDTO.getTitle());
        return post;
    }
}
