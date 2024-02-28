package br.finax.repository;

import br.finax.models.CashFlow;
import br.finax.utils.InterfacesSQL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface CashFlowRepository extends JpaRepository<CashFlow, Long> {
    @Query(value =
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
                cf.attachment_name AS attachmentName,
                cf.duplicated_release_id AS duplicatedReleaseId,
                (CASE WHEN
                        EXISTS (SELECT 1 FROM cash_flow WHERE duplicated_release_id = cf.id)
                            OR
                        cf.duplicated_release_id IS NOT NULL
                    THEN true
                    ELSE false
                END) AS isDuplicatedRelease
            FROM
                cash_flow cf
                LEFT JOIN bank_account ba ON cf.account_id = ba.id
                LEFT JOIN bank_account tba ON cf.target_account_id  = tba.id
                LEFT JOIN category c ON cf.category_id = c.id
            WHERE
                cf.user_id = :userId
                AND cf.date between :firstDt and :lastDt
            ORDER BY
                cf.date, cf.time, cf.id ASC
            """, nativeQuery = true)
    List<InterfacesSQL.MonthlyReleases> getMonthlyReleases(long userId, Date firstDt, Date lastDt);

    @Query(value =
            """
            SELECT
                COALESCE(SUM(CASE WHEN cf.type = 'R' THEN cf.amount ELSE 0 END), 0) AS revenues,
                COALESCE(SUM(CASE WHEN cf.type = 'E' THEN cf.amount ELSE 0 END), 0) AS expenses,
                COALESCE(SUM(CASE WHEN cf.type = 'R' THEN cf.amount ELSE 0 END), 0)
                    - COALESCE(SUM(CASE WHEN cf.type = 'E' THEN cf.amount ELSE 0 END), 0) AS balance,
                (
                    SELECT
                        COALESCE(SUM(ba.balance), 0)
                    FROM
                        bank_account ba
                    WHERE
                        ba.user_id = :userId
                        AND ba.active = true
                        AND ba.add_overall_balance = true
                ) AS generalBalance,
                (
                   SELECT
                        COALESCE(
                           (SELECT SUM(ba.balance) FROM bank_account ba WHERE ba.user_id = :userId AND ba.active = true AND ba.add_overall_balance = true)
                           + COALESCE(SUM(CASE WHEN cf.type = 'R' THEN cf.amount ELSE 0 END), 0)
                           - COALESCE(SUM(CASE WHEN cf.type = 'E' THEN cf.amount ELSE 0 END), 0)
                        , 0)
                   FROM
                       cash_flow cf
                   WHERE
                       cf.user_id = :userId
                       AND cf.done = false
                       AND cf.date between :firstDtCurrentMonth AND :lastDt
                    ) AS expectedBalance
            FROM
                cash_flow cf
            WHERE
                cf.user_id = :userId
                AND cf.done = true
                AND cf.date between :firstDt and :lastDt
            """, nativeQuery = true)
    InterfacesSQL.MonthlyBalance getMonthlyBalance(long userId, Date firstDt, Date lastDt, Date firstDtCurrentMonth);

    @Query(value =
            """
            SELECT
                COALESCE(SUM(CASE WHEN cf.type = 'R' THEN cf.amount ELSE 0 END), 0) AS revenues,
                COALESCE(SUM(CASE WHEN cf.type = 'E' THEN cf.amount ELSE 0 END), 0) AS expenses
            FROM
                cash_flow cf
            WHERE
                cf.user_id = :userId
                AND cf.done = true
                AND cf.date between :firstDt and :lastDt
            LIMIT 1
            """, nativeQuery = true)
    InterfacesSQL.HomeBalances getHomeBalances(long userId, Date firstDt, Date lastDt);

    @Query(value =
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
                cf.attachment_name AS attachmentName,
                cf.duplicated_release_id AS duplicatedReleaseId,
                false AS isDuplicatedRelease
            FROM
                cash_flow cf
                LEFT JOIN bank_account ba ON cf.account_id = ba.id
                LEFT JOIN bank_account tba ON cf.target_account_id  = tba.id
                LEFT JOIN category c ON cf.category_id = c.id
            WHERE
                ba.user_id = :userId
                AND cf.date between current_date AND (current_date + interval '1 month')
                AND cf.done = false
            ORDER BY
                cf.date, cf.time, cf.id ASC
            """, nativeQuery = true)
    List<InterfacesSQL.MonthlyReleases> getUpcomingReleasesExpected(long userId);

    @Query(value =
            """
            SELECT
                *
            FROM
                cash_flow cf
            WHERE
                cf.duplicated_release_id = :duplicatedReleaseId
                AND cf.date > :date
            ORDER BY cf.id
            """, nativeQuery = true)
    List<CashFlow> getNextDuplicatedReleases(long duplicatedReleaseId, LocalDate date);

    @Query(value =
            """
            SELECT
                *
            FROM
                cash_flow cf
            WHERE
                cf.duplicated_release_id = :duplicatedReleaseId
                OR cf.id = :duplicatedReleaseId
            ORDER BY cf.id
            """, nativeQuery = true)
    List<CashFlow> getAllDuplicatedReleases(long duplicatedReleaseId);
}
