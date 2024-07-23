package br.finax.services;

import br.finax.dto.EmailDTO;
import br.finax.enums.EmailType;
import br.finax.enums.ErrorCategory;
import br.finax.exceptions.ServiceException;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.Token;
import br.finax.models.User;
import br.finax.security.TokenService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserConfigsService userConfigsService;
    private final UserTokenService userTokenService;
    private final CategoryService categoryService;
    private final EmailService emailService;
    private final UserService userService;
    private final TokenService tokenService;

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Transactional
    public void activateUser(Long userId, String token) {
        final String savedToken = userTokenService.findByUserId(userId).getToken();

        final User user = userService.findById(userId);

        if (savedToken.equals(token)) {
            userService.activeUser(user.getId());
        }

        Thread.ofVirtual().start(() -> {
            categoryService.insertNewUserCategories(userId);
            userConfigsService.insertNewUserConfigs(userId);
        });
    }

    @Transactional
    public void sendChangePasswordEmail(String email) {
        email = HtmlUtils.htmlEscape(email);

        final boolean isValidEmail = emailService.verifyEmail(email);
        if (!isValidEmail)
            throw new ServiceException(ErrorCategory.BAD_REQUEST, "Invalid email");

        final User user = userService.findByEmail(email);

        final Token token = userTokenService.generateToken(user);

        emailService.sendMail(
                new EmailDTO(
                        email,
                        "Alteração da senha Finax",
                        emailService.buildEmailTemplate(EmailType.CHANGE_PASSWORD, user.getId(), token.getToken())
                )
        );
    }

    @Transactional
    public void permitChangePassword(long userId, String token) {
        final String savedToken = userTokenService.findByUserId(userId).getToken();

        final User user = userService.findById(userId);

        if (savedToken.equals(token)) {
            user.setCanChangePassword(true);
            userService.save(user);
        } else {
            throw new WithoutPermissionException();
        }
    }

    @Transactional
    public void sendCancelUserAccountEmail(long userId) {
        final User user = userService.findById(userId);

        final String token = tokenService.generateToken(user);

        emailService.sendMail(
                new EmailDTO(
                        user.getEmail(),
                        "Confirmação de cancelamento da conta",
                        emailService.buildCancelAccountEmailTemplate(userId, token)
                )
        );
    }

    @Transactional
    public void cancelUserAccount(long userId, @NonNull String token) {
        final String userMail = tokenService.validateToken(token);

        final User user = userService.findById(userId);

        if (userMail == null || !userMail.equals(user.getEmail()))
            throw new ServiceException(ErrorCategory.BAD_REQUEST, "Invalid token");

        logger.info("Conta cancelada: " + user.getEmail());
    }
}
