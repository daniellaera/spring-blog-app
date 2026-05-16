package com.daniellaera.blog_app.it;

import com.daniellaera.blog_app.dto.RecoveryStartRequest;
import com.daniellaera.blog_app.dto.RecoveryVerifyRequest;
import com.daniellaera.blog_app.model.OtpCode;
import com.daniellaera.blog_app.model.User;
import com.daniellaera.blog_app.repository.OtpCodeRepository;
import com.daniellaera.blog_app.repository.UserRepository;
import com.daniellaera.blog_app.utils.TestcontainersConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class RecoveryFlowIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired OtpCodeRepository otpCodeRepository;
    @MockitoBean JavaMailSender mailSender;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User testUser;

    @BeforeEach
    void setUp() {
        otpCodeRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("recovery-test-user");
        testUser.setEmail("recovery@example.com");
        testUser.setUserHandle(new byte[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 11, 12, 13, 14, 15, 16,
                17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36});
        testUser.setRegistrationComplete(true);
        testUser = userRepository.save(testUser);
    }

    @Test
    void startRecovery_validUser_returns200WithMessage() throws Exception {
        String body = objectMapper.writeValueAsString(
                new RecoveryStartRequest("recovery-test-user", "recovery@example.com"));

        mockMvc.perform(post("/recovery/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Recovery code sent")));

        assertThat(otpCodeRepository.findAll()).hasSize(1);
    }

    @Test
    void startRecovery_unknownUser_returns404() throws Exception {
        String body = objectMapper.writeValueAsString(
                new RecoveryStartRequest("no-such-user", "x@x.com"));

        mockMvc.perform(post("/recovery/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    void startRecovery_emailMismatch_returns401() throws Exception {
        String body = objectMapper.writeValueAsString(
                new RecoveryStartRequest("recovery-test-user", "wrong@email.com"));

        mockMvc.perform(post("/recovery/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("EMAIL_MISMATCH"));
    }

    @Test
    void verifyOtp_validOtp_setsRegistrationFalseAndReturnsOptions() throws Exception {
        OtpCode otp = new OtpCode();
        otp.setUser(testUser);
        otp.setCode("123456");
        otp.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        otp.setUsed(false);
        otp = otpCodeRepository.save(otp);

        String body = objectMapper.writeValueAsString(
                new RecoveryVerifyRequest("recovery-test-user", "123456"));

        mockMvc.perform(post("/recovery/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.challenge").exists());

        User updatedUser = userRepository.findByUsername("recovery-test-user").orElseThrow();
        assertThat(updatedUser.isRegistrationComplete()).isFalse();
        assertThat(updatedUser.getAssertion()).isNull();

        OtpCode usedOtp = otpCodeRepository.findById(otp.getId()).orElseThrow();
        assertThat(usedOtp.isUsed()).isTrue();
    }

    @Test
    void verifyOtp_invalidOtp_returns401() throws Exception {
        String body = objectMapper.writeValueAsString(
                new RecoveryVerifyRequest("recovery-test-user", "000000"));

        mockMvc.perform(post("/recovery/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_OTP"));
    }

    @Test
    void verifyOtp_expiredOtp_returns401() throws Exception {
        OtpCode expired = new OtpCode();
        expired.setUser(testUser);
        expired.setCode("654321");
        expired.setExpiresAt(Instant.now().minus(5, ChronoUnit.MINUTES));
        expired.setUsed(false);
        otpCodeRepository.save(expired);

        String body = objectMapper.writeValueAsString(
                new RecoveryVerifyRequest("recovery-test-user", "654321"));

        mockMvc.perform(post("/recovery/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_OTP"));
    }

    @Test
    void verifyOtp_secondUseOfSameOtp_returns401() throws Exception {
        OtpCode otp = new OtpCode();
        otp.setUser(testUser);
        otp.setCode("111222");
        otp.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        otp.setUsed(false);
        otpCodeRepository.save(otp);

        String body = objectMapper.writeValueAsString(
                new RecoveryVerifyRequest("recovery-test-user", "111222"));

        // First use succeeds
        mockMvc.perform(post("/recovery/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        // Reset user for second attempt (re-registration sets registrationComplete=false, need it true for next test)
        testUser = userRepository.findByUsername("recovery-test-user").orElseThrow();

        // Second use of same OTP fails
        mockMvc.perform(post("/recovery/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_OTP"));
    }

    @Test
    void fullRecoveryFlow_startThenVerify_userRegistrationResetAndOtpMarkedUsed() throws Exception {
        // Step 1: Start recovery - gets OTP saved to DB
        String startBody = objectMapper.writeValueAsString(
                new RecoveryStartRequest("recovery-test-user", "recovery@example.com"));

        mockMvc.perform(post("/recovery/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(startBody))
                .andExpect(status().isOk());

        // Step 2: Get the OTP from DB directly (since dev mode may not be active in test profile)
        List<OtpCode> otps = otpCodeRepository.findAll();
        assertThat(otps).hasSize(1);
        String otpCode = otps.get(0).getId() != null ? otps.get(0).getCode() : null;
        assertThat(otpCode).isNotNull();

        // Step 3: Verify with the OTP
        String verifyBody = objectMapper.writeValueAsString(
                new RecoveryVerifyRequest("recovery-test-user", otpCode));

        mockMvc.perform(post("/recovery/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.challenge").exists());

        // Step 4: Verify user.registrationComplete is now false
        User updatedUser = userRepository.findByUsername("recovery-test-user").orElseThrow();
        assertThat(updatedUser.isRegistrationComplete()).isFalse();

        // Step 5: Verify OTP is marked as used
        OtpCode usedOtp = otpCodeRepository.findAll().get(0);
        assertThat(usedOtp.isUsed()).isTrue();

        // Step 6: Second use of same OTP fails with 401
        mockMvc.perform(post("/recovery/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_OTP"));
    }
}
