package br.finax.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import br.finax.dto.InterfacesSQL.BasicCard;
import br.finax.dto.InterfacesSQL.UserCreditCard;
import br.finax.models.CreditCard;

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

    @Modifying
    @Query("""
            UPDATE CreditCard ac
            SET ac.active = false
            WHERE ac.id = :id
            """)
    void inactivateCard(long id);

    @Modifying
    @Query("""
            UPDATE CreditCard ac
            SET ac.active = true
            WHERE ac.id in :id
            """)
    void activateCard(long id);
}
