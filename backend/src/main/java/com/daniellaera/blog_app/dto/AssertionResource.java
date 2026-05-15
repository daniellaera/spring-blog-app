package com.daniellaera.blog_app.dto;

import com.yubico.webauthn.data.ByteArray;

public record AssertionResource(ByteArray authenticatorData, ByteArray clientDataJSON, ByteArray signature) {
}
