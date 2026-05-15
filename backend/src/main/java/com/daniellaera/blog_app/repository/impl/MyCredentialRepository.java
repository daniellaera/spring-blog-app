package com.daniellaera.blog_app.repository.impl;

import com.daniellaera.blog_app.model.Passkey;
import com.daniellaera.blog_app.model.User;
import com.daniellaera.blog_app.repository.PasskeyRepository;
import com.daniellaera.blog_app.repository.UserRepository;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.AuthenticatorTransport;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.daniellaera.blog_app.utils.ByteArrayUtils.byteArrayToBytes;
import static com.daniellaera.blog_app.utils.ByteArrayUtils.bytesToByteArray;

@Slf4j
@RequiredArgsConstructor
public class MyCredentialRepository implements CredentialRepository {

    private final UserRepository userRepository;
    private final PasskeyRepository passkeyRepository;

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        log.info("Get credentials id's for {}", username);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            log.info("Username {} found", username);
            Set<PublicKeyCredentialDescriptor> descriptors = new HashSet<>();
            passkeyRepository.findAllByUserHandle(user.get().getUserHandle())
                    .forEach(descriptor -> {
                        log.info("Found credential for {}", username);
                        descriptors.add(PublicKeyCredentialDescriptor.builder()
                                .id(bytesToByteArray(descriptor.getKeyId()))
                                .transports(getTransports(descriptor.getTransport()))
                                .build());
                    });
            return descriptors;
        }
        return Collections.emptySet();
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        log.info("Get user handle for {}", username);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            log.info("User handle found for {}", username);
            return Optional.of(bytesToByteArray(user.get().getUserHandle()));
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        log.info("Get username for user handle");
        Optional<User> user = userRepository.findByUserHandle(byteArrayToBytes(userHandle));
        if (user.isPresent()) {
            log.info("Username: {} found for user handle", user.get().getUsername());
            return Optional.of(user.get().getUsername());
        }
        return Optional.empty();

        /*
        more refactored
        Optional<User> user = userRepository.findByUserHandle(byteArrayToBytes(userHandle));
        user.ifPresent(u -> log.info("Username: {} found for user handle", u.getUsername()));
        return user.map(User::getUsername);
         */
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        log.info("Get key for credential id and user handle");
        Optional<Passkey> key = passkeyRepository
                .findByUserHandleAndKeyId(byteArrayToBytes(userHandle), byteArrayToBytes(credentialId));
        if (key.isPresent()) {
            log.info("Key found for credential id and user handle");
            RegisteredCredential db = RegisteredCredential.builder()
                    .credentialId(bytesToByteArray(key.get().getKeyId()))
                    .userHandle(bytesToByteArray(key.get().getUserHandle()))
                    .publicKeyCose(bytesToByteArray(key.get().getPublicKey()))
                    .signatureCount(key.get().getSignatureCount())
                    .build();
            return Optional.of(db);
        }
        return Optional.empty();
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        log.info("Get keys for credential id");
        List<Passkey> passkeys = passkeyRepository.findAllByKeyId(byteArrayToBytes(credentialId));
        if (passkeys.isEmpty()) {
            log.info("No keys found for credential id");
        } else {
            log.info("Keys found for credential id");
        }

        Set<RegisteredCredential> registeredCredentials = new HashSet<>();
        passkeys.forEach(passkey -> {
            RegisteredCredential db = RegisteredCredential.builder()
                    .credentialId(bytesToByteArray(passkey.getKeyId()))
                    .userHandle(bytesToByteArray(passkey.getUserHandle()))
                    .publicKeyCose(bytesToByteArray(passkey.getPublicKey()))
                    .signatureCount(passkey.getSignatureCount())
                    .build();
            registeredCredentials.add(db);
        });
        return registeredCredentials;
    }

    private Set<AuthenticatorTransport> getTransports(String transport) {
        Set<AuthenticatorTransport> transports = new HashSet<>();
        String[] transportAsArray = transport.split(",");
        Arrays.stream(transportAsArray).toList().forEach(t -> {
            transports.add(AuthenticatorTransport.of(t));
        });
        return transports;
    }
}
