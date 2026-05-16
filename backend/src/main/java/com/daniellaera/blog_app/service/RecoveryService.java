package com.daniellaera.blog_app.service;

import com.daniellaera.blog_app.dto.RecoveryStartResponse;
import com.daniellaera.blog_app.dto.StartRegisterResponseResource;
import com.daniellaera.blog_app.exception.PasskeyException;
import com.daniellaera.blog_app.model.OtpCode;
import com.daniellaera.blog_app.model.User;
import com.daniellaera.blog_app.repository.OtpCodeRepository;
import com.daniellaera.blog_app.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecoveryService {

    private final UserRepository userRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final JavaMailSender mailSender;
    private final StartRegistrationService startRegistrationService;

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    public RecoveryStartResponse startRecovery(String username, String email) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new PasskeyException(
                        HttpStatus.NOT_FOUND, "No account found.", "USER_NOT_FOUND"));

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            user.setEmail(email);
            userRepository.save(user);
        } else if (!user.getEmail().equalsIgnoreCase(email)) {
            throw new PasskeyException(
                    HttpStatus.UNAUTHORIZED,
                    "Email does not match our records.",
                    "EMAIL_MISMATCH");
        }

        otpCodeRepository.deleteByUserAndUsedTrue(user);

        String code = String.format("%06d", new SecureRandom().nextInt(999999));

        OtpCode otp = new OtpCode();
        otp.setUser(user);
        otp.setCode(code);
        otp.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        otpCodeRepository.save(otp);

        sendOtpEmail(email, code, username);

        boolean isDev = activeProfile.contains("dev");
        log.info("Recovery OTP for {} (dev={}): {}", username, isDev, isDev ? code : "***");

        return new RecoveryStartResponse(
                "Recovery code sent to " + maskEmail(email),
                isDev ? code : null);
    }

    public StartRegisterResponseResource verifyAndStartReregistration(
            String username, String code) throws JsonProcessingException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new PasskeyException(
                        HttpStatus.NOT_FOUND, "No account found.", "USER_NOT_FOUND"));

        OtpCode otp = otpCodeRepository
                .findByUserAndCodeAndUsedFalseAndExpiresAtAfter(user, code, Instant.now())
                .orElseThrow(() -> new PasskeyException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid or expired recovery code.",
                        "INVALID_OTP"));

        otp.setUsed(true);
        otpCodeRepository.save(otp);

        user.setRegistrationComplete(false);
        user.setAssertion(null);
        userRepository.save(user);

        return startRegistrationService.startRegistrationForExistingUser(user);
    }

    private void sendOtpEmail(String to, String code, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Your recovery code");
            message.setText(
                    "Hi " + username + ",\n\n" +
                    "Your recovery code is: " + code + "\n\n" +
                    "This code expires in 10 minutes.\n\n" +
                    "If you did not request this, ignore this email.\n\n" +
                    "— The App Team"
            );
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send recovery email to {}: {}", to, e.getMessage());
        }
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return email;
        return email.charAt(0) + "***" + email.substring(at);
    }
}
