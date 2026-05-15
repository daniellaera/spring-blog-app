package com.daniellaera.blog_app.service;

import com.daniellaera.blog_app.dto.StartRegisterRequestResource;
import com.daniellaera.blog_app.dto.StartRegisterResponseResource;
import com.daniellaera.blog_app.model.User;
import com.daniellaera.blog_app.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;

import static com.daniellaera.blog_app.utils.ByteArrayUtils.bytesToByteArray;

@RequiredArgsConstructor
@Service
@Slf4j
public class StartRegistrationService {

    private final UserRepository userRepository;
    private final AuthenticatorSelectionCriteria authenticatorSelection;
    private final RelyingPartyIdentity relyingPartyIdentity;
    private final RelyingParty relyingParty;
    private final List<PublicKeyCredentialParameters> publicKeyCredentialParameters;
    private final SecureRandom random = new SecureRandom();

    public StartRegisterResponseResource startRegistration(StartRegisterRequestResource resource) throws JsonProcessingException {

        if (userRepository.findByUsername(resource.username()).isPresent()) {
            log.error("User with username '{}' already exists", resource.username());
            throw new IllegalArgumentException("User with username '" + resource.username() + "' already exists");
        }

        byte[] userHandle = new byte[36];
        random.nextBytes(userHandle);

        UserIdentity userIdentity = createUserIdentity(resource.username(), bytesToByteArray(userHandle));
        StartRegistrationOptions startRegistrationOptions = createStartRegistrationOptions(userIdentity);
        PublicKeyCredentialCreationOptions pbOptions = relyingParty.startRegistration(startRegistrationOptions);

        User createdUser = createUser(resource.username(), pbOptions.toJson(), userHandle);
        userRepository.save(createdUser);

        return new StartRegisterResponseResource(pbOptions.getChallenge().getBase64Url(), relyingPartyIdentity,
                userIdentity, publicKeyCredentialParameters, 60000, "none", Collections.emptyList(), authenticatorSelection);
    }

    private UserIdentity createUserIdentity(String username, ByteArray userHandle) {
        return UserIdentity.builder()
                .name(username)
                .displayName(username)
                .id(userHandle)
                .build();
    }

    private StartRegistrationOptions createStartRegistrationOptions(UserIdentity userIdentity) {
        return StartRegistrationOptions.builder()
                .user(userIdentity)
                .timeout(60000)
                .authenticatorSelection(authenticatorSelection)
                .build();
    }

    private User createUser(String username, String publicKey, byte[] userHandle) {
        User user = new User();
        user.setUsername(username);
        user.setPublicKeyJson(publicKey);
        user.setUserHandle(userHandle);
        user.setRegistrationComplete(false);
        return user;
    }
}