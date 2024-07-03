package br.finax.repository;

import br.finax.dto.InterfacesSQL;
import br.finax.models.CashFlow;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface CashFlowRepository extends JpaRepository<CashFlow, Long> {

    @Query(value = """
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
                cf.attachment,
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
                cf.date, cf.time, cf.id
            """, nativeQuery = true)
    List<InterfacesSQL.MonthlyReleases> getMonthlyReleases(long userId, @NonNull LocalDate firstDt, @NonNull LocalDate lastDt);

    @Query(value = """
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
    InterfacesSQL.HomeBalances getHomeBalances(long userId, @NonNull Date firstDt, @NonNull Date lastDt);

    @Query(value = """
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
                cf.date, cf.time, cf.id
            """, nativeQuery = true)
    List<InterfacesSQL.MonthlyReleases> getUpcomingReleasesExpected(long userId);

    @Query(value = """
            SELECT
                *
            FROM
                cash_flow cf
            WHERE
                cf.duplicated_release_id = :duplicatedReleaseId
                AND cf.date > :date
            ORDER BY cf.id
            """, nativeQuery = true)
    List<CashFlow> getNextDuplicatedReleases(long duplicatedReleaseId, @NonNull LocalDate date);

    @Query(value = """
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

    @Query(value = """
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
                cf.user_id = :userId
                AND cf.credit_card_id = :creditCardId
                AND cf.date BETWEEN :firstDt AND :lastDt
            ORDER BY
                cf.date, cf.time, cf.id
            """, nativeQuery = true)
    List<InterfacesSQL.MonthlyReleases> getByInvoice(long userId, long creditCardId, @NonNull Date firstDt, @NonNull Date lastDt);
    // ajustar para buscar entre as datas de fechamento do cartão

    @Query(value = """
            SELECT
                *
            FROM
                cash_flow cf
            WHERE
                cf.user_id = :userId
                AND cf.date between :firstDt AND :lastDt
                AND cf.type = 'E'
                AND cf.done IS TRUE
                AND cf.category_id <> 1
            """, nativeQuery = true)
    List<CashFlow> findReleasesForHomeSpendsCategory(
            long userId, @NonNull LocalDate firstDt, @NonNull LocalDate lastDt
    );
}
