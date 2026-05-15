package com.daniellaera.blog_app.service;

import com.daniellaera.blog_app.dto.*;
import com.daniellaera.blog_app.model.Passkey;
import com.daniellaera.blog_app.model.User;
import com.daniellaera.blog_app.repository.PasskeyRepository;
import com.daniellaera.blog_app.repository.UserRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.RegistrationFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VerifyRegistrationServiceTest {

    @Mock RelyingParty relyingParty;
    @Mock UserRepository userRepository;
    @Mock PasskeyRepository passkeyRepository;

    @InjectMocks VerifyRegistrationService service;

    private User testUser;
    private RegistrationResult mockResult;

    @BeforeEach
    void setUp() throws Exception {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setUserHandle(new byte[]{1, 2, 3, 4});
        testUser.setPublicKeyJson(buildOptionsJson());

        mockResult = mock(RegistrationResult.class);
        ByteArray credId = new ByteArray(new byte[]{10, 20, 30, 40, 50});
        PublicKeyCredentialDescriptor descriptor = PublicKeyCredentialDescriptor.builder().id(credId).build();
        when(mockResult.getKeyId()).thenReturn(descriptor);
        when(mockResult.getPublicKeyCose()).thenReturn(new ByteArray(new byte[]{1, 2, 3}));
        when(mockResult.getSignatureCount()).thenReturn(0L);
        when(mockResult.getAaguid()).thenReturn(new ByteArray(new byte[16]));
        when(mockResult.isBackedUp()).thenReturn(false);
        when(mockResult.isUserVerified()).thenReturn(true);
        when(relyingParty.getIdentity()).thenReturn(
                RelyingPartyIdentity.builder().id("localhost").name("Test").build());
    }

    @Test
    void userNotFound_throwsRuntimeException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verify(buildRequest()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void finishRegistrationFails_returnsNotVerified() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(relyingParty.finishRegistration(any()))
                .thenThrow(new RegistrationFailedException(new IllegalArgumentException("bad attestation")));

        var result = service.verify(buildRequest());

        assertThat(result.verified()).isFalse();
        verify(passkeyRepository, never()).save(any());
    }

    @Test
    void duplicateCredentialId_throws409Conflict() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(relyingParty.finishRegistration(any())).thenReturn(mockResult);
        when(passkeyRepository.findAllByKeyId(any())).thenReturn(List.of(new Passkey()));

        assertThatThrownBy(() -> service.verify(buildRequest()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(passkeyRepository, never()).save(any());
    }

    @Test
    void happyPath_passkeyIsSavedWithAllFields() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(relyingParty.finishRegistration(any())).thenReturn(mockResult);
        when(passkeyRepository.findAllByKeyId(any())).thenReturn(List.of());

        var response = service.verify(buildRequest());

        assertThat(response.verified()).isTrue();

        ArgumentCaptor<Passkey> captor = ArgumentCaptor.forClass(Passkey.class);
        verify(passkeyRepository).save(captor.capture());
        Passkey saved = captor.getValue();

        assertThat(saved.getUserId()).isEqualTo(testUser.getId());
        assertThat(saved.getDeviceName()).isEqualTo("New passkey");
        assertThat(saved.getPublicKey()).isNotEmpty();
        assertThat(saved.getKeyId()).isNotEmpty();
        assertThat(saved.getAaguid()).isNotNull();
        assertThat(saved.getUserHandle()).isEqualTo(testUser.getUserHandle());
    }

    // ---- helpers ----

    private static String buildOptionsJson() throws Exception {
        PublicKeyCredentialCreationOptions options = PublicKeyCredentialCreationOptions.builder()
                .rp(RelyingPartyIdentity.builder().id("localhost").name("Test").build())
                .user(UserIdentity.builder()
                        .name("testuser").displayName("testuser")
                        .id(new ByteArray(new byte[16]))
                        .build())
                .challenge(new ByteArray(new byte[32]))
                .pubKeyCredParams(List.of(PublicKeyCredentialParameters.ES256))
                .build();
        return options.toJson();
    }

    /**
     * Minimal CBOR-encoded attestation object: {"fmt":"none","attStmt":{},"authData":<37 zeros>}.
     * The CBOR map header and field encodings are hand-crafted to satisfy Yubico's parser.
     */
    private static ByteArray buildAttestationObject() {
        byte[] authData = new byte[37]; // 37 zero bytes: valid rpIdHash(32)+flags(1)+signCount(4)
        byte[] header = {
            (byte) 0xA3,                                                     // map(3)
            0x63, 0x66, 0x6D, 0x74,                                         // text(3) "fmt"
            0x64, 0x6E, 0x6F, 0x6E, 0x65,                                   // text(4) "none"
            0x67, 0x61, 0x74, 0x74, 0x53, 0x74, 0x6D, 0x74,                // text(7) "attStmt"
            (byte) 0xA0,                                                     // map(0) {}
            0x68, 0x61, 0x75, 0x74, 0x68, 0x44, 0x61, 0x74, 0x61,         // text(8) "authData"
            0x58, 0x25                                                       // bytes(37)
        };
        byte[] result = new byte[header.length + 37];
        System.arraycopy(header, 0, result, 0, header.length);
        // authData bytes are already zero
        return new ByteArray(result);
    }

    private static VerifyRegistrationRequestResource buildRequest() {
        ByteArray credId = new ByteArray(new byte[32]);
        ByteArray clientDataJSON = buildClientDataJSON("webauthn.create");
        ByteArray attestationObject = buildAttestationObject();
        VerifyClientRequestResource clientResp =
                new VerifyClientRequestResource(attestationObject, clientDataJSON, List.of("internal"));
        VerifyAttestationRequestResource attestation =
                new VerifyAttestationRequestResource(credId, credId, clientResp, "public-key", null, "platform");
        return new VerifyRegistrationRequestResource("testuser", attestation);
    }

    private static ByteArray buildClientDataJSON(String type) {
        String json = "{\"type\":\"" + type + "\","
                + "\"challenge\":\"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\","
                + "\"origin\":\"http://localhost:4200\"}";
        return new ByteArray(json.getBytes(StandardCharsets.UTF_8));
    }
}
