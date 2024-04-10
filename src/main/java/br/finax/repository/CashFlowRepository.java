package br.finax.repository;

import br.finax.models.CashFlow;
import br.finax.dto.InterfacesSQL;
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
                cf.credit_card_id AS cardId,
                cc.name AS cardName,
                cc.image AS cardImg,
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
                LEFT JOIN credit_card cc ON cf.credit_card_id = cc.id
                LEFT JOIN category c ON cf.category_id = c.id
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
                null AS accountId,
                null AS accountName,
                null AS accountImg,
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
                END) AS isDuplicatedRelease
            FROM
                cash_flow cf
                LEFT JOIN bank_account ba ON cf.account_id = ba.id
                LEFT JOIN bank_account tba ON cf.target_account_id = tba.id
                LEFT JOIN category c ON cf.category_id = c.id
            WHERE
                cf.user_id = :userId
                AND cf.date BETWEEN :firstDt AND :lastDt
                AND cf.account_id IS NOT NULL
            UNION ALL
            SELECT
                cf.invoice_id AS id,
                cf.user_id AS userId,
                i.month_year AS description,
                null AS accountId,
                null AS accountName,
                cc.id AS cardId,
                cc.name AS cardName,
                cc.image AS cardImg,
                SUM(cf.amount) AS amount,
                'I' AS type,
                TO_DATE(cc.expires_day || '/' || i.month_year, 'DD/MM/YYYY') AS date,
                false AS done,
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
                false AS isDuplicatedRelease
            FROM
                cash_flow cf
                JOIN invoice i ON cf.invoice_id = i.id
                JOIN credit_card cc ON i.credit_card_id = cc.id
            WHERE
                cf.user_id = :userId
                AND cf.date BETWEEN :firstDtInvoice AND :lastDtInvoice
                AND cf.invoice_id IS NOT NULL
                AND cf.done IS TRUE
            GROUP BY
                cf.invoice_id,
                cf.user_id,
                i.month_year,
                cc.id,
                cc.name,
                i.payment_account_id is not null,
                TO_DATE(cc.expires_day || '/' || i.month_year, 'DD/MM/YYYY')
            ORDER BY
                date, time, id
            """, nativeQuery = true)
    List<InterfacesSQL.MonthlyReleases> getMonthlyReleasesInvoiceMode(long userId, Date firstDt, Date lastDt, Date firstDtInvoice, Date lastDtInvoice);

    @Query(value =
            """
            SELECT
                (
                    (SELECT COALESCE(SUM(ba.balance), 0)
                    FROM bank_account ba
                    WHERE ba.user_id = :user_id
                    AND ba.active IS TRUE
                    AND ba.archived IS FALSE
                    AND ba.add_overall_balance IS TRUE) +
                    COALESCE(SUM(CASE WHEN cf.type = 'R' THEN cf.amount ELSE 0 END), 0) -
                    COALESCE(SUM(CASE WHEN cf.type = 'E' THEN cf.amount ELSE 0 END), 0) -
                    (SELECT
                        COALESCE(SUM(cf2.amount), 0) -
                        (SELECT COALESCE(SUM(ip.payment_amount), 0)
                        FROM invoice_payment ip
                        JOIN invoice i ON ip.invoice_id = i.id
                        WHERE i.user_id = :user_id
                        AND ip.payment_date < :last_dt) AS open_card_expenses
                    FROM cash_flow cf2
                    WHERE cf2.user_id = :user_id
                    AND cf2.done IS TRUE
                    AND cf2.date < :last_dt
                    AND cf2.invoice_id IS NOT NULL)
                ) AS expectedBalance
            FROM
                cash_flow cf
            WHERE
                cf.user_id = :user_id
                AND cf.date BETWEEN :first_dt AND :last_dt
                AND cf.done IS FALSE
                AND cf.account_id IS NOT NULL
            """, nativeQuery = true)
    double getExpectedBalance(long user_id, Date first_dt, Date last_dt);

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
                cf.credit_card_id AS cardId,
                cc.name AS cardName,
                cc.image AS cardImg,
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
                LEFT JOIN credit_card cc ON cf.credit_card_id = cc.id
                LEFT JOIN category c ON cf.category_id = c.id
            WHERE
                cf.user_id = :userId
                AND cf.date between current_date AND (current_date + interval '1 month')
                AND cf.type <> 'T'
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
                null AS accountId,
                null AS accountName,
                cc.id AS cardId,
                cc.name AS cardName,
                cc.image AS cardImg,
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
                END) AS isDuplicatedRelease
            FROM
                cash_flow cf
                JOIN credit_card cc ON cf.credit_card_id = cc.id
                LEFT JOIN category c ON cf.category_id = c.id
            WHERE
                cf.user_id = :user_id
                AND cf.credit_card_id = :credit_card_id
                AND cf.date BETWEEN :first_dt AND :last_dt
            ORDER BY
                cf.date, cf.time, cf.id
            """, nativeQuery = true)
    List<InterfacesSQL.MonthlyReleases> getByInvoice(long user_id, long credit_card_id, Date first_dt, Date last_dt);
    // ajustar para buscar entre as datas de fechamento do cartão

    List<CashFlow> findByUserIdAndDateBetweenAndTypeAndDone(long userId, LocalDate startDate, LocalDate endDate, String type, boolean done);
}
