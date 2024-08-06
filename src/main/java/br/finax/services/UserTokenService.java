package br.finax.services;

import br.finax.exceptions.NotFoundException;
import br.finax.models.Token;
import br.finax.models.User;
import br.finax.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static br.finax.utils.UtilsService.generateHash;

@Service
@RequiredArgsConstructor
public class UserTokenService {

    private final TokenRepository tokenRepository;

    @Transactional
    public Token generateToken(final User user) {
        final Optional<Token> existentToken = tokenRepository.findByUserId(user.getId());

        final Token token;

        if (existentToken.isPresent()) {
            token = existentToken.get();
        } else {
            token = new Token();
            token.setUserId(user.getId());
        }

        token.setToken(generateHash(user.getEmail()));

        return tokenRepository.save(token);
    }

    @Transactional(readOnly = true)
    public Token findByUserId(Long userId) {
        return tokenRepository.findByUserId(userId)
                .orElseThrow(NotFoundException::new);
    }
}
