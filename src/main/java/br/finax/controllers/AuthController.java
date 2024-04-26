package br.finax.controllers;

import br.finax.dto.AuthenticationDTO;
import br.finax.dto.EmailDTO;
import br.finax.dto.LoginResponseDTO;
import br.finax.enums.EmailType;
import br.finax.exceptions.BadCredentialsException;
import br.finax.exceptions.UnsendedEmailException;
import br.finax.models.Token;
import br.finax.models.User;
import br.finax.repository.TokenRepository;
import br.finax.repository.UserRepository;
import br.finax.security.TokenService;
import br.finax.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static br.finax.utils.UtilsService.generateHash;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder bCrypt;

    private final EmailService emailService;
    private final TokenService tokenService;

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid AuthenticationDTO data) {
        final var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());

        final Authentication auth;
        try {
            auth = authenticationManager.authenticate(usernamePassword);
        } catch (org.springframework.security.authentication.BadCredentialsException ex) {
            throw new BadCredentialsException("Bad credentials");
        } catch (DisabledException ex) {
            throw new BadCredentialsException("Inactive user");
        }

        final String token = tokenService.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok(
                new LoginResponseDTO((User) auth.getPrincipal(), token)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody @Valid User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent())
            return ResponseEntity.status(406).build();

        user.setPassword(bCrypt.encode(user.getPassword()));
        user.setActive(false);
        user.setAccess("premium");
        user.setCanChangePassword(false);
        user.setSignature("month");

        final Token token = new Token();
        token.setUserId(user.getId());
        token.setToken(generateHash(user.getEmail()));
        tokenRepository.save(token);

        sendActivateAccountEmail(user.getEmail(), user.getId(), token.getToken());

        return ResponseEntity.ok().body(userRepository.save(user));
    }

    private void sendActivateAccountEmail(String userMail, Long userId, String token) {
        try {
            emailService.sendMail(
                    new EmailDTO(
                            userMail,
                            "Ativação da conta Finax",
                            emailService.buildEmailTemplate(EmailType.ACTIVATE_ACCOUNT, userId, token)
                    )
            );
        } catch (MessagingException messagingException) {
            throw new UnsendedEmailException();
        }
    }
}
