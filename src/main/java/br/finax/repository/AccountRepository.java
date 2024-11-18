package br.finax.repository;

import br.finax.dto.InterfacesSQL.BasicAccount;
import br.finax.dto.InterfacesSQL.HomeAccount;
import br.finax.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findAllByUserIdOrderByIdAsc(long userId);

    @Query(value = """
            SELECT
                ac.name AS name,
                ac.image AS image,
                ac.balance AS balance
            FROM
                Account ac
            WHERE
                ac.userId = :userId
                AND ac.active is true
                AND ac.addOverallBalance is true
                AND ac.archived is false
            ORDER BY
                ac.id
            """)
    List<HomeAccount> getHomeAccountsList(long userId);

    @Query(value = """
            SELECT
                ac.id AS id,
                ac.name AS name,
                ac.image AS image,
                ac.balance AS balance
            FROM
                Account ac
            WHERE
                ac.userId = :userId
                AND ac.active is true
                AND ac.archived is false
            ORDER BY
                ac.id
            """)
    List<BasicAccount> getBasicList(long userId);
}
