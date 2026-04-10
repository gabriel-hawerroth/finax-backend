package br.finax.controllers;

import java.util.TimeZone;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.finax.dto.auth.AuthenticationDTO;
import br.finax.dto.auth.LoginDTO;
import br.finax.dto.auth.GoogleAuthDTO;
import br.finax.dto.auth.ResendActivationEmailDTO;
import br.finax.models.User;
import br.finax.security.JwtCookieService;
import br.finax.services.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtCookieService jwtCookieService;

    @PostMapping("/login")
    public ResponseEntity<User> login(
            @RequestBody @Valid AuthenticationDTO authDTO,
            HttpServletResponse response
    ) {
        final LoginDTO loginResponse = authService.doLogin(authDTO);
        jwtCookieService.addTokenCookie(response, loginResponse.token());
        return ResponseEntity.ok(loginResponse.user());
    }

    @PostMapping("/google")
    public ResponseEntity<User> googleLogin(
            @RequestBody @Valid GoogleAuthDTO googleAuthDTO,
            HttpServletResponse response
    ) {
        final LoginDTO loginResponse = authService.doGoogleLogin(googleAuthDTO);
        jwtCookieService.addTokenCookie(response, loginResponse.token());
        return ResponseEntity.ok(loginResponse.user());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        jwtCookieService.clearTokenCookie(response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody @Valid User user) {
        return ResponseEntity.ok(
                authService.registerNewUser(user)
        );
    }

    @PostMapping("/resend-activation-email")
    public ResponseEntity<Void> resendActivationEmail(@RequestBody @Valid ResendActivationEmailDTO email) {
        authService.resendActivationEmail(email.email());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken csrfToken) {
        log.info("[CSRF] Endpoint called - forcing token materialization");
        
        if (csrfToken != null) {
            // Acessar o token força a sua materialização e Spring Security
            // automaticamente seta o cookie XSRF-TOKEN na resposta via CookieCsrfTokenRepository
            final String tokenValue = csrfToken.getToken();
            log.info("[CSRF] Token materialized: {} (length: {})", 
                tokenValue.substring(0, Math.min(20, tokenValue.length())) + "...",
                tokenValue.length());
            log.info("[CSRF] Header name: {}, Parameter name: {}", 
                csrfToken.getHeaderName(), csrfToken.getParameterName());
        } else {
            log.warn("[CSRF] WARNING: CsrfToken is NULL - check SecurityFilterChain configuration");
        }
        
        // Spring Security + CookieCsrfTokenRepository gerencia o Set-Cookie automaticamente
        // Apenas adicione cache headers para não cachear este endpoint
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .build();
    }

    @GetMapping("/timezone")
    public ResponseEntity<String> getTimeZone() {
        return ResponseEntity.ok("Timezone atual: " + TimeZone.getDefault().getID());
    }
}
