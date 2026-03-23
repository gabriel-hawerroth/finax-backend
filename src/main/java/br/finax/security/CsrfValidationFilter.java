package br.finax.security;

import br.finax.exceptions.ServiceException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CsrfValidationFilter extends OncePerRequestFilter {

    private static final String HEADER_XSRF_TOKEN = "X-XSRF-TOKEN";
    private static final String COOKIE_AUTH_TOKEN = SecurityFilter.TOKEN_COOKIE_NAME;

    private final CsrfTokenService csrfTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String method = request.getMethod().toUpperCase();

        if (isMutatingMethod(method)) {
            final String tokenFromHeader = request.getHeader(HEADER_XSRF_TOKEN);
            try {
                csrfTokenService.validateToken(tokenFromHeader);
            } catch (ServiceException ex) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private static boolean isMutatingMethod(String method) {
        return "POST".equals(method) || "PUT".equals(method)
                || "PATCH".equals(method) || "DELETE".equals(method);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        final String method = request.getMethod().toUpperCase();
        if (!isMutatingMethod(method)) {
            return true;
        }

        final String path = request.getServletPath();

        if ("/auth/login".equals(path)
                || "/auth/google".equals(path)
                || "/auth/register".equals(path)
                || "/auth/resend-activation-email".equals(path)
                || "/auth/csrf".equals(path)
                || path.startsWith("/login/")) {
            return true;
        }

        final Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return true;
        }

        for (Cookie cookie : cookies) {
            if (COOKIE_AUTH_TOKEN.equals(cookie.getName())) {
                return false;
            }
        }

        return true;
    }
}
