package com.daniellaera.blog_app.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UserSessionResource(
        String username,
        LocalDateTime lastLogin,
        List<SimplePasskeyResource> passkeys
) {}
