package com.daniellaera.blog_app.repository;

import com.daniellaera.blog_app.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    @Transactional
    Optional<User> findByUsername(String username);

    Optional<User> findByUserHandle(byte[] userHandle);
}
