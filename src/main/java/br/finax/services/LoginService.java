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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

import static br.finax.utils.UtilsService.generateHash;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final EmailService emailService;

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final CategoryRepository categoryRepository;

    public ResponseEntity<Void> activateUser(Long userId, String token) {
        final String savedToken = tokenRepository.findByUserId(userId)
                .orElseThrow(NotFoundException::new).getToken();

        final User user = userRepository.findById(userId)
                .orElseThrow(NotFoundException::new);

        if (savedToken.equals(token)) {
            user.setActive(true);
            userRepository.save(user);
        }

        categoryRepository.insertNewUserCategories(userId);

        final URI uri = URI.create(UriComponentsBuilder
                .fromUriString("https://hawetec.com.br/finax/ativacao-da-conta").build().toUriString());

        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(uri)
                .build();
    }

    public void sendChangePasswordMail(String email) {
        final User user = userRepository.findByEmail(email)
                .orElseThrow(NotFoundException::new);

        final Optional<Token> tok = tokenRepository.findByUserId(user.getId());

        final Token token = new Token();
        tok.ifPresent(value -> token.setId(value.getId()));
        token.setUserId(user.getId());
        token.setToken(generateHash(user.getEmail()));
        tokenRepository.save(token);

        sendChangePasswordEmail(user.getEmail(), user.getId(), token.getToken());
    }

    public ResponseEntity<Void> permitChangePassword(Long userId, String token) {
        final String savedToken = tokenRepository.findByUserId(userId)
                .orElseThrow(NotFoundException::new).getToken();

        final User user = userRepository.findById(userId)
                .orElseThrow(NotFoundException::new);

        if (savedToken.equals(token)) {
            user.setCanChangePassword(true);
            userRepository.save(user);
        }

        final URI uri = URI.create(UriComponentsBuilder
                .fromUriString("https://hawetec.com.br/finax/recuperacao-da-senha/" + user.getId()).build().toUriString());

        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(uri)
                .build();
    }

    private void sendChangePasswordEmail(String userMail, Long userId, String token) {
        try {
            emailService.sendMail(
                    new EmailDTO(
                            userMail,
                            "Alteração da senha Finax",
                            emailService.buildEmailTemplate(EmailType.CHANGE_PASSWORD, userId, token)
                    )
            );
        } catch (MessagingException messagingException) {
            throw new UnsendedEmailException();
        }
    }
}
