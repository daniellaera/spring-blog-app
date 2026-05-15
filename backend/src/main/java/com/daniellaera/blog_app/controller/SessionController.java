package com.daniellaera.blog_app.controller;

import com.daniellaera.blog_app.dto.SimplePasskeyResource;
import com.daniellaera.blog_app.dto.UserSessionResource;
import com.daniellaera.blog_app.repository.PasskeyRepository;
import com.daniellaera.blog_app.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/session")
@CrossOrigin(value = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
public class SessionController {

    private final UserRepository userRepository;
    private final PasskeyRepository passkeyRepository;

    @GetMapping("/me")
    public ResponseEntity<UserSessionResource> getSession(HttpSession session) {

        String username = (String) session.getAttribute("username");

        if (username == null) {
            return ResponseEntity.status(401).build(); // not logged in
        }

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var passkeys = passkeyRepository.findAllByUserHandle(user.getUserHandle());

        var passkeyResources = passkeys.stream()
                .map(pk -> new SimplePasskeyResource(
                        pk.getType(),
                        Arrays.asList(pk.getTransport().split(",")),
                        pk.getSignatureCount(),
                        pk.getKeyId(),
                        pk.getPublicKey(),
                        pk.getClientDataJSON(),
                        pk.getAttestationObject(),
                        pk.getOrigin(),
                        pk.isUserPresence(),
                        pk.isUserVerification(),
                        pk.isResidentKey(),
                        pk.getAuthenticatorAttachment()
                ))
                .toList();

        var sessionResource = new UserSessionResource(
                user.getUsername(),
                null, // you can add lastLogin later
                passkeyResources
        );

        return ResponseEntity.ok(sessionResource);
    }
}