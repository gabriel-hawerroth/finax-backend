package br.finax.controllers;

import br.finax.dto.AuthenticationDTO;
import br.finax.dto.LoginResponseDTO;
import br.finax.models.User;
import br.finax.repository.UserRepository;
import br.finax.security.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository repository;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder bCrypt;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid AuthenticationDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
        Authentication auth = this.authenticationManager.authenticate(usernamePassword);

        String token = tokenService.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok(
                new LoginResponseDTO((User) auth.getPrincipal(), token)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody @Valid User data) {
        if (this.repository.findByEmail(data.getEmail()).isPresent())
            return ResponseEntity.badRequest().build();

        data.setPassword(bCrypt.encode(data.getPassword()));

        return ResponseEntity.ok().body(this.repository.save(data));
    }
}
