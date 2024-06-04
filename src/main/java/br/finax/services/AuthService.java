package br.finax.services;

import br.finax.dto.AuthenticationDTO;
import br.finax.dto.EmailDTO;
import br.finax.dto.LoginResponseDTO;
import br.finax.enums.EmailType;
import br.finax.exceptions.BadCredentialsException;
import br.finax.exceptions.EmailAlreadyExistsException;
import br.finax.exceptions.UnsendedEmailException;
import br.finax.models.AccessLog;
import br.finax.models.Token;
import br.finax.models.User;
import br.finax.security.TokenService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    private final AccessLogService accessLogService;
    private final UserTokenService userTokenService;
    private final TokenService tokenService;
    private final EmailService emailService;

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDTO doLogin(AuthenticationDTO authDTO) {
        final var usernamePassword = new UsernamePasswordAuthenticationToken(authDTO.login(), authDTO.password());

        final Authentication auth;
        try {
            auth = authenticationManager.authenticate(usernamePassword);
        } catch (org.springframework.security.authentication.BadCredentialsException ex) {
            throw new BadCredentialsException("Bad credentials");
        } catch (DisabledException ex) {
            throw new BadCredentialsException("Inactive user");
        }

        final User user = (User) auth.getPrincipal();

        final String token = tokenService.generateToken(user);

        if (user.getId() != 1) {
            accessLogService.save(
                    new AccessLog(user.getId(), LocalDateTime.now())
            );
        }

        return new LoginResponseDTO(user, token);
    }

    public User registerNewUser(User user) {
        if (userService.findByEmail(user.getEmail()) != null)
            throw new EmailAlreadyExistsException();

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(false);
        user.setAccess("premium");
        user.setCanChangePassword(false);
        user.setSignature("month");

        final Token token = userTokenService.generateToken(user);

        sendActivateAccountEmail(user.getEmail(), user.getId(), token.getToken());

        return userService.save(user);
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
