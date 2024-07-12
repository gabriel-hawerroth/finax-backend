package br.finax.services;

import br.finax.dto.AuthenticationDTO;
import br.finax.dto.EmailDTO;
import br.finax.dto.LoginResponseDTO;
import br.finax.enums.EmailType;
import br.finax.enums.user.UserAccess;
import br.finax.enums.user.UserSignature;
import br.finax.exceptions.BadCredentialsException;
import br.finax.exceptions.EmailAlreadyExistsException;
import br.finax.models.AccessLog;
import br.finax.models.Token;
import br.finax.models.User;
import br.finax.security.SecurityFilter;
import br.finax.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    private final SecurityFilter securityFilter;
    private final AccessLogService accessLogService;
    private final UserTokenService userTokenService;
    private final TokenService tokenService;
    private final EmailService emailService;

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Transactional
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

        final User user = userService.findById(((User) auth.getPrincipal()).getId());
        securityFilter.updateCachedUser(user);

        final String token = tokenService.generateToken(user);

        Thread.ofVirtual().start(() -> saveAccessLog(user));

        return new LoginResponseDTO(user, token);
    }

    @Transactional
    public User registerNewUser(User user) {
        if (userService.existsByEmail(user.getEmail()))
            throw new EmailAlreadyExistsException();

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(false);
        user.setAccess(UserAccess.PREMIUM);
        user.setCanChangePassword(false);
        user.setSignature(UserSignature.MONTH);
        user.setCreatedAt(LocalDateTime.now());

        user = userService.save(user);

        final Token token = userTokenService.generateToken(user);

        sendActivateAccountEmail(user.getEmail(), user.getId(), token.getToken());

        return user;
    }

    private void saveAccessLog(User user) {
        if (user.getAccess().equals(UserAccess.ADM)) {
            return;
        }

        accessLogService.save(
                new AccessLog(user.getId(), LocalDateTime.now())
        );
    }

    private void sendActivateAccountEmail(String userMail, Long userId, String token) {
        emailService.sendMail(
                new EmailDTO(
                        userMail,
                        "Ativação da conta Finax",
                        emailService.buildEmailTemplate(EmailType.ACTIVATE_ACCOUNT, userId, token)
                )
        );
    }
}
