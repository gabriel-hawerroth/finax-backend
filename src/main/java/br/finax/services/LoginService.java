package br.finax.services;

import br.finax.dto.EmailDTO;
import br.finax.enums.EmailType;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.Token;
import br.finax.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
