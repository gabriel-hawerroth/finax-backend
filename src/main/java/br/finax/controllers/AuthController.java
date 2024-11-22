package br.finax.controllers;

import br.finax.dto.AuthenticationDTO;
import br.finax.dto.LoginResponseDTO;
import br.finax.models.User;
import br.finax.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.TimeZone;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid AuthenticationDTO authDTO) {
        return ResponseEntity.ok(
                authService.doLogin(authDTO)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody @Valid User user) {
        return ResponseEntity.ok(
                authService.registerNewUser(user)
        );
    }

    @GetMapping("/timezone")
    public ResponseEntity<String> getTimeZone() {
        return ResponseEntity.ok("Timezone atual: " + TimeZone.getDefault().getID());
    }
}
