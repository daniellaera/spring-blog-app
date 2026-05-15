package com.daniellaera.blog_app.controller;

import com.daniellaera.blog_app.dto.PasskeyRenameRequest;
import com.daniellaera.blog_app.model.Passkey;
import com.daniellaera.blog_app.model.User;
import com.daniellaera.blog_app.repository.PasskeyRepository;
import com.daniellaera.blog_app.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PasskeyManagementControllerTest {

    @Mock UserRepository userRepository;
    @Mock PasskeyRepository passkeyRepository;
    @InjectMocks PasskeyManagementController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private User user;
    private final byte[] userHandle = new byte[]{1, 2, 3, 4};

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalControllerAdvice())
                .build();

        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setUserHandle(userHandle);
    }

    @Test
    void listPasskeys_authenticated_returns200WithList() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", "testuser");

        Passkey p = buildPasskey(UUID.randomUUID(), "My iPhone");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passkeyRepository.findAllByUserHandle(userHandle)).thenReturn(List.of(p));

        mockMvc.perform(get("/api/user/passkeys").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].deviceName").value("My iPhone"));
    }

    @Test
    void listPasskeys_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/user/passkeys"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deletePasskey_success_returns204() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", "testuser");

        UUID passkeyId = UUID.randomUUID();
        Passkey p1 = buildPasskey(passkeyId, "Device A");
        Passkey p2 = buildPasskey(UUID.randomUUID(), "Device B");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passkeyRepository.findById(passkeyId)).thenReturn(Optional.of(p1));
        when(passkeyRepository.findAllByUserHandle(userHandle)).thenReturn(List.of(p1, p2));

        mockMvc.perform(delete("/api/user/passkeys/{id}", passkeyId).session(session))
                .andExpect(status().isNoContent());

        verify(passkeyRepository).delete(p1);
    }

    @Test
    void deletePasskey_lastCredential_returns400WithLastPasskeyCode() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", "testuser");

        UUID passkeyId = UUID.randomUUID();
        Passkey only = buildPasskey(passkeyId, "Only Device");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passkeyRepository.findById(passkeyId)).thenReturn(Optional.of(only));
        when(passkeyRepository.findAllByUserHandle(userHandle)).thenReturn(List.of(only));

        mockMvc.perform(delete("/api/user/passkeys/{id}", passkeyId).session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LAST_PASSKEY"));

        verify(passkeyRepository, never()).delete(any());
    }

    @Test
    void deleteOrphanedPasskeys_removesStaleCredentials() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", "testuser");

        Instant staleTime = Instant.now().minus(100, ChronoUnit.DAYS);
        Passkey stale = buildPasskey(UUID.randomUUID(), "Old Device");
        stale.setLastUsedAt(staleTime);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passkeyRepository.findByUserHandleAndLastUsedAtNotNullAndLastUsedAtBefore(
                any(), any())).thenReturn(List.of(stale));

        mockMvc.perform(delete("/api/user/passkeys/orphaned").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.removed").value(1));

        verify(passkeyRepository).deleteAll(List.of(stale));
    }

    @Test
    void renamePasskey_success_returnsUpdatedDto() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("username", "testuser");

        UUID passkeyId = UUID.randomUUID();
        Passkey p = buildPasskey(passkeyId, "Old Name");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passkeyRepository.findById(passkeyId)).thenReturn(Optional.of(p));
        when(passkeyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String body = objectMapper.writeValueAsString(new PasskeyRenameRequest("New Name"));

        mockMvc.perform(patch("/api/user/passkeys/{id}", passkeyId)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceName").value("New Name"));
    }

    private Passkey buildPasskey(UUID id, String deviceName) {
        Passkey p = new Passkey();
        p.setId(id);
        p.setUserHandle(userHandle);
        p.setDeviceName(deviceName);
        p.setTransport("internal");
        p.setKeyId(new byte[]{1, 2, 3});
        p.setPublicKey(new byte[]{4, 5, 6});
        p.setType("public-key");
        p.setSignatureCount(0L);
        return p;
    }
}
