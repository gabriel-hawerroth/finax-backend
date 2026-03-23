package br.finax.repository;

import br.finax.models.PasswordRecoveryToken;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface PasswordRecoveryTokenRepository extends JpaRepository<PasswordRecoveryToken, Long> {

    Optional<PasswordRecoveryToken> findByTokenHash(@NotNull String tokenHash);

    @Modifying
    @Query("""
            UPDATE PasswordRecoveryToken t
            SET t.usedAt = :usedAt
            WHERE t.user.id = :userId
                AND t.usedAt IS NULL
                AND t.expiresAt > :now
            """)
    int invalidateActiveTokens(
            @Param("userId") long userId,
            @Param("usedAt") Instant usedAt,
            @Param("now") Instant now
    );

    @Modifying
    @Query("""
            UPDATE PasswordRecoveryToken t
            SET t.usedAt = :usedAt
            WHERE t.id = :id
                AND t.usedAt IS NULL
                AND t.expiresAt > :now
            """)
    int markAsUsed(@Param("id") Long id, @Param("usedAt") Instant usedAt, @Param("now") Instant now);
}
