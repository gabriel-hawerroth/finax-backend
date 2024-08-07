package br.finax.repository;

import br.finax.dto.InterfacesSQL.CardBasicList;
import br.finax.dto.InterfacesSQL.UserCreditCards;
import br.finax.models.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {

    List<CreditCard> findAllByUserId(long userId);

    @Query(value = """
            SELECT
                cc.id,
                cc.user_id AS userId,
                cc.name,
                cc.card_limit AS cardLimit,
                cc.close_day AS closeDay,
                cc.expires_day AS expiresDay,
                cc.image,
                cc.standard_payment_account_id AS standardPaymentAccountId,
                cc.active,
                ac.name AS accountName,
                ac.image AS accountImage
            FROM
                credit_card cc
                JOIN account ac ON cc.standard_payment_account_id = ac.id
            WHERE
                cc.user_id = :userId
            ORDER BY
                cc.id
            """, nativeQuery = true)
    List<UserCreditCards> getAllByUser(long userId);

    @Query(value = """
            SELECT
                cc.id,
                cc.name,
                cc.image
            FROM
                credit_card cc
            WHERE
                cc.user_id = :userId
                AND cc.active = true
            ORDER BY
                cc.id
            """, nativeQuery = true)
    List<CardBasicList> getBasicList(long userId);

    @Query(value = """
            SELECT
                cc.user_id
            FROM
                credit_card cc
            WHERE
                cc.id = :id
            """, nativeQuery = true)
    long findUserIdById(long id);
}
