package br.finax.repository;

import br.finax.dto.InterfacesSQL.AccountBasicList;
import br.finax.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findAllByUserIdOrderByIdAsc(long userId);

    @Query(value = """
            SELECT
                *
            FROM
                account ac
            WHERE
                ac.user_id = :userId
                AND ac.active is true
                AND ac.add_overall_balance is true
            ORDER BY
                ac.id
            """, nativeQuery = true)
    List<Account> getHomeAccountsList(long userId);

    @Query(value = """
            SELECT
                ac.id,
                ac.name,
                ac.image,
                ac.balance
            FROM
                account ac
            WHERE
                ac.user_id = :userId
                AND ac.active is true
            ORDER BY
                ac.id
            """, nativeQuery = true)
    List<AccountBasicList> getBasicList(long userId);
}
