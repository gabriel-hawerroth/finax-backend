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
                CASE
                    WHEN cf.account_id IS NULL THEN cc.id
                    ELSE cf.account_id
                END AS accountId,
                CASE
                    WHEN cf.account_id IS NULL THEN cc.name
                    ELSE ba.name
                END AS accountName,
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
                END) AS isDuplicatedRelease,
                cf.invoice_id IS NOT NULL AS isCreditCardRelease
            FROM
                cash_flow cf
                LEFT JOIN bank_account ba ON cf.account_id = ba.id
                LEFT JOIN bank_account tba ON cf.target_account_id  = tba.id
                LEFT JOIN category c ON cf.category_id = c.id
                LEFT JOIN invoice i ON cf.invoice_id = i.id
                LEFT JOIN credit_card cc ON i.credit_card_id = cc.id
            WHERE
                cf.user_id = :userId
                AND cf.date between :firstDt and :lastDt
            ORDER BY
                cf.date, cf.time, cf.id asc
            """, nativeQuery = true)
    List<InterfacesSQL.MonthlyReleases> getMonthlyReleases(long userId, Date firstDt, Date lastDt);

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
                cf.date,
                cf.done,
                cf.target_account_id AS targetAccountId,
                tba.name AS targetAccountName,
                cf.category_id AS categoryId,
                c.name AS categoryName,
                c.color AS categoryColor,
                c.icon AS categoryIcon,
                cf.time,
                cf.observation,
                cf.attachment_name AS attachmentName,
                cf.duplicated_release_id AS duplicatedReleaseId,
                (CASE
                    WHEN EXISTS (SELECT 1 FROM cash_flow WHERE duplicated_release_id = cf.id)
                            OR
                        cf.duplicated_release_id IS NOT NULL
                    THEN true
                    ELSE false
                END) AS isDuplicatedRelease,
                false AS isCreditCardRelease
            FROM
                cash_flow cf
                LEFT JOIN bank_account ba ON cf.account_id = ba.id
                LEFT JOIN bank_account tba ON cf.target_account_id  = tba.id
                LEFT JOIN category c ON cf.category_id = c.id
            WHERE
                cf.user_id = :userId
                AND cf.date between :firstDt and :lastDt
                and cf.account_id is not null
            UNION ALL
            SELECT
                cf.invoice_id AS id,
                cf.user_id AS userId,
                i.month_year AS description,
                cc.id AS accountId,
                cc.name AS accountName,
                SUM(cf.amount) AS amount,
                'I' AS type,
                CASE
                    WHEN i.payment_date IS NOT NULL
                        THEN i.payment_date
                    ELSE TO_DATE(cc.expires_day || '/' || i.month_year, 'DD/MM/YYYY')
                END AS date,
                i.payment_account_id IS NOT NULL AS done,
                null AS targetAccountId,
                null AS targetAccountName,
                0 AS categoryId,
                '' AS categoryName,
                '' AS categoryColor,
                '' AS categoryIcon,
                '' AS time,
                '' AS observation,
                null AS attachmentName,
                null AS duplicatedReleaseId,
                false AS isDuplicatedRelease,
                false AS isCreditCardRelease
            FROM
                cash_flow cf
                JOIN invoice i ON cf.invoice_id = i.id
                JOIN credit_card cc ON i.credit_card_id = cc.id
            WHERE
                cf.user_id = :userId
                AND cf.date between :firstDtInvoice and :lastDtInvoice
                AND cf.invoice_id IS NOT NULL
                AND cf.done IS TRUE
            GROUP BY
                cf.invoice_id,
                cf.user_id,
                i.month_year,
                cc.id,
                cc.name,
                i.payment_account_id is not null,
                CASE
                    WHEN i.payment_date IS NOT NULL
                        THEN i.payment_date
                    ELSE TO_DATE(cc.expires_day || '/' || i.month_year, 'DD/MM/YYYY')
                END
            ORDER BY
                date, time, id ASC
            """, nativeQuery = true)
    List<InterfacesSQL.MonthlyReleases> getMonthlyReleasesInvoiceMode(long userId, Date firstDt, Date lastDt, Date firstDtInvoice, Date lastDtInvoice);

    @Query(value =
            """
            SELECT
                COALESCE(SUM(CASE WHEN cf.type = 'R' THEN cf.amount ELSE 0 END), 0) AS revenues,
                COALESCE(SUM(CASE WHEN cf.type = 'E' THEN cf.amount ELSE 0 END), 0) AS expenses,
                COALESCE(SUM(CASE WHEN cf.type = 'R' THEN cf.amount ELSE 0 END), 0)
                    - COALESCE(SUM(CASE WHEN cf.type = 'E' THEN cf.amount ELSE 0 END), 0) AS balance,
                0 AS generalBalance,
                0 AS expectedBalance
            FROM
                cash_flow cf
            WHERE
                cf.user_id = :userId
                AND cf.done = true
                AND cf.date between :firstDt and :lastDt
            """, nativeQuery = true)
    InterfacesSQL.MonthlyBalance getMonthlyBalance(long userId, Date firstDt, Date lastDt);

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
    InterfacesSQL.MonthlyBalance getMonthlyBalanceInvoiceMode(long userId, Date firstDt, Date lastDt, Date firstDtCurrentMonth);

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

    @Query(value =
            """
            SELECT
                cf.id,
                cf.user_id AS userId,
                cf.description,
                cc.id AS accountId,
                cc.name AS accountName,
                cf.amount,
                cf.type,
                cf.done,
                '' AS targetAccountId,
                '' AS targetAccountName,
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
                END) AS isDuplicatedRelease,
                true AS isCreditCardRelease
            FROM
                cash_flow cf
                JOIN invoice i ON cf.invoice_id = i.id
                JOIN credit_card cc ON i.credit_card_id = cc.id
                LEFT JOIN category c ON cf.category_id = c.id
            WHERE
                cf.invoice_id = :invoice_id
            ORDER BY
                cf.date, cf.time, cf.id asc
            """, nativeQuery = true)
    List<InterfacesSQL.MonthlyReleases> getByInvoice(long invoice_id);
}
