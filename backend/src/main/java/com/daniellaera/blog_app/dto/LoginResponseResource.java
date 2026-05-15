package com.daniellaera.blog_app.dto;

import java.util.List;

public record LoginResponseResource(String challenge, List<AllowCredentialsResponseResource> allowCredentials,
                                    int timeout, String userVerification, String rpId) {
}
