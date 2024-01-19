package br.finax.repository;

import br.finax.models.CashFlow;
import br.finax.models.InterfacesSQL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface CashFlowRepository extends JpaRepository<CashFlow, Long> {
    @Query(
        value =
            """
            SELECT
                cf.id,
                cf.user_id AS userId,
                cf.description,
                cf.account_id AS accountId,
                ba.name AS accountName,
                cf.amount,
                cf.type,
                cf.done,
                cf.target_account_id AS targetAccountId,
                tba.name AS targetAccountName,
                cf.category_id AS categoryId,
                c.name AS categoryName,
                c.color AS categoryColor,
                c.icon AS categoryIcon,
                cf.date,
                cf.time,
                cf.observation,
                cf.attachment,
                cf.attachment_name AS attachmentName,
                cf.duplicated_release_id AS duplicatedReleaseId,
                (CASE WHEN
                        (SELECT COUNT(1) FROM cash_flow WHERE duplicated_release_id = cf.id) > 0
                            OR
                        cf.duplicated_release_id IS NOT NULL
                    THEN true
                    ELSE false
                END) AS isDuplicatedRelease
            FROM
                cash_flow cf
                JOIN bank_accounts ba ON cf.account_id = ba.id
                LEFT JOIN bank_accounts tba ON cf.target_account_id  = tba.id
                LEFT JOIN category c ON cf.category_id = c.id
            WHERE
                ba.user_id = :userId
                AND TO_CHAR(cf.date, 'MM/yyyy') = TO_CHAR(CAST(:date AS DATE), 'MM/yyyy')
            ORDER BY
                CAST(cf.date || ' ' || CASE WHEN cf.time <> '' THEN cf.time ELSE '23:59' END as timestamp), cf.id ASC
            """, nativeQuery = true
    )
    List<InterfacesSQL.MonthlyReleases> getCashFlow(Long userId, LocalDate date);

    @Query(
        value =
            """
            SELECT
                COALESCE(SUM(CASE WHEN cf.type = 'R' THEN cf.amount ELSE 0 END), 0) AS revenues,
                COALESCE(SUM(CASE WHEN cf.type = 'E' THEN cf.amount ELSE 0 END), 0) AS expenses,
                COALESCE(SUM(CASE WHEN cf.type = 'R' THEN cf.amount ELSE 0 END), 0)
                    - COALESCE(SUM(CASE WHEN cf.type = 'E' THEN cf.amount ELSE 0 END), 0) AS balance,
                (
                    SELECT
                        SUM(ba.balance)
                    FROM
                        bank_accounts ba
                    WHERE
                        ba.user_id = :userId
                        AND ba.active = true
                        AND ba.add_overall_balance = true
                ) AS generalBalance,
                (
                   SELECT
                       ((SELECT SUM(ba.balance) FROM bank_accounts ba WHERE ba.user_id = :userId AND ba.active = true AND ba.add_overall_balance = true) +
                       COALESCE(SUM(CASE WHEN cf.type = 'R' THEN COALESCE(cf.amount, 0) ELSE 0 END), 0)) - COALESCE(SUM(CASE WHEN cf.type = 'E' THEN COALESCE(cf.amount, 0) ELSE 0 END), 0) AS expectedBalance
                   FROM
                       cash_flow cf
                   WHERE
                       cf.user_id = :userId
                       AND cf.done = false
                       AND (cf.date between DATE_TRUNC('MONTH', current_date) AND (DATE_TRUNC('MONTH', CAST(:date AS DATE)) + INTERVAL '1 month - 1 day'))
                ) AS expectedBalance
            FROM
                cash_flow cf
            WHERE
                cf.user_id = :userId
                AND cf.done = true
                AND TO_CHAR(cf.date, 'MM/yyyy') = TO_CHAR(CAST(:date AS DATE), 'MM/yyyy')
            """, nativeQuery = true
    )
    List<InterfacesSQL.MonthlyBalance> getMonthlyBalance(Long userId, LocalDate date);

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
                AND cf.date between current_date AND (current_date + interval '1 month')
                AND cf.done = false
            ORDER BY
                CAST(cf.date || ' ' || CASE WHEN cf.time <> '' THEN cf.time ELSE '23:59' END as timestamp), cf.id ASC
            """, nativeQuery = true
    )
    List<InterfacesSQL.MonthlyReleases> getUpcomingReleasesExpected(Long userId);

    @Query(
        value =
            """
            SELECT
                *
            FROM
                cash_flow cf
            WHERE
                cf.duplicated_release_id = :duplicatedReleaseId
                AND cf.date > :date
            ORDER BY cf.id
            """, nativeQuery = true
    )
    List<CashFlow> getNextDuplicatedReleases(Long duplicatedReleaseId, LocalDate date);

    @Query(
            value =
                    """
                    SELECT
                        *
                    FROM
                        cash_flow cf
                    WHERE
                        cf.duplicated_release_id = :duplicatedReleaseId
                        OR cf.id = :duplicatedReleaseId
                    ORDER BY cf.id
                    """, nativeQuery = true
    )
    List<CashFlow> getAllDuplicatedReleases(Long duplicatedReleaseId);
}
