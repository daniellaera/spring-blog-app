package com.daniellaera.blog_app.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PasskeyListItemResource(
        UUID id,
        String deviceName,
        Instant createdAt,
        Instant lastUsedAt,
        boolean backedUp,
        List<String> transports
) {}
