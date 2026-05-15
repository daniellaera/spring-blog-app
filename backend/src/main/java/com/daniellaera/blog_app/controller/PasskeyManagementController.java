package com.daniellaera.blog_app.controller;

import com.daniellaera.blog_app.dto.PasskeyListItemResource;
import com.daniellaera.blog_app.dto.PasskeyRenameRequest;
import com.daniellaera.blog_app.exception.PasskeyException;
import com.daniellaera.blog_app.model.Passkey;
import com.daniellaera.blog_app.model.User;
import com.daniellaera.blog_app.repository.PasskeyRepository;
import com.daniellaera.blog_app.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user/passkeys")
@CrossOrigin(value = {"http://localhost:4200", "https://blog-app-frontend.fly.dev"}, allowCredentials = "true")
@RequiredArgsConstructor
public class PasskeyManagementController {

    private final UserRepository userRepository;
    private final PasskeyRepository passkeyRepository;

    @GetMapping
    public ResponseEntity<List<PasskeyListItemResource>> listPasskeys(HttpSession session) {
        User user = resolveUser(session);
        List<PasskeyListItemResource> items = passkeyRepository
                .findAllByUserHandle(user.getUserHandle())
                .stream()
                .map(this::toResource)
                .toList();
        return ResponseEntity.ok(items);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePasskey(@PathVariable UUID id, HttpSession session) {
        User user = resolveUser(session);
        Passkey passkey = passkeyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Passkey not found"));

        if (!Arrays.equals(passkey.getUserHandle(), user.getUserHandle())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        long passkeyCount = passkeyRepository.findAllByUserHandle(user.getUserHandle()).size();
        if (passkeyCount <= 1) {
            throw new PasskeyException(HttpStatus.BAD_REQUEST,
                    "Cannot remove your only passkey. Register a new one first.",
                    "LAST_PASSKEY");
        }

        passkeyRepository.delete(passkey);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PasskeyListItemResource> renamePasskey(
            @PathVariable UUID id,
            @RequestBody PasskeyRenameRequest request,
            HttpSession session) {
        User user = resolveUser(session);
        Passkey passkey = passkeyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Passkey not found"));

        if (!Arrays.equals(passkey.getUserHandle(), user.getUserHandle())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        passkey.setDeviceName(request.deviceName());
        passkeyRepository.save(passkey);
        return ResponseEntity.ok(toResource(passkey));
    }

    @DeleteMapping("/orphaned")
    public ResponseEntity<Map<String, Integer>> deleteOrphanedPasskeys(HttpSession session) {
        User user = resolveUser(session);
        Instant cutoff = Instant.now().minus(90, ChronoUnit.DAYS);
        List<Passkey> orphaned = passkeyRepository
                .findByUserHandleAndLastUsedAtNotNullAndLastUsedAtBefore(user.getUserHandle(), cutoff);
        passkeyRepository.deleteAll(orphaned);
        return ResponseEntity.ok(Map.of("removed", orphaned.size()));
    }

    private User resolveUser(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private PasskeyListItemResource toResource(Passkey pk) {
        List<String> transports = pk.getTransport() == null || pk.getTransport().isBlank()
                ? List.of()
                : Arrays.asList(pk.getTransport().split(","));
        return new PasskeyListItemResource(
                pk.getId(),
                pk.getDeviceName(),
                pk.getCreatedAt(),
                pk.getLastUsedAt(),
                pk.isBackedUp(),
                transports
        );
    }
}
