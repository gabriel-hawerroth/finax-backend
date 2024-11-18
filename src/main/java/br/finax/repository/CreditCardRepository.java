package br.finax.repository;

import br.finax.dto.InterfacesSQL.BasicCard;
import br.finax.dto.InterfacesSQL.UserCreditCard;
import br.finax.models.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {

    List<CreditCard> findAllByUserId(long userId);

    @Query(value = """
            SELECT
                cc.id AS id,
                cc.userId AS userId,
                cc.name AS name,
                cc.cardLimit AS cardLimit,
                cc.closeDay AS closeDay,
                cc.expiresDay AS expiresDay,
                cc.image AS image,
                cc.standardPaymentAccountId AS standardPaymentAccountId,
                cc.active AS active,
                ac.name AS accountName,
                ac.image AS accountImage
            FROM
                CreditCard cc
                JOIN Account ac ON cc.standardPaymentAccountId = ac.id
            WHERE
                cc.userId = :userId
            ORDER BY
                cc.id
            """)
    List<UserCreditCard> getAllByUser(long userId);

    @Query(value = """
            SELECT
                cc.id AS id,
                cc.name AS name,
                cc.image AS image
            FROM
                CreditCard cc
            WHERE
                cc.userId = :userId
                AND cc.active = true
            ORDER BY
                cc.id
            """)
    List<BasicCard> getBasicList(long userId);

    @Query(value = """
            SELECT
                cc.userId AS userId
            FROM
                CreditCard cc
            WHERE
                cc.id = :id
            """)
    long findUserIdById(long id);
}
