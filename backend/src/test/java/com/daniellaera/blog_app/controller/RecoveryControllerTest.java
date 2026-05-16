package com.daniellaera.blog_app.controller;

import com.daniellaera.blog_app.dto.RecoveryStartRequest;
import com.daniellaera.blog_app.dto.RecoveryStartResponse;
import com.daniellaera.blog_app.dto.RecoveryVerifyRequest;
import com.daniellaera.blog_app.exception.PasskeyException;
import com.daniellaera.blog_app.service.RecoveryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RecoveryControllerTest {

    @Mock RecoveryService recoveryService;
    @InjectMocks RecoveryController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalControllerAdvice())
                .build();
    }

    @Test
    void start_validBody_returns200() throws Exception {
        RecoveryStartResponse response = new RecoveryStartResponse("Code sent to t***@example.com", null);
        when(recoveryService.startRecovery("testuser", "test@example.com")).thenReturn(response);

        String body = objectMapper.writeValueAsString(new RecoveryStartRequest("testuser", "test@example.com"));

        mockMvc.perform(post("/recovery/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Code sent to t***@example.com"));
    }

    @Test
    void start_unknownUsername_returns404() throws Exception {
        when(recoveryService.startRecovery(anyString(), anyString()))
                .thenThrow(new PasskeyException(HttpStatus.NOT_FOUND, "No account found.", "USER_NOT_FOUND"));

        String body = objectMapper.writeValueAsString(new RecoveryStartRequest("unknown", "a@b.com"));

        mockMvc.perform(post("/recovery/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    void start_emailMismatch_returns401() throws Exception {
        when(recoveryService.startRecovery(anyString(), anyString()))
                .thenThrow(new PasskeyException(HttpStatus.UNAUTHORIZED, "Email does not match our records.", "EMAIL_MISMATCH"));

        String body = objectMapper.writeValueAsString(new RecoveryStartRequest("testuser", "wrong@email.com"));

        mockMvc.perform(post("/recovery/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("EMAIL_MISMATCH"));
    }

    @Test
    void verify_validOtp_returns200() throws Exception {
        when(recoveryService.verifyAndStartReregistration("testuser", "123456")).thenReturn(null);

        String body = objectMapper.writeValueAsString(new RecoveryVerifyRequest("testuser", "123456"));

        mockMvc.perform(post("/recovery/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void verify_invalidOtp_returns401() throws Exception {
        when(recoveryService.verifyAndStartReregistration(anyString(), anyString()))
                .thenThrow(new PasskeyException(HttpStatus.UNAUTHORIZED, "Invalid or expired recovery code.", "INVALID_OTP"));

        String body = objectMapper.writeValueAsString(new RecoveryVerifyRequest("testuser", "000000"));

        mockMvc.perform(post("/recovery/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_OTP"));
    }

    @Test
    void verify_expiredOtp_returns401() throws Exception {
        when(recoveryService.verifyAndStartReregistration(anyString(), anyString()))
                .thenThrow(new PasskeyException(HttpStatus.UNAUTHORIZED, "Invalid or expired recovery code.", "INVALID_OTP"));

        String body = objectMapper.writeValueAsString(new RecoveryVerifyRequest("testuser", "999999"));

        mockMvc.perform(post("/recovery/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_OTP"));
    }
}
