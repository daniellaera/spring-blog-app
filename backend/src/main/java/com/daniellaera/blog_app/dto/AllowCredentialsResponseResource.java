package com.daniellaera.blog_app.dto;

import com.yubico.webauthn.data.AuthenticatorTransport;
import com.yubico.webauthn.data.ByteArray;

import java.util.Set;

public record AllowCredentialsResponseResource(ByteArray id, String type,
                                               Set<AuthenticatorTransport> transports) {
}
