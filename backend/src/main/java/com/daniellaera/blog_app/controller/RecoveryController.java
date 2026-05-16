package com.daniellaera.blog_app.controller;

import com.daniellaera.blog_app.dto.RecoveryStartRequest;
import com.daniellaera.blog_app.dto.RecoveryStartResponse;
import com.daniellaera.blog_app.dto.RecoveryVerifyRequest;
import com.daniellaera.blog_app.dto.StartRegisterResponseResource;
import com.daniellaera.blog_app.service.RecoveryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recovery")
@RequiredArgsConstructor
@CrossOrigin(value = {"http://localhost:4200",
        "https://blog-app-frontend.fly.dev"}, allowCredentials = "true")
public class RecoveryController {

    private final RecoveryService recoveryService;

    @PostMapping("/start")
    public ResponseEntity<RecoveryStartResponse> start(
            @RequestBody RecoveryStartRequest request) throws JsonProcessingException {
        return ResponseEntity.ok(
                recoveryService.startRecovery(request.username(), request.email()));
    }

    @PostMapping("/verify")
    public ResponseEntity<StartRegisterResponseResource> verify(
            @RequestBody RecoveryVerifyRequest request) throws JsonProcessingException {
        return ResponseEntity.ok(
                recoveryService.verifyAndStartReregistration(
                        request.username(), request.code()));
    }
}
