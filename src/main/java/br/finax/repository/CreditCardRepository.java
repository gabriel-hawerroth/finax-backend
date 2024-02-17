package br.finax.repository;

import br.finax.models.CreditCard;
import br.finax.utils.InterfacesSQL.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {

    @Query(
        value =
            """
            SELECT
                cc.*,
                ba.name AS account_name,
                ba.image AS account_image
            FROM
                credit_card cc
                JOIN bank_accounts ba ON cc.standard_payment_account_id = ba.id
            WHERE
                cc.user_id = :userId
            ORDER BY
                cc.id
            """, nativeQuery = true
    )
    List<UserCreditCards> getAllByUser(Long userId);

    @Query(
        value =
            """
            SELECT
                cc.id,
                cc.name,
                cc.image
            FROM
                credit_card cc
            WHERE
                cc.user_id = :user_id
                AND cc.active = true
            ORDER BY
                cc.id
            """, nativeQuery = true
    )
    List<CardBasicList> getBasicList(Long user_id);
}
