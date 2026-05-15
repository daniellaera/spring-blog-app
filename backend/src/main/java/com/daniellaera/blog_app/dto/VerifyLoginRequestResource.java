package com.daniellaera.blog_app.dto;

public record VerifyLoginRequestResource(String username, AssertionRequestResource response) {
}
