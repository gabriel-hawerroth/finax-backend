package br.finax.services;

import br.finax.dto.EmailDTO;
import br.finax.enums.EmailType;
import br.finax.exceptions.UnsendedEmailException;
import br.finax.models.Token;
import br.finax.models.User;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final EmailService emailService;
    private final UserTokenService userTokenService;
    private final UserService userService;
    private final CategoryService categoryService;

    public void activateUser(Long userId, String token) {
        final String savedToken = userTokenService.findByUserId(userId).getToken();

        final User user = userService.findById(userId);

        if (savedToken.equals(token)) {
            user.setActive(true);
            userService.save(user);
        }

        categoryService.insertNewUserCategories(userId);
    }

    public void sendChangePasswordMail(String email) {
        final User user = userService.findByEmail(email);

        final Token token = userTokenService.generateToken(user);

        try {
            emailService.sendMail(
                    new EmailDTO(
                            email,
                            "Alteração da senha Finax",
                            emailService.buildEmailTemplate(EmailType.CHANGE_PASSWORD, user.getId(), token.getToken())
                    )
            );
        } catch (MessagingException messagingException) {
            throw new UnsendedEmailException();
        }
    }

    public void permitChangePassword(Long userId, String token) {
        final String savedToken = userTokenService.findByUserId(userId).getToken();

        final User user = userService.findById(userId);

        if (savedToken.equals(token)) {
            user.setCanChangePassword(true);
            userService.save(user);
        }
    }
}
