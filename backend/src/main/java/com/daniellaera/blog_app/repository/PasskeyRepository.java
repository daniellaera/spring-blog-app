package com.daniellaera.blog_app.repository;

import com.daniellaera.blog_app.model.Passkey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasskeyRepository extends JpaRepository<Passkey, UUID> {
    List<Passkey> findAllByUserHandle(byte[] userHandle);

    List<Passkey> findAllByKeyId(byte[] keyId);

    Optional<Passkey> findByUserHandleAndKeyId(byte[] userHandle, byte[] keyId);

    List<Passkey> findByUserId(UUID userId);

    List<Passkey> findByUserHandleAndLastUsedAtNotNullAndLastUsedAtBefore(byte[] userHandle, Instant cutoff);
}
