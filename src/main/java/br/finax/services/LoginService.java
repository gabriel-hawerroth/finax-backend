package br.finax.services;

import br.finax.dto.EmailDTO;
import br.finax.enums.EmailType;
import br.finax.exceptions.NotFoundException;
import br.finax.exceptions.UnsendedEmailException;
import br.finax.models.Token;
import br.finax.models.User;
import br.finax.repository.CategoryRepository;
import br.finax.repository.TokenRepository;
import br.finax.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final EmailService emailService;
    private final UserTokenService userTokenService;

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final CategoryRepository categoryRepository;

    public void activateUser(Long userId, String token) {
        final String savedToken = tokenRepository.findByUserId(userId)
                .orElseThrow(NotFoundException::new).getToken();

        final User user = userRepository.findById(userId)
                .orElseThrow(NotFoundException::new);

        if (savedToken.equals(token)) {
            user.setActive(true);
            userRepository.save(user);
        }

        categoryRepository.insertNewUserCategories(userId);
    }

    public void sendChangePasswordMail(String email) {
        final User user = userRepository.findByEmail(email)
                .orElseThrow(NotFoundException::new);

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
        final String savedToken = tokenRepository.findByUserId(userId)
                .orElseThrow(NotFoundException::new).getToken();

        final User user = userRepository.findById(userId)
                .orElseThrow(NotFoundException::new);

        if (savedToken.equals(token)) {
            user.setCanChangePassword(true);
            userRepository.save(user);
        }
    }
}
