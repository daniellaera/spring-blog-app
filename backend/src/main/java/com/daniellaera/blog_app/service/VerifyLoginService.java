package com.daniellaera.blog_app.service;

import com.daniellaera.blog_app.dto.VerifyLoginRequestResource;
import com.daniellaera.blog_app.dto.VerifyLoginResponseResource;
import com.daniellaera.blog_app.exception.PasskeyException;
import com.daniellaera.blog_app.model.Passkey;
import com.daniellaera.blog_app.model.User;
import com.daniellaera.blog_app.repository.PasskeyRepository;
import com.daniellaera.blog_app.repository.UserRepository;
import com.daniellaera.blog_app.utils.CustomClientExtensionOutput;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.exception.Base64UrlException;
import com.yubico.webauthn.exception.AssertionFailedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class VerifyLoginService {

    private final RelyingParty relyingParty;
    private final UserRepository userRepository;
    private final PasskeyRepository passkeyRepository;

    @Transactional
    public VerifyLoginResponseResource verify(VerifyLoginRequestResource resource) throws IOException, Base64UrlException {
        User user = getUser(resource.username());

        // Pre-check: ensure the credential presented exists in our DB for this user.
        // If not found, the passkey was deleted from the DB or belongs to a different account.
        byte[] credentialIdBytes = resource.response().id().getBytes();
        Passkey storedPasskey = passkeyRepository
                .findByUserHandleAndKeyId(user.getUserHandle(), credentialIdBytes)
                .orElseThrow(() -> new PasskeyException(
                        HttpStatus.NOT_FOUND,
                        "Passkey not found. It may have been deleted from your device.",
                        "PASSKEY_NOT_FOUND_IN_DB"));

        AssertionRequest assertionRequest = createAssertionRequest(user.getAssertion());
        AuthenticatorAssertionResponse assertionResponse = createAuthenticatorAssertionResponse(resource);
        PublicKeyCredential publicKeyCredential = createPublicKeyCredential(resource, assertionResponse);
        FinishAssertionOptions finishAssertionOptions = createFinishAssertionOptions(assertionRequest, publicKeyCredential);

        AssertionResult result;
        try {
            result = relyingParty.finishAssertion(finishAssertionOptions);
        } catch (AssertionFailedException e) {
            user.setAssertion(null);
            userRepository.save(user);
            throw new PasskeyException(
                    HttpStatus.UNAUTHORIZED,
                    "Passkey verification failed. The key on your device may no longer match.",
                    "PASSKEY_SIGNATURE_INVALID");
        }

        user.setAssertion(null);
        userRepository.save(user);

        if (!result.isSuccess()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication failed");
        }

        // Replay-attack protection: Yubico flags signCount anomalies via isSignatureCounterValid()
        if (!result.isSignatureCounterValid()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Replay attack detected: invalid signature counter");
        }

        // Update signCount and lastUsedAt on successful authentication
        storedPasskey.setSignatureCount(result.getSignatureCount());
        storedPasskey.setLastUsedAt(Instant.now());
        passkeyRepository.save(storedPasskey);

        return new VerifyLoginResponseResource(true);
    }

    private User getUser(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty() || !user.get().isRegistrationComplete()) {
            if (user.isEmpty()) {
                log.error("User with username {} not found", username);
            } else {
                log.error("Registration for username {} is not complete", username);
            }
            throw new RuntimeException(String.format("User with username %s not registered", username));
        }
        return user.get();
    }

    private AssertionRequest createAssertionRequest(String json) throws JsonProcessingException {
        return AssertionRequest.fromJson(json);
    }

    private AuthenticatorAssertionResponse createAuthenticatorAssertionResponse(VerifyLoginRequestResource resource) throws Base64UrlException, IOException {
        return AuthenticatorAssertionResponse.builder()
                .authenticatorData(resource.response().response().authenticatorData())
                .clientDataJSON(resource.response().response().clientDataJSON())
                .signature(resource.response().response().signature())
                .build();
    }

    private PublicKeyCredential createPublicKeyCredential(VerifyLoginRequestResource resource, AuthenticatorAssertionResponse response) {
        CustomClientExtensionOutput customClientExtensionOutput = new CustomClientExtensionOutput();

        return PublicKeyCredential.builder()
                .id(resource.response().id())
                .response(response)
                .clientExtensionResults(customClientExtensionOutput)
                .build();
    }

    private FinishAssertionOptions createFinishAssertionOptions(AssertionRequest assertionRequest, PublicKeyCredential publicKeyCredential) {
        return FinishAssertionOptions.builder()
                .request(assertionRequest)
                .response(publicKeyCredential)
                .build();
    }
}
