package com.daniellaera.blog_app.config;

import com.daniellaera.blog_app.repository.PasskeyRepository;
import com.daniellaera.blog_app.repository.UserRepository;
import com.daniellaera.blog_app.repository.impl.MyCredentialRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Configuration
public class ServerConfiguration {

    private final UserRepository userRepository;
    private final PasskeyRepository passkeyRepository;

    @Value("${webauthn.rp-id}")
    private String rpId;

    @Value("${webauthn.origin}")
    private String origin;

    @Bean
    public RelyingPartyIdentity relyingPartyIdentity() {
        return RelyingPartyIdentity.builder()
                .id(rpId)
                .name("WebAuthn - Daniel Laera")
                .build();
    }

    @Bean
    public RelyingParty relyingParty() {
        return RelyingParty.builder()
                .identity(relyingPartyIdentity())
                .credentialRepository(new MyCredentialRepository(userRepository, passkeyRepository))
                .origins(Set.of(origin))
                .allowOriginPort(true)
                .build();
    }

    @Bean
    public AuthenticatorSelectionCriteria authenticatorSelectionCriteria() {
        AuthenticatorAttachment authenticatorAttachment = AuthenticatorAttachment.CROSS_PLATFORM;
        ResidentKeyRequirement residentKeyRequirement = ResidentKeyRequirement.PREFERRED;
        UserVerificationRequirement userVerificationRequirement = UserVerificationRequirement.PREFERRED;

        return AuthenticatorSelectionCriteria.builder()
                .authenticatorAttachment(authenticatorAttachment)
                .residentKey(residentKeyRequirement)
                .userVerification(userVerificationRequirement)
                .build();
    }

    @Bean
    public List<PublicKeyCredentialParameters> publicKeyCredentialParameters() {
        List<PublicKeyCredentialParameters> pubKeyCredParams = new ArrayList<>();

        PublicKeyCredentialParameters param1 = PublicKeyCredentialParameters.ES256;
        PublicKeyCredentialParameters param2 = PublicKeyCredentialParameters.RS256;
        pubKeyCredParams.add(param1);
        pubKeyCredParams.add(param2);
        return pubKeyCredParams;
    }
}
