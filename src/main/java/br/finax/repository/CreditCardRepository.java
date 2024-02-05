package br.finax.repository;

import br.finax.models.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {

    @Query(
        value =
            """
            SELECT
                *
            FROM
                credit_card
            WHERE
                user_id = :userId
            ORDER BY id
            """, nativeQuery = true
    )
    List<CreditCard> getAllByUser(Long userId);
}
