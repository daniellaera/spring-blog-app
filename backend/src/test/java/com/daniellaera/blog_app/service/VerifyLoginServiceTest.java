package com.daniellaera.blog_app.service;

import com.daniellaera.blog_app.dto.*;
import com.daniellaera.blog_app.exception.PasskeyException;
import com.daniellaera.blog_app.model.Passkey;
import com.daniellaera.blog_app.model.User;
import com.daniellaera.blog_app.repository.PasskeyRepository;
import com.daniellaera.blog_app.repository.UserRepository;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import com.yubico.webauthn.exception.AssertionFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VerifyLoginServiceTest {

    @Mock RelyingParty relyingParty;
    @Mock UserRepository userRepository;
    @Mock PasskeyRepository passkeyRepository;

    @InjectMocks VerifyLoginService service;

    private User testUser;
    private VerifyLoginRequestResource validRequest;
    private AssertionResult mockResult;
    private final ByteArray credentialId = new ByteArray(new byte[]{1, 2, 3, 4, 5});

    @BeforeEach
    void setUp() throws Exception {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setUserHandle(new byte[]{10, 20, 30});
        testUser.setRegistrationComplete(true);
        testUser.setAssertion(buildAssertionJson());

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // authenticatorData must be ≥37 bytes to satisfy AuthenticatorData parser
        ByteArray authData = new ByteArray(new byte[37]);
        ByteArray clientDataJSON = buildClientDataJSON("webauthn.get");
        ByteArray signature = new ByteArray(new byte[32]);
        AssertionResource assertionResp = new AssertionResource(authData, clientDataJSON, signature);
        AssertionRequestResource assertionReq = new AssertionRequestResource(
                credentialId, credentialId, assertionResp, "public-key", null, "platform");
        validRequest = new VerifyLoginRequestResource("testuser", assertionReq);

        mockResult = mock(AssertionResult.class);
        when(mockResult.isSuccess()).thenReturn(true);
        when(mockResult.isSignatureCounterValid()).thenReturn(true);
        when(mockResult.getSignatureCount()).thenReturn(1L);
        when(mockResult.getCredentialId()).thenReturn(credentialId);
    }

    @Test
    void userNotFound_throwsRuntimeException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verify(validRequest))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void credentialNotInDb_returns404WithPasskeyNotFoundCode() {
        when(passkeyRepository.findByUserHandleAndKeyId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verify(validRequest))
                .isInstanceOf(PasskeyException.class)
                .satisfies(ex -> {
                    PasskeyException pe = (PasskeyException) ex;
                    assertThat(pe.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(pe.getCode()).isEqualTo("PASSKEY_NOT_FOUND_IN_DB");
                });
    }

    @Test
    void finishAssertionThrows_returns401WithSignatureInvalidCode() throws Exception {
        Passkey storedPasskey = buildStoredPasskey();
        when(passkeyRepository.findByUserHandleAndKeyId(any(), any())).thenReturn(Optional.of(storedPasskey));
        when(relyingParty.finishAssertion(any()))
                .thenThrow(new AssertionFailedException("bad signature"));

        assertThatThrownBy(() -> service.verify(validRequest))
                .isInstanceOf(PasskeyException.class)
                .satisfies(ex -> {
                    PasskeyException pe = (PasskeyException) ex;
                    assertThat(pe.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(pe.getCode()).isEqualTo("PASSKEY_SIGNATURE_INVALID");
                });
    }

    @Test
    void isSuccessFalse_returns401() throws Exception {
        Passkey storedPasskey = buildStoredPasskey();
        when(passkeyRepository.findByUserHandleAndKeyId(any(), any())).thenReturn(Optional.of(storedPasskey));
        when(mockResult.isSuccess()).thenReturn(false);
        when(relyingParty.finishAssertion(any())).thenReturn(mockResult);

        assertThatThrownBy(() -> service.verify(validRequest))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void invalidSignatureCounter_returns401() throws Exception {
        Passkey storedPasskey = buildStoredPasskey();
        when(passkeyRepository.findByUserHandleAndKeyId(any(), any())).thenReturn(Optional.of(storedPasskey));
        when(mockResult.isSignatureCounterValid()).thenReturn(false);
        when(relyingParty.finishAssertion(any())).thenReturn(mockResult);

        assertThatThrownBy(() -> service.verify(validRequest))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void happyPath_signCountAndLastUsedAtUpdated() throws Exception {
        Passkey storedPasskey = buildStoredPasskey();
        when(passkeyRepository.findByUserHandleAndKeyId(any(), any())).thenReturn(Optional.of(storedPasskey));
        when(relyingParty.finishAssertion(any())).thenReturn(mockResult);

        var response = service.verify(validRequest);

        assertThat(response.verified()).isTrue();
        assertThat(storedPasskey.getSignatureCount()).isEqualTo(1L);
        assertThat(storedPasskey.getLastUsedAt()).isNotNull();
        verify(passkeyRepository).save(storedPasskey);
    }

    private Passkey buildStoredPasskey() {
        Passkey pk = new Passkey();
        pk.setSignatureCount(0L);
        pk.setUserHandle(testUser.getUserHandle());
        pk.setKeyId(credentialId.getBytes());
        return pk;
    }

    private static ByteArray buildClientDataJSON(String type) {
        String json = "{\"type\":\"" + type + "\","
                + "\"challenge\":\"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\","
                + "\"origin\":\"http://localhost:4200\"}";
        return new ByteArray(json.getBytes(StandardCharsets.UTF_8));
    }

    private static String buildAssertionJson() throws Exception {
        PublicKeyCredentialRequestOptions options = PublicKeyCredentialRequestOptions
                .builder()
                .challenge(new ByteArray(new byte[32]))
                .build();
        return AssertionRequest.builder()
                .publicKeyCredentialRequestOptions(options)
                .username("testuser")
                .build()
                .toJson();
    }
}
