package com.daniellaera.blog_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class PasskeyException extends ResponseStatusException {
    private final String code;
    private final String hint;

    public PasskeyException(HttpStatus status, String message, String code) {
        this(status, message, code, null);
    }

    public PasskeyException(HttpStatus status, String message, String code, String hint) {
        super(status, message);
        this.code = code;
        this.hint = hint;
    }

    public String getCode() { return code; }
    public String getHint() { return hint; }
}
