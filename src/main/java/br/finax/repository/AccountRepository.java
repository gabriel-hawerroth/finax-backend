package br.finax.repository;

import br.finax.dto.InterfacesSQL.BasicAccount;
import br.finax.models.Account;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findAllByUserIdOrderByIdAsc(long userId);

    @Query(value = """
            SELECT ac
            FROM Account ac
            WHERE
                ac.userId = :userId
                AND ac.active is true
            ORDER BY
                ac.id
            """)
    List<Account> findAllActiveByUserId(long userId);

    @Query(value = """
            SELECT
                ac.id AS id,
                ac.name AS name,
                ac.image AS image,
                ac.balance AS balance,
                ac.type AS type
            FROM
                account ac
            WHERE
                ac.user_id = :userId
                AND ac.active is true
                AND ac.grouper is false
                AND CASE WHEN :showSubAccounts
                    THEN ac.primary_account_id is null
                    ELSE true
                END
            ORDER BY
                ac.id
            """, nativeQuery = true)
    List<BasicAccount> getBasicList(long userId, boolean showSubAccounts);

    @Transactional
    @Modifying
    @Query("""
                    DELETE FROM Account ac
                    WHERE ac.primaryAccountId = :primaryAccountId
            """)
    void deleteSubAccounts(long primaryAccountId) throws DataIntegrityViolationException;

    @Modifying
    @Query("""
                    UPDATE Account ac
                    SET ac.active = false
                    WHERE
                        ac.id = :id
                        OR ac.primaryAccountId = :id
            """)
    void inactivateAccount(long id);

    @Modifying
    @Query("""
                    UPDATE Account ac
                    SET ac.active = true
                    WHERE
                        ac.id in :ids
            """)
    void activateAccounts(List<Long> ids);
}
