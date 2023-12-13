package br.finax.finax.repository;

import br.finax.finax.models.CashFlow;
import br.finax.finax.models.InterfacesSQL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CashFlowRepository extends JpaRepository<CashFlow, Long> {
    @Query(
        value =
            """
            SELECT
                cf.id,
                cf.description,
                cf.account_id as accountId,
                ba.name as accountName,
                cf.amount,
                cf.type,
                cf.done,
                cf.target_account_id as targetAccountId,
                tba.name as targetAccountName,
                cf.category_id as categoryId,
                c.name as categoryName,
                cf.date,
                cf.time,
                cf.observation
            FROM
                cash_flow cf
                join bank_accounts ba on cf.account_id = ba.id
                LEFT JOIN bank_accounts tba on cf.target_account_id  = tba.id
                LEFT JOIN category c on cf.category_id = c.id
            WHERE
                ba.user_id = :userId
                AND EXTRACT(year from cf.date) = :year
                AND EXTRACT(month from cf.date) = :month
            ORDER BY
                CAST(cf.date || ' ' || CASE WHEN cf.time <> '' THEN cf.time ELSE '23:59' END as timestamp) ASC
            """, nativeQuery = true
    )
    List<InterfacesSQL.MonthlyCashFlow> getCashFlow(Long userId, Integer year, Integer month);
}
