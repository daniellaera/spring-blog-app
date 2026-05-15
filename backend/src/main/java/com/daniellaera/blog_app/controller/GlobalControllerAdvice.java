package com.daniellaera.blog_app.controller;

import com.daniellaera.blog_app.exception.PasskeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@ControllerAdvice
public class GlobalControllerAdvice {

    record ErrorResponse(String error, String code, String timestamp, String hint) {}

    @ExceptionHandler(PasskeyException.class)
    public ResponseEntity<ErrorResponse> handlePasskeyException(PasskeyException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ErrorResponse(ex.getReason(), ex.getCode(), Instant.now().toString(), ex.getHint()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("User already exists", "USER_ALREADY_EXISTS", Instant.now().toString(), null));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        String reason = ex.getReason() != null ? ex.getReason() : "An unexpected error occurred";
        String code = toCode(reason);
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ErrorResponse(reason, code, Instant.now().toString(), null));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        if (message.contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("User not found", "USER_NOT_FOUND", Instant.now().toString(), null));
        }
        if (message.contains("not registered") || message.contains("not complete")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid credentials", "INVALID_CREDENTIALS", Instant.now().toString(), null));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An unexpected error occurred", "INTERNAL_ERROR", Instant.now().toString(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An unexpected error occurred", "INTERNAL_ERROR", Instant.now().toString(), null));
    }

    private static String toCode(String reason) {
        return reason.toUpperCase().replace(' ', '_').replaceAll("[^A-Z_]", "");
    }
}
