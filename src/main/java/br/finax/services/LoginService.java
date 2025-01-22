package br.finax.services;

import br.finax.dto.EmailDTO;
import br.finax.enums.EmailType;
import br.finax.enums.ErrorCategory;
import br.finax.exceptions.ExpiredLinkException;
import br.finax.exceptions.ServiceException;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.Token;
import br.finax.models.User;
import br.finax.security.TokenService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class LoginService {

    private static final String DELETE_ALL_FROM_USER = """
                DELETE FROM user_configs WHERE user_id = :userId;
                DELETE FROM access_log WHERE user_id = :userId;
                DELETE FROM token WHERE user_id = :userId;
            
                DELETE FROM invoice_payment ip
                    WHERE credit_card_id in (
                        select credit_card_id from credit_card where user_id = :userId
                    );
            
                DELETE FROM release WHERE user_id = :userId;
            
                DELETE FROM category WHERE user_id = :userId;
                DELETE FROM credit_card WHERE user_id = :userId;
                DELETE FROM account WHERE user_id = :userId;
            
                DELETE FROM users WHERE id = :userId;
            """;

    private final UserConfigsService userConfigsService;
    private final UserTokenService userTokenService;
    private final CategoryService categoryService;
    private final EmailService emailService;
    private final UserService userService;
    private final TokenService tokenService;

    @PersistenceContext
    private final EntityManager entityManager;

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Transactional
    public void activateUser(Long userId, String token) {
        final User user = userService.findById(userId);
        final String userMail = tokenService.validateToken(token);

        if (!user.getEmail().equals(userMail))
            throw new ExpiredLinkException();

        userService.activeUser(user.getId());

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

        final String token = tokenService.generateToken(user);

        try {
            final String mailContent = emailService.buildEmailContent(EmailType.CHANGE_PASSWORD, user, token);

            emailService.sendMail(
                    new EmailDTO(
                            email,
                            "Alteração de senha",
                            emailService.buildEmailTemplate(mailContent)
                    )
            );
        } catch (Exception e) {
            throw new ServiceException(ErrorCategory.BAD_GATEWAY, "Error sending email");
        }
    }

    @Transactional
    public void permitChangePassword(long userId, String token) {
        final User user = userService.findById(userId);
        final String userMail = tokenService.validateToken(token);

        if (!user.getEmail().equals(userMail))
            throw new ExpiredLinkException();

        user.setCanChangePassword(true);
        userService.save(user);
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

        entityManager.createNativeQuery(DELETE_ALL_FROM_USER)
                .setParameter("userId", userId)
                .executeUpdate();

        logger.info("Conta cancelada:\nId - " + user.getId() + "\nEmail - " + user.getEmail());
    }
}
