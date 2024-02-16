package br.finax.repository;

import br.finax.models.Account;
import br.finax.utils.InterfacesSQL.GenericIdDs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccountsRepository extends JpaRepository<Account, Long> {

    List<Account> findAllByUserIdOrderByIdAsc(Long userId);

    @Query(
        value =
            """
            SELECT
                SUM(ba.balance)
            FROM
                bank_accounts ba
            WHERE
                ba.user_id = :user_id
                AND ba.active = true
                AND ba.add_overall_balance = true
            """, nativeQuery = true
    )
    Double getCurrentBalance(Long user_id);

    @Query(
        value =
            """
            SELECT
                *
            FROM
                bank_accounts ba
            WHERE
                ba.user_id = :user_id
                AND ba.active = true
                AND ba.add_overall_balance = true
            ORDER BY
                ba.id
            """, nativeQuery = true
    )
    List<Account> getHomeAccountsList(Long user_id);

    @Query(
        value =
            """
            SELECT
                ba.id,
                ba.name AS ds
            FROM
                bank_accounts ba
            WHERE
                ba.user_id = :user_id
                AND ba.active = true
            ORDER BY
                ba.id
            """, nativeQuery = true
    )
    List<GenericIdDs> getBasicList(Long user_id);
}
