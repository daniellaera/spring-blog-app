package com.daniellaera.blog_app.dto;

import com.yubico.webauthn.data.ByteArray;

public record StartRegisterCredentialResponseResource(ByteArray id, String type, String[] transports) {
}
