package com.group06.restaurantevent.auth.service;

import com.group06.restaurantevent.auth.dto.request.ForgotPasswordRequest;
import com.group06.restaurantevent.auth.dto.request.ResetPasswordRequest;
import com.group06.restaurantevent.auth.dto.response.ForgotPasswordResponse;
import com.group06.restaurantevent.auth.entity.PasswordResetToken;
import com.group06.restaurantevent.auth.repository.PasswordResetTokenRepository;
import com.group06.restaurantevent.common.exception.BadRequestException;
import com.group06.restaurantevent.users.entity.User;
import com.group06.restaurantevent.users.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Forgot-password flow. There is no email server in this project, so the reset link is
 * written to the backend log (a simulated email) and, for local demos only, can be returned
 * in the API response by setting app.auth.expose-reset-link=true.
 */
@Service
@Slf4j
public class PasswordResetService {

    static final String GENERIC_MESSAGE =
            "If an account exists for that email, a password reset link has been sent.";

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final int validMinutes;
    private final boolean exposeLink;
    private final String frontendUrl;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${app.auth.reset-token-minutes:30}") int validMinutes,
                                @Value("${app.auth.expose-reset-link:false}") boolean exposeLink,
                                @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.validMinutes = validMinutes;
        this.exposeLink = exposeLink;
        this.frontendUrl = frontendUrl;
    }

    @Transactional
    public ForgotPasswordResponse requestReset(ForgotPasswordRequest req) {
        Optional<User> account = userRepository.findByEmailAndIsActiveTrue(req.getEmail().trim());
        if (account.isEmpty()) {
            log.info("[Password reset] requested for unknown or inactive email");
            return ForgotPasswordResponse.builder().message(GENERIC_MESSAGE).build();
        }
        User user = account.get();

        // Only the newest link works.
        tokenRepository.findByUserIdAndUsedFalse(user.getId()).forEach(t -> t.setUsed(true));

        PasswordResetToken token = tokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .token(newToken())
                .expiresAt(LocalDateTime.now().plusMinutes(validMinutes))
                .used(false)
                .build());

        String link = frontendUrl + "/reset-password?token=" + token.getToken();
        log.info("[Password reset email simulated] to {}: {} (valid {} minutes)", user.getEmail(), link, validMinutes);

        return ForgotPasswordResponse.builder()
                .message(GENERIC_MESSAGE)
                .resetLink(exposeLink ? link : null)
                .build();
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        PasswordResetToken token = tokenRepository.findByToken(req.getToken().trim())
                .filter(t -> t.isUsable(LocalDateTime.now()))
                .orElseThrow(() -> new BadRequestException("This reset link is invalid or has expired"));
        User user = token.getUser();
        if (!user.isActive())
            throw new BadRequestException("This account is not active");

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        token.setUsed(true);
        tokenRepository.save(token);
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
