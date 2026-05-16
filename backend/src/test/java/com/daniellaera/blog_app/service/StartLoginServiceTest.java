package com.daniellaera.blog_app.service;

import com.daniellaera.blog_app.dto.LoginRequestResource;
import com.daniellaera.blog_app.dto.LoginResponseResource;
import com.daniellaera.blog_app.model.User;
import com.daniellaera.blog_app.repository.UserRepository;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StartLoginServiceTest {

    @Mock RelyingParty relyingParty;
    @Mock UserRepository userRepository;

    private StartLoginService service;

    private User user;
    private AssertionRequest assertionRequest;

    @BeforeEach
    void setUp() throws Exception {
        service = new StartLoginService(relyingParty, userRepository);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setUserHandle(new byte[]{1, 2, 3, 4});
        user.setRegistrationComplete(true);

        PublicKeyCredentialRequestOptions requestOptions = PublicKeyCredentialRequestOptions.builder()
                .challenge(new ByteArray(new byte[32]))
                .build();
        assertionRequest = AssertionRequest.builder()
                .publicKeyCredentialRequestOptions(requestOptions)
                .username("testuser")
                .build();

        when(relyingParty.getIdentity()).thenReturn(
                RelyingPartyIdentity.builder().id("localhost").name("Test").build());
        when(relyingParty.startAssertion(any(StartAssertionOptions.class))).thenReturn(assertionRequest);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void startLogin_userNotFound_throwsRuntimeException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startLogin(new LoginRequestResource("unknown")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not registered");
    }

    @Test
    void startLogin_registrationIncomplete_throwsRuntimeException() {
        user.setRegistrationComplete(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.startLogin(new LoginRequestResource("testuser")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not registered");
    }

    @Test
    void startLogin_validUser_returnsLoginResponse() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        LoginResponseResource response = service.startLogin(new LoginRequestResource("testuser"));

        assertThat(response).isNotNull();
        assertThat(response.challenge()).isNotBlank();
        assertThat(response.rpId()).isEqualTo("localhost");
        assertThat(response.timeout()).isEqualTo(60000);
        assertThat(response.userVerification()).isEqualTo("preferred");
    }

    @Test
    void startLogin_validUser_savesAssertionOnUser() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        service.startLogin(new LoginRequestResource("testuser"));

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getAssertion()).isNotBlank();
    }

    @Test
    void startLogin_validUser_includesAllowCredentials() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        LoginResponseResource response = service.startLogin(new LoginRequestResource("testuser"));

        // allowCredentials can be empty when no passkeys are registered; the list itself is non-null
        assertThat(response.allowCredentials()).isNotNull();
    }
}
