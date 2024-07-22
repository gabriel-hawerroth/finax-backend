package br.finax.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import br.finax.dto.EmailDTO;
import br.finax.enums.EmailType;
import br.finax.enums.ErrorCategory;
import br.finax.exceptions.ServiceException;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.Token;
import br.finax.models.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserConfigsService userConfigsService;
    private final UserTokenService userTokenService;
    private final CategoryService categoryService;
    private final EmailService emailService;
    private final UserService userService;

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
    public void sendChangePasswordMail(String email) {
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
    public void permitChangePassword(Long userId, String token) {
        final String savedToken = userTokenService.findByUserId(userId).getToken();

        final User user = userService.findById(userId);

        if (savedToken.equals(token)) {
            user.setCanChangePassword(true);
            userService.save(user);
        } else {
            throw new WithoutPermissionException();
        }
    }
}
