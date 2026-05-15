package com.daniellaera.blog_app.dto;

import com.yubico.webauthn.data.ByteArray;

public record AssertionRequestResource(ByteArray id, ByteArray rawId, AssertionResource response,
                                       String type, Object clientExtensionResults,
                                       String authenticatorAttachment) {
}
