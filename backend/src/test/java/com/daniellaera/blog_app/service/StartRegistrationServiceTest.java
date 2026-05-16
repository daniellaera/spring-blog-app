package com.daniellaera.blog_app.service;

import com.daniellaera.blog_app.dto.StartRegisterRequestResource;
import com.daniellaera.blog_app.dto.StartRegisterResponseResource;
import com.daniellaera.blog_app.model.User;
import com.daniellaera.blog_app.repository.UserRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StartRegistrationServiceTest {

    @Mock UserRepository userRepository;
    @Mock RelyingParty relyingParty;

    private StartRegistrationService service;
    private PublicKeyCredentialCreationOptions creationOptions;

    @BeforeEach
    void setUp() throws Exception {
        AuthenticatorSelectionCriteria criteria = AuthenticatorSelectionCriteria.builder().build();
        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder().id("localhost").name("Test").build();
        List<PublicKeyCredentialParameters> params = List.of(PublicKeyCredentialParameters.ES256);

        service = new StartRegistrationService(userRepository, criteria, rpIdentity, relyingParty, params);

        creationOptions = PublicKeyCredentialCreationOptions.builder()
                .rp(rpIdentity)
                .user(UserIdentity.builder()
                        .name("testuser").displayName("testuser")
                        .id(new ByteArray(new byte[16]))
                        .build())
                .challenge(new ByteArray(new byte[32]))
                .pubKeyCredParams(List.of(PublicKeyCredentialParameters.ES256))
                .build();

        when(relyingParty.startRegistration(any(StartRegistrationOptions.class))).thenReturn(creationOptions);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void startRegistration_existingUser_throwsIllegalArgumentException() {
        User existing = new User();
        existing.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.startRegistration(new StartRegisterRequestResource("testuser")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("testuser");

        verify(userRepository, never()).save(any());
    }

    @Test
    void startRegistration_newUser_createsAndSavesUser() throws Exception {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        StartRegisterResponseResource result = service.startRegistration(new StartRegisterRequestResource("newuser"));

        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getUsername()).isEqualTo("newuser");
        assertThat(saved.isRegistrationComplete()).isFalse();
        assertThat(saved.getUserHandle()).isNotEmpty();
        assertThat(result).isNotNull();
        assertThat(result.challenge()).isNotBlank();
    }

    @Test
    void startRegistrationForExistingUser_updatesPublicKeyJsonAndReturnsOptions() throws Exception {
        User existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        existingUser.setUsername("testuser");
        existingUser.setUserHandle(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});
        existingUser.setRegistrationComplete(true);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        StartRegisterResponseResource result = service.startRegistrationForExistingUser(existingUser);

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPublicKeyJson()).isNotBlank();
        assertThat(result).isNotNull();
        assertThat(result.challenge()).isNotBlank();
    }

    @Test
    void startRegistrationForExistingUser_doesNotCreateNewUser() throws Exception {
        User existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        existingUser.setUsername("testuser");
        existingUser.setUserHandle(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});

        service.startRegistrationForExistingUser(existingUser);

        // Only one save call (update existing), no new user creation
        verify(userRepository, times(1)).save(existingUser);
        verify(userRepository, never()).findByUsername(any());
    }
}
