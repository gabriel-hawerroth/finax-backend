package br.finax.services;

import br.finax.dto.app.emails.EmailDTO;
import br.finax.enums.ErrorCategory;
import br.finax.exceptions.ServiceException;
import br.finax.models.PasswordRecoveryToken;
import br.finax.models.User;
import br.finax.repository.PasswordRecoveryTokenRepository;
import br.finax.repository.UserRepository;
import br.finax.security.SecurityFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class PasswordRecoveryService {

    private static final int TOKEN_TTL_MINUTES = 20;
    private static final int TOKEN_BYTES = 32;
    private static final String INVALID_OR_EXPIRED = "Invalid or expired recovery token";
    private static final String INVALID_PASSWORD = "Password does not meet security requirements";

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordRecoveryTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityFilter securityFilter;

    @Transactional
    public void requestRecovery(String email, HttpServletRequest request) {
        email = HtmlUtils.htmlEscape(email).trim().toLowerCase(Locale.ROOT);

        final Optional<User> optionalUser = userService.findByEmailOptional(email);
        if (optionalUser.isEmpty()) {
            return;
        }

        final User user = optionalUser.get();
        final Instant now = Instant.now();
        final String rawToken = generateRawToken();
        final String tokenHash = sha256(rawToken);

        tokenRepository.invalidateActiveTokens(user.getId(), now, now);

        final PasswordRecoveryToken recoveryToken = new PasswordRecoveryToken();
        recoveryToken.setUser(user);
        recoveryToken.setTokenHash(tokenHash);
        recoveryToken.setCreatedAt(now);
        recoveryToken.setExpiresAt(now.plusSeconds(TOKEN_TTL_MINUTES * 60L));
        recoveryToken.setCreatedIp(extractClientIp(request));
        recoveryToken.setRequestUserAgent(truncateUserAgent(request.getHeader("User-Agent")));

        tokenRepository.save(recoveryToken);
        logger.info("Password recovery token created: userId=" + user.getId()
            + ", tokenId=" + recoveryToken.getId()
            + ", createdAt=" + recoveryToken.getCreatedAt()
            + ", expiresAt=" + recoveryToken.getExpiresAt());

        try {
            final String mailContent = emailService.buildPasswordRecoveryContent(user, rawToken);

            emailService.sendMail(new EmailDTO(
                    user.getEmail(),
                    "Alteracao de senha",
                    emailService.buildEmailTemplate(mailContent)
            ));
        } catch (Exception ex) {
            logger.warning("Password recovery email dispatch failed for user id=" + user.getId());
        }
    }

    @Transactional
    public void confirmRecovery(String token, String newPassword) {
        token = token != null ? token.trim() : null;
        validateTokenFormat(token);
        validatePasswordPolicy(newPassword);

        final Instant now = Instant.now();
        final String tokenHash = sha256(token);

        final PasswordRecoveryToken recoveryToken = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ServiceException(ErrorCategory.GONE, INVALID_OR_EXPIRED));

        if (recoveryToken.getUsedAt() != null || recoveryToken.getExpiresAt().isBefore(now)) {
            logger.info("Password recovery confirm denied: tokenId=" + recoveryToken.getId()
                + ", usedAt=" + recoveryToken.getUsedAt()
                + ", expiresAt=" + recoveryToken.getExpiresAt()
                + ", now=" + now);
            throw new ServiceException(ErrorCategory.GONE, INVALID_OR_EXPIRED);
        }

        final int marked = tokenRepository.markAsUsed(recoveryToken.getId(), now, now);
        if (marked == 0) {
            throw new ServiceException(ErrorCategory.GONE, INVALID_OR_EXPIRED);
        }

        final User user = recoveryToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        securityFilter.updateCachedUser(user);

        tokenRepository.invalidateActiveTokens(user.getId(), now, now);
    }

    @Transactional(readOnly = true)
    public void validateToken(String token) {
        token = token != null ? token.trim() : null;
        validateTokenFormat(token);

        final String tokenHash = sha256(token);
        final Instant now = Instant.now();

        final PasswordRecoveryToken recoveryToken = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ServiceException(ErrorCategory.GONE, INVALID_OR_EXPIRED));

        if (recoveryToken.getUsedAt() != null || recoveryToken.getExpiresAt().isBefore(now)) {
            logger.info("Password recovery validate denied: tokenId=" + recoveryToken.getId()
                    + ", usedAt=" + recoveryToken.getUsedAt()
                    + ", expiresAt=" + recoveryToken.getExpiresAt()
                    + ", now=" + now);
            throw new ServiceException(ErrorCategory.GONE, INVALID_OR_EXPIRED);
        }
    }

    private static String generateRawToken() {
        final byte[] bytes = new byte[TOKEN_BYTES];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256(String value) {
        try {
            final byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new ServiceException(ErrorCategory.INTERNAL_ERROR, "Could not hash recovery token", ex);
        }
    }

    private static void validateTokenFormat(String token) {
        if (token == null || token.isBlank() || !token.matches("^[A-Za-z0-9_-]{30,200}$")) {
            throw new ServiceException(ErrorCategory.GONE, INVALID_OR_EXPIRED);
        }
    }

    private static void validatePasswordPolicy(String password) {
        if (password == null || !password.matches("^(?=.*[!@#$%^&*(),.?\":{}|<>])(?=.*\\d)(?=.*[A-Z]).{8,}$")) {
            throw new ServiceException(ErrorCategory.BAD_REQUEST, INVALID_PASSWORD);
        }
    }

    private static String extractClientIp(HttpServletRequest request) {
        final String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    private static String truncateUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return null;
        }

        return userAgent.length() > 255 ? userAgent.substring(0, 255) : userAgent;
    }
}
