package com.daniellaera.blog_app.dto;

import com.yubico.webauthn.data.ByteArray;

import java.util.List;

public record VerifyClientRequestResource(
        ByteArray attestationObject,
        ByteArray clientDataJSON,
        List<String> transports
) {
}
