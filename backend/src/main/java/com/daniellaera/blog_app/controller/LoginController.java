package com.daniellaera.blog_app.controller;

import com.daniellaera.blog_app.dto.LoginRequestResource;
import com.daniellaera.blog_app.dto.LoginResponseResource;
import com.daniellaera.blog_app.dto.VerifyLoginRequestResource;
import com.daniellaera.blog_app.dto.VerifyLoginResponseResource;
import com.daniellaera.blog_app.service.StartLoginService;
import com.daniellaera.blog_app.service.VerifyLoginService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.data.exception.Base64UrlException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/login")
@CrossOrigin("http://localhost:4200")
public class LoginController {
    private final StartLoginService startLoginService;
    private final VerifyLoginService verifyLoginService;

    @PostMapping("/start")
    public ResponseEntity<LoginResponseResource> startLogin(@RequestBody LoginRequestResource resource) throws JsonProcessingException {
        return ResponseEntity.ok(startLoginService.startLogin(resource));
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyLoginResponseResource> verifyLogin(
            @RequestBody VerifyLoginRequestResource resource,
            HttpSession session
    ) throws Base64UrlException, IOException {

        var result = verifyLoginService.verify(resource);

        if (result.verified()) {
            session.setAttribute("username", resource.username());
        }

        return ResponseEntity.ok(result);
    }

}
