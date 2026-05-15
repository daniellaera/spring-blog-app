package com.daniellaera.blog_app.service;

import com.daniellaera.blog_app.dto.VerifyClientRequestResource;
import com.daniellaera.blog_app.dto.VerifyRegistrationRequestResource;
import com.daniellaera.blog_app.dto.VerifyRegistrationResponseResource;
import com.daniellaera.blog_app.model.Passkey;
import com.daniellaera.blog_app.model.User;
import com.daniellaera.blog_app.repository.PasskeyRepository;
import com.daniellaera.blog_app.repository.UserRepository;
import com.daniellaera.blog_app.utils.CustomClientExtensionOutput;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.data.exception.Base64UrlException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;

import static com.daniellaera.blog_app.utils.ByteArrayUtils.byteArrayToBytes;

@Slf4j
@RequiredArgsConstructor
@Service
public class VerifyRegistrationService {

    private final RelyingParty relyingParty;
    private final UserRepository userRepository;
    private final PasskeyRepository passkeyRepository;

    @Transactional
    public VerifyRegistrationResponseResource verify(VerifyRegistrationRequestResource resource) throws Base64UrlException, IOException {
        Optional<User> user = userRepository.findByUsername(resource.username());

        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        // 1. Extract raw client response for mapping later
        var clientResp = resource.response().response();

        AuthenticatorAttestationResponse authenticatorAttestationResponse
                = createAuthenticatorAttestationResponse(clientResp);

        PublicKeyCredentialCreationOptions publicKeyCredentials = createPublicKeyCredentialCreationOptions(user.get().getPublicKeyJson());
        PublicKeyCredential publicKeyCredential = createPublicKeyCredential(resource.response().id(), authenticatorAttestationResponse);

        FinishRegistrationOptions finishRegistrationOptions = createFinishRegistrationOptions(publicKeyCredentials, publicKeyCredential);

        RegistrationResult registrationResult;
        try {
            registrationResult = relyingParty.finishRegistration(finishRegistrationOptions);
        } catch (RegistrationFailedException e) {
            user.get().setPublicKeyJson(null);
            user.get().setRegistrationComplete(false);
            userRepository.save(user.get());
            return new VerifyRegistrationResponseResource(false);
        }

        user.get().setPublicKeyJson(null);
        user.get().setRegistrationComplete(true);
        userRepository.save(user.get());

        // 2. Map existing fields
        byte[] publicKey = byteArrayToBytes(registrationResult.getPublicKeyCose());
        byte[] keyId = byteArrayToBytes(registrationResult.getKeyId().getId());

        // Reject 409 if credential ID already exists (prevents duplicate registration)
        if (!passkeyRepository.findAllByKeyId(keyId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Credential ID already registered");
        }
        String type = registrationResult.getKeyId().getType().getId();
        long signatureCount = registrationResult.getSignatureCount();

        String transport = "";
        Optional<SortedSet<AuthenticatorTransport>> transports = registrationResult.getKeyId().getTransports();
        if (transports.isPresent()) {
            List<String> transportList = transports.get().stream().map(AuthenticatorTransport::getId).toList();
            transport = String.join(",", transportList);
        }

        // 3. Map the NEW fields
        Passkey passkey = new Passkey();
        passkey.setUserHandle(user.get().getUserHandle());
        passkey.setPublicKey(publicKey);
        passkey.setKeyId(keyId);
        passkey.setType(type);
        passkey.setTransport(transport);
        passkey.setSignatureCount(signatureCount);

        // Raw Strings from the client
        passkey.setClientDataJSON(clientResp.clientDataJSON().getBase64Url());
        passkey.setAttestationObject(clientResp.attestationObject().getBase64Url());

        // Origin from RP identity
        passkey.setOrigin(relyingParty.getIdentity().getId());

        // Flags from the registration result
        passkey.setUserVerification(registrationResult.isUserVerified());
        // UP is always validated by finishRegistration — if we reach here it was set
        passkey.setUserPresence(true);
        passkey.setResidentKey(passkey.getUserHandle() != null);
        // backedUp (BE flag): credential is synced to cloud/password manager
        passkey.setBackedUp(registrationResult.isBackedUp());

        passkey.setAuthenticatorAttachment(resource.response().authenticatorAttachment());

        // AAGUID identifies the authenticator model (may be all-zeros for privacy-preserving authenticators)
        passkey.setAaguid(registrationResult.getAaguid().getHex());

        // Default device name — can be renamed later via PATCH /api/user/passkeys/{id}
        passkey.setDeviceName("New passkey");

        // FK to owning user
        passkey.setUserId(user.get().getId());

        log.info("Saving passkey for user {} with origin {}", resource.username(), passkey.getOrigin());
        passkeyRepository.save(passkey);

        return new VerifyRegistrationResponseResource(true);
    }

    private AuthenticatorAttestationResponse createAuthenticatorAttestationResponse(VerifyClientRequestResource client) throws Base64UrlException, IOException {
        Set<AuthenticatorTransport> transports = new HashSet<>();
        client.transports().forEach(transport -> {
            transports.add(AuthenticatorTransport.of(transport));
        });

        return AuthenticatorAttestationResponse.builder()
                .attestationObject(client.attestationObject())
                .clientDataJSON(client.clientDataJSON())
                .transports(transports)
                .build();
    }

    private PublicKeyCredentialCreationOptions createPublicKeyCredentialCreationOptions(String json) throws JsonProcessingException {
        return PublicKeyCredentialCreationOptions.fromJson(json);
    }

    private PublicKeyCredential createPublicKeyCredential(ByteArray id, AuthenticatorAttestationResponse authenticator) {
        CustomClientExtensionOutput extensionOutput = new CustomClientExtensionOutput();

        return PublicKeyCredential.builder()
                .id(id)
                .response(authenticator)
                .clientExtensionResults(extensionOutput)
                .build();
    }

    private FinishRegistrationOptions createFinishRegistrationOptions(PublicKeyCredentialCreationOptions publicKey, PublicKeyCredential credential) {
        return FinishRegistrationOptions.builder()
                .request(publicKey)
                .response(credential)
                .build();
    }
}
