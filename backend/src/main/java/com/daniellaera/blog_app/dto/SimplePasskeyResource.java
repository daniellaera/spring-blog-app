package com.daniellaera.blog_app.dto;

import java.util.List;

public record SimplePasskeyResource(
        String type,
        List<String> transports,
        long signatureCount,
        byte[] keyId,
        byte[] publicKey,
        String clientDataJSON,
        String attestationObject,
        String origin,
        boolean userPresence,
        boolean userVerification,
        boolean residentKey,
        String authenticatorAttachment
) {}

