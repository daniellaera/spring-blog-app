package com.daniellaera.blog_app.repository;

import com.daniellaera.blog_app.model.OtpCode;
import com.daniellaera.blog_app.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {

    Optional<OtpCode> findByUserAndCodeAndUsedFalseAndExpiresAtAfter(User user, String code, Instant now);

    @Transactional
    void deleteByUserAndUsedTrue(User user);

    @Transactional
    void deleteByExpiresAtBefore(Instant now);
}
