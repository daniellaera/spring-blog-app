package com.daniellaera.blog_app.controller;

import com.daniellaera.blog_app.dto.StartRegisterRequestResource;
import com.daniellaera.blog_app.dto.StartRegisterResponseResource;
import com.daniellaera.blog_app.dto.VerifyRegistrationRequestResource;
import com.daniellaera.blog_app.dto.VerifyRegistrationResponseResource;
import com.daniellaera.blog_app.service.StartRegistrationService;
import com.daniellaera.blog_app.service.VerifyRegistrationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.data.exception.Base64UrlException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/register")
@CrossOrigin("http://localhost:4200")
public class RegistrationController {

    private final StartRegistrationService startRegistrationService;
    private final VerifyRegistrationService verifyRegistrationService;

    @PostMapping("/start")
    public ResponseEntity<StartRegisterResponseResource> startRegistration(@RequestBody StartRegisterRequestResource resource) throws JsonProcessingException {
        return ResponseEntity.ok(startRegistrationService.startRegistration(resource));
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyRegistrationResponseResource> verifyRegistration(@RequestBody VerifyRegistrationRequestResource resource) throws Base64UrlException, IOException {
        return ResponseEntity.ok(verifyRegistrationService.verify(resource));
    }
}
