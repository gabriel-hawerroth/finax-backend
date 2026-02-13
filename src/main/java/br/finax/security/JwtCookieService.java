package br.finax.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class JwtCookieService {

    private static final String TOKEN_COOKIE_NAME = SecurityFilter.TOKEN_COOKIE_NAME;
    private static final int TOKEN_MAX_AGE_SECONDS = 86400; // 24 horas (mesmo tempo do token JWT)

    @Value("${finax.security.cookie-secure:true}")
    private boolean secureCookie;

    public void addTokenCookie(HttpServletResponse response, String token) {
        final ResponseCookie cookie = ResponseCookie.from(TOKEN_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/")
                .maxAge(TOKEN_MAX_AGE_SECONDS)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void clearTokenCookie(HttpServletResponse response) {
        final ResponseCookie cookie = ResponseCookie.from(TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}


