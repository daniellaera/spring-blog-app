package com.daniellaera.blog_app.service;

import com.daniellaera.blog_app.dto.AllowCredentialsResponseResource;
import com.daniellaera.blog_app.dto.LoginRequestResource;
import com.daniellaera.blog_app.dto.LoginResponseResource;
import com.daniellaera.blog_app.model.User;
import com.daniellaera.blog_app.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.data.AuthenticatorTransport;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.UserVerificationRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class StartLoginService {

    private final RelyingParty relyingParty;
    private final UserRepository userRepository;

    public LoginResponseResource startLogin(LoginRequestResource resource) throws JsonProcessingException {
        User user = getUser(resource.username());

        StartAssertionOptions assertionOptions = createStartAssertionOptions(user.getUsername());
        AssertionRequest assertionRequest = relyingParty.startAssertion(assertionOptions);
        List<AllowCredentialsResponseResource> credentials = getAllowCredentials(assertionRequest);

        user.setAssertion(assertionRequest.toJson());
        userRepository.save(user);

        return new LoginResponseResource(assertionRequest.getPublicKeyCredentialRequestOptions().getChallenge().getBase64Url(),
                credentials, 60000, "preferred", relyingParty.getIdentity().getId());
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

    private StartAssertionOptions createStartAssertionOptions(String username) {
        return StartAssertionOptions.builder()
                .timeout(60000)
                .username(username)
                .userVerification(UserVerificationRequirement.PREFERRED)
                .build();
    }

    private List<AllowCredentialsResponseResource> getAllowCredentials(AssertionRequest assertionRequest) {
        List<AllowCredentialsResponseResource> allowCredentialsList = new ArrayList<>();

        Optional<List<PublicKeyCredentialDescriptor>> keys = assertionRequest.getPublicKeyCredentialRequestOptions().getAllowCredentials();
        if (keys.isPresent()) {
            keys.get().forEach(key -> {
                if (key.getTransports().isPresent()) {
                    log.info("Transports found");
                    Set<AuthenticatorTransport> transports = key.getTransports().get();
                    allowCredentialsList.add(new AllowCredentialsResponseResource(key.getId(), key.getType().getId(), transports));
                } else {
                    log.error("Transports not found");
                }
            });
        }
        return allowCredentialsList;
    }
}
