package br.finax.services;

import br.finax.exceptions.NotFoundException;
import br.finax.models.Token;
import br.finax.models.User;
import br.finax.repository.TokenRepository;
import br.finax.utils.UtilsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserTokenService {

    private final TokenRepository tokenRepository;

    private final UtilsService utils;

    public Token generateToken(final User user) {
        final Optional<Token> existentToken = tokenRepository.findByUserId(user.getId());

        final Token token;

        if (existentToken.isPresent()) {
            token = existentToken.get();
        } else {
            token = new Token();
            token.setUserId(user.getId());
        }

        token.setToken(utils.generateHash(user.getEmail()));

        return tokenRepository.save(token);
    }

    public Token findByUserId(Long userId) {
        return tokenRepository.findByUserId(userId)
                .orElseThrow(NotFoundException::new);
    }
}
