package br.finax.controllers;

import br.finax.security.CsrfTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class CsrfController {

    private final CsrfTokenService csrfTokenService;

    @Value("${finax.security.cookie-secure:false}")
    private boolean secureCookie;

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf() {
        final String token = csrfTokenService.generateToken();

        final ResponseCookie cookie = ResponseCookie.from("XSRF-TOKEN", token)
                .path("/")
                .httpOnly(false)
                .sameSite("Lax")
                .secure(secureCookie)
                .maxAge(600)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate")
                .header("Pragma", "no-cache")
                .build();
    }
}
