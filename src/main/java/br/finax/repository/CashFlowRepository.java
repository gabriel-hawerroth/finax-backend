package br.finax.repository;

import br.finax.models.CashFlow;
import br.finax.models.InterfacesSQL;
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
                c.color as categoryColor,
                c.icon as categoryIcon,
                cf.date,
                cf.time,
                cf.observation,
                cf.attachment,
                cf.attachment_name as attachmentName
            FROM
                cash_flow cf
                JOIN bank_accounts ba ON cf.account_id = ba.id
                LEFT JOIN bank_accounts tba ON cf.target_account_id  = tba.id
                LEFT JOIN category c ON cf.category_id = c.id
            WHERE
                ba.user_id = :userId
                AND EXTRACT(year from cf.date) = :year
                AND EXTRACT(month from cf.date) = :month
            ORDER BY
                CAST(cf.date || ' ' || CASE WHEN cf.time <> '' THEN cf.time ELSE '23:59' END as timestamp), cf.id ASC
            """, nativeQuery = true
    )
    List<InterfacesSQL.MonthlyCashFlow> getCashFlow(Long userId, Integer year, Integer month);

    @Query(
        value =
            """
            SELECT
                COALESCE(SUM(CASE WHEN cf.type = 'R' THEN cf.amount ELSE 0 END), 0) AS revenues,
                COALESCE(SUM(CASE WHEN cf.type = 'E' THEN cf.amount ELSE 0 END), 0) AS expenses
            FROM
                cash_flow cf
                JOIN bank_accounts ba ON cf.account_id = ba.id
            WHERE
                ba.user_id = :userId
                AND cf.done = true
                AND cf.category_id <> 21
            """, nativeQuery = true
    )
    List<InterfacesSQL.MonthlyValues> getMonthlyFlow(Long userId);

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
                c.color as categoryColor,
                c.icon as categoryIcon,
                cf.date,
                cf.time,
                cf.observation
            FROM
                cash_flow cf
                JOIN bank_accounts ba ON cf.account_id = ba.id
                LEFT JOIN bank_accounts tba ON cf.target_account_id  = tba.id
                LEFT JOIN category c ON cf.category_id = c.id
            WHERE
                ba.user_id = :userId
                AND cf.date between current_date AND (current_date + interval '3 months')
                AND cf.done = false
            ORDER BY
                CAST(cf.date || ' ' || CASE WHEN cf.time <> '' THEN cf.time ELSE '23:59' END as timestamp), cf.id ASC
            """, nativeQuery = true
    )
    List<InterfacesSQL.MonthlyCashFlow> getUpcomingReleasesExpected(Long userId);
}
