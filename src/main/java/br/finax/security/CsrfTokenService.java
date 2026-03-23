package br.finax.security;

import br.finax.enums.ErrorCategory;
import br.finax.exceptions.ServiceException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class CsrfTokenService {

    private static final String ISSUER = "api-finax-csrf";
    private static final int TOKEN_TTL_SECONDS = 600;

    @Value("${CSRF_SECRET_KEY:${csrf.secret-key:change-me-in-production}}")
    private String secretKey;

    public String generateToken() {
        final Instant now = Instant.now();
        final Instant expiration = now.plusSeconds(TOKEN_TTL_SECONDS);

        try {
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withJWTId(UUID.randomUUID().toString())
                    .withIssuedAt(now)
                    .withExpiresAt(expiration)
                    .sign(getAlgorithm());
        } catch (JWTCreationException ex) {
            throw new ServiceException(ErrorCategory.INTERNAL_ERROR, "Could not generate CSRF token", ex);
        }
    }

    public void validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new ServiceException(ErrorCategory.UNAUTHORIZED, "CSRF token is missing");
        }

        try {
            JWT.require(getAlgorithm())
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token);
        } catch (JWTVerificationException ex) {
            throw new ServiceException(ErrorCategory.UNAUTHORIZED, "Invalid or expired CSRF token");
        }
    }

    private Algorithm getAlgorithm() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new ServiceException(ErrorCategory.INTERNAL_ERROR, "CSRF secret key is not configured");
        }

        return Algorithm.HMAC256(secretKey);
    }
}
