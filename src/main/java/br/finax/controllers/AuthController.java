package br.finax.controllers;

import java.util.TimeZone;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.finax.dto.AuthenticationDTO;
import br.finax.dto.LoginDTO;
import br.finax.dto.auth.ResendActivationEmailDTO;
import br.finax.models.User;
import br.finax.security.JwtCookieService;
import br.finax.services.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
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

    @GetMapping("/timezone")
    public ResponseEntity<String> getTimeZone() {
        return ResponseEntity.ok("Timezone atual: " + TimeZone.getDefault().getID());
    }
}
