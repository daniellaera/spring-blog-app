package com.daniellaera.blog_app.service;

import com.daniellaera.blog_app.dto.RecoveryStartResponse;
import com.daniellaera.blog_app.dto.StartRegisterResponseResource;
import com.daniellaera.blog_app.exception.PasskeyException;
import com.daniellaera.blog_app.model.OtpCode;
import com.daniellaera.blog_app.model.User;
import com.daniellaera.blog_app.repository.OtpCodeRepository;
import com.daniellaera.blog_app.repository.UserRepository;
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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecoveryServiceTest {

    @Mock UserRepository userRepository;
    @Mock OtpCodeRepository otpCodeRepository;
    @Mock JavaMailSender mailSender;
    @Mock StartRegistrationService startRegistrationService;

    @InjectMocks RecoveryService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setUserHandle(new byte[]{1, 2, 3, 4});
        user.setRegistrationComplete(true);

        when(otpCodeRepository.save(any(OtpCode.class))).thenAnswer(inv -> inv.getArgument(0));

        // @Value fields are not injected by @InjectMocks; set the default profile explicitly
        ReflectionTestUtils.setField(service, "activeProfile", "prod");
    }

    @Test
    void startRecovery_userNotFound_throwsPasskeyException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startRecovery("unknown", "a@b.com"))
                .isInstanceOf(PasskeyException.class)
                .satisfies(ex -> {
                    PasskeyException pe = (PasskeyException) ex;
                    assertThat(pe.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(pe.getCode()).isEqualTo("USER_NOT_FOUND");
                });
    }

    @Test
    void startRecovery_emailMismatch_throwsPasskeyException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.startRecovery("testuser", "wrong@example.com"))
                .isInstanceOf(PasskeyException.class)
                .satisfies(ex -> {
                    PasskeyException pe = (PasskeyException) ex;
                    assertThat(pe.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(pe.getCode()).isEqualTo("EMAIL_MISMATCH");
                });
    }

    @Test
    void startRecovery_newEmail_savesEmailOnUser() {
        user.setEmail(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        service.startRecovery("testuser", "new@example.com");

        assertThat(user.getEmail()).isEqualTo("new@example.com");
        verify(userRepository, atLeastOnce()).save(user);
    }

    @Test
    void startRecovery_generatesOtpAndSavesIt() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        ArgumentCaptor<OtpCode> captor = ArgumentCaptor.forClass(OtpCode.class);
        when(otpCodeRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.startRecovery("testuser", "test@example.com");

        OtpCode saved = captor.getValue();
        assertThat(saved.getCode()).hasSize(6).matches("\\d{6}");
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getExpiresAt()).isAfter(Instant.now());
        assertThat(saved.isUsed()).isFalse();
    }

    @Test
    void startRecovery_devProfile_returnsOtpInResponse() {
        ReflectionTestUtils.setField(service, "activeProfile", "dev");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        RecoveryStartResponse response = service.startRecovery("testuser", "test@example.com");

        assertThat(response.devOtp()).isNotNull().hasSize(6);
    }

    @Test
    void startRecovery_prodProfile_returnsNullOtp() {
        ReflectionTestUtils.setField(service, "activeProfile", "prod");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        RecoveryStartResponse response = service.startRecovery("testuser", "test@example.com");

        assertThat(response.devOtp()).isNull();
    }

    @Test
    void verifyAndStartReregistration_validOtp_resetsRegistrationAndReturnsOptions() throws Exception {
        OtpCode otp = new OtpCode();
        otp.setUser(user);
        otp.setCode("123456");
        otp.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        otp.setUsed(false);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(otpCodeRepository.findByUserAndCodeAndUsedFalseAndExpiresAtAfter(
                eq(user), eq("123456"), any(Instant.class))).thenReturn(Optional.of(otp));
        when(otpCodeRepository.save(otp)).thenReturn(otp);
        when(userRepository.save(user)).thenReturn(user);

        StartRegisterResponseResource mockOptions = mock(StartRegisterResponseResource.class);
        when(startRegistrationService.startRegistrationForExistingUser(user)).thenReturn(mockOptions);

        StartRegisterResponseResource result = service.verifyAndStartReregistration("testuser", "123456");

        assertThat(result).isEqualTo(mockOptions);
        assertThat(otp.isUsed()).isTrue();
        assertThat(user.isRegistrationComplete()).isFalse();
        assertThat(user.getAssertion()).isNull();
    }

    @Test
    void verifyAndStartReregistration_invalidOtp_throwsPasskeyException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(otpCodeRepository.findByUserAndCodeAndUsedFalseAndExpiresAtAfter(any(), any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verifyAndStartReregistration("testuser", "000000"))
                .isInstanceOf(PasskeyException.class)
                .satisfies(ex -> {
                    PasskeyException pe = (PasskeyException) ex;
                    assertThat(pe.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(pe.getCode()).isEqualTo("INVALID_OTP");
                });
    }

    @Test
    void verifyAndStartReregistration_expiredOtp_throwsPasskeyException() {
        // Expired OTPs are excluded by the repo query (expiresAtAfter clause), so it returns empty
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(otpCodeRepository.findByUserAndCodeAndUsedFalseAndExpiresAtAfter(any(), any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verifyAndStartReregistration("testuser", "123456"))
                .isInstanceOf(PasskeyException.class)
                .satisfies(ex -> assertThat(((PasskeyException) ex).getCode()).isEqualTo("INVALID_OTP"));
    }

    @Test
    void verifyAndStartReregistration_alreadyUsedOtp_throwsPasskeyException() {
        // Already-used OTPs are excluded by the repo query (usedFalse clause), so it returns empty
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(otpCodeRepository.findByUserAndCodeAndUsedFalseAndExpiresAtAfter(any(), any(), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verifyAndStartReregistration("testuser", "123456"))
                .isInstanceOf(PasskeyException.class)
                .satisfies(ex -> assertThat(((PasskeyException) ex).getCode()).isEqualTo("INVALID_OTP"));
    }
}
