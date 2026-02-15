package br.finax.services;

import br.finax.dto.AuthenticationDTO;
import br.finax.dto.EmailDTO;
import br.finax.dto.LoginDTO;
import br.finax.dto.auth.GoogleAuthDTO;
import br.finax.enums.EmailType;
import br.finax.enums.ErrorCategory;
import br.finax.enums.user.AuthProvider;
import br.finax.enums.user.UserAccess;
import br.finax.enums.user.UserSignature;
import br.finax.events.user_created.UserCreatedEvent;
import br.finax.exceptions.BadCredentialsException;
import br.finax.exceptions.EmailAlreadyExistsException;
import br.finax.exceptions.ServiceException;
import br.finax.models.AccessLog;
import br.finax.models.User;
import br.finax.security.GoogleTokenVerifierService;
import br.finax.security.SecurityFilter;
import br.finax.security.TokenService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final CategoryService categoryService;
    private final UserConfigsService userConfigsService;

    private final SecurityFilter securityFilter;
    private final AccessLogService accessLogService;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final GoogleTokenVerifierService googleTokenVerifierService;

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public LoginDTO doLogin(AuthenticationDTO authDTO) {
        final var existingUser = userService.findByEmailOptional(authDTO.login()).orElse(null);
        if (existingUser != null && existingUser.getPassword() == null) {
            throw new BadCredentialsException("Use Google to sign in");
        }

        final var usernamePassword = new UsernamePasswordAuthenticationToken(authDTO.login(), authDTO.password());

        final Authentication auth;
        try {
            auth = authenticationManager.authenticate(usernamePassword);
        } catch (DisabledException ex) {
            throw new BadCredentialsException("Inactive user");
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Bad credentials");
        }

        final User user = userService.findById(((User) auth.getPrincipal()).getId());
        securityFilter.updateCachedUser(user);

        final String token = tokenService.generateToken(user);

        saveAccessLog(user);

        return new LoginDTO(user, token);
    }

    @Transactional
    public User registerNewUser(User user) {
        user.setEmail(HtmlUtils.htmlEscape(user.getEmail()));

        final boolean isValidEmail = emailService.verifyEmail(user.getEmail());
        if (!isValidEmail)
            throw new ServiceException(ErrorCategory.BAD_REQUEST, "Invalid email");

        if (userService.existsByEmail(user.getEmail()))
            throw new EmailAlreadyExistsException();

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(false);
        user.setAccess(UserAccess.PREMIUM);
        user.setCanChangePassword(false);
        user.setSignature(UserSignature.MONTH);
        user.setCreatedAt(LocalDateTime.now());

        user = userService.save(user);

        final String token = tokenService.generateToken(user);

        sendActivateAccountEmail(user.getEmail(), user, token);

        final var userCreatedEvent = new UserCreatedEvent(user);
        applicationEventPublisher.publishEvent(userCreatedEvent);

        return user;
    }

    @Transactional(readOnly = true)
    public void resendActivationEmail(String userMail) {
        final User user = userService.findByEmail(
                HtmlUtils.htmlEscape(userMail)
        );

        if (user.isActive())
            throw new ServiceException(ErrorCategory.BAD_REQUEST, "User already active");

        final String token = tokenService.generateToken(user);

        sendActivateAccountEmail(user.getEmail(), user, token);
    }

    @Transactional
    public LoginDTO doGoogleLogin(GoogleAuthDTO googleAuthDTO) {
        final GoogleIdToken.Payload payload = googleTokenVerifierService.verifyToken(googleAuthDTO.credential());

        if (payload == null) {
            throw new BadCredentialsException("Invalid Google token");
        }

        final String email = payload.getEmail();
        final String googleId = payload.getSubject();
        final String firstName = (String) payload.get("given_name");
        final String lastName = (String) payload.get("family_name");

        if (email == null || !Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new BadCredentialsException("Google account email not verified");
        }

        User user = userService.findByEmailOptional(email).orElse(null);

        if (user != null) {
            if (user.getProvider() == AuthProvider.LOCAL) {
                user.setProvider(AuthProvider.GOOGLE);
                user.setProviderId(googleId);
                user = userService.save(user);
            } else if (user.getProvider() == AuthProvider.GOOGLE && !user.getProviderId().equals(googleId)) {
                throw new BadCredentialsException("Google account does not match the registered account");
            }

            if (!user.isActive()) {
                userService.activeUser(user.getId());
                user.setActive(true);
                categoryService.insertNewUserCategories(user.getId());
                userConfigsService.insertUserConfigsIfNotExists(user.getId());
            }
        } else {
            user = new User();
            user.setEmail(email);
            user.setPassword(null);
            user.setFirstName(getFirstName(firstName, email));
            user.setLastName(lastName);
            user.setProvider(AuthProvider.GOOGLE);
            user.setProviderId(googleId);
            user.setActive(true);
            user.setAccess(UserAccess.PREMIUM);
            user.setCanChangePassword(false);
            user.setSignature(UserSignature.MONTH);
            user.setCreatedAt(LocalDateTime.now());

            user = userService.save(user);

            categoryService.insertNewUserCategories(user.getId());
            userConfigsService.insertUserConfigsIfNotExists(user.getId());
        }

        securityFilter.updateCachedUser(user);

        final String token = tokenService.generateToken(user);

        saveAccessLog(user);

        return new LoginDTO(user, token);
    }

    private static String getFirstName(String firstName, String email) {
        String resolvedFirstName = firstName;
        if (resolvedFirstName == null) {
            int atIndex = email.indexOf('@');
            if (atIndex > 0) {
                resolvedFirstName = email.substring(0, atIndex);
            } else {
                resolvedFirstName = email;
            }
        }
        return resolvedFirstName;
    }

    private void saveAccessLog(User user) {
        if (user.getAccess().equals(UserAccess.ADM))
            return;

        accessLogService.save(
                new AccessLog(user.getId(), LocalDateTime.now())
        );
    }

    private void sendActivateAccountEmail(String userMail, User user, String token) {
        final String mailContent = emailService.buildEmailContent(EmailType.ACTIVATE_ACCOUNT, user, token);

        final EmailDTO emailDTO = new EmailDTO(
                userMail,
                "Ativação da conta",
                emailService.buildEmailTemplate(mailContent)
        );

        emailService.sendMail(emailDTO);
    }
}
