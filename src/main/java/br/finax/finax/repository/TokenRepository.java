package br.finax.finax.repository;

import br.finax.finax.models.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Token findByUserId(Long userId);
}
