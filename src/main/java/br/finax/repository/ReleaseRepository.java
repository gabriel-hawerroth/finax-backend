package br.finax.repository;

import br.finax.dto.InterfacesSQL.HomeRevenueExpense;
import br.finax.dto.InterfacesSQL.HomeUpcomingRelease;
import br.finax.dto.InterfacesSQL.MonthlyRelease;
import br.finax.models.Release;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ReleaseRepository extends JpaRepository<Release, Long> {

    @Query(value = """
            SELECT
                rls.id,
                rls.user_id AS userId,
                rls.description,
                rls.account_id AS accountId,
                ac.name AS accountName,
                rls.credit_card_id AS cardId,
                cc.name AS cardName,
                cc.image AS cardImg,
                rls.amount,
                rls.type,
                rls.done,
                rls.target_account_id AS targetAccountId,
                ac2.name AS targetAccountName,
                rls.category_id AS categoryId,
                ctg.name AS categoryName,
                ctg.color AS categoryColor,
                ctg.icon AS categoryIcon,
                rls.date,
                rls.time,
                rls.observation,
                rls.attachment_s3_file_name AS attachmentS3FileName,
                rls.attachment_name AS attachmentName,
                rls.duplicated_release_id AS duplicatedReleaseId,
                (CASE WHEN
                        EXISTS (SELECT 1 FROM release WHERE duplicated_release_id = rls.id)
                            OR
                        rls.duplicated_release_id IS NOT NULL
                    THEN true
                    ELSE false
                END) AS isDuplicatedRelease,
                rls.is_balance_adjustment AS isBalanceAdjustment
            FROM
                release rls
                LEFT JOIN account ac ON rls.account_id = ac.id
                LEFT JOIN account ac2 ON rls.target_account_id  = ac2.id
                LEFT JOIN credit_card cc ON rls.credit_card_id = cc.id
                LEFT JOIN category ctg ON rls.category_id = ctg.id
            WHERE
                rls.user_id = :userId
                AND rls.date between :firstDt and :lastDt
            ORDER BY
                rls.date, rls.time, rls.id
            """, nativeQuery = true)
    List<MonthlyRelease> getMonthlyReleases(long userId, @NonNull LocalDate firstDt, @NonNull LocalDate lastDt);

    @Query(value = """
            SELECT
                COALESCE(SUM(CASE WHEN rls.type = 'R' THEN rls.amount ELSE 0 END), 0) AS revenues,
                COALESCE(SUM(CASE WHEN rls.type = 'E' THEN rls.amount ELSE 0 END), 0) AS expenses
            FROM
                release rls
            WHERE
                rls.user_id = :userId
                AND rls.done = true
                AND rls.date between :firstDt and :lastDt
                AND rls.is_balance_adjustment is false
            LIMIT 1
            """, nativeQuery = true)
    HomeRevenueExpense getHomeBalances(long userId, @NonNull LocalDate firstDt, @NonNull LocalDate lastDt);

    @Query(value = """
            SELECT
                ctg.color AS categoryColor,
                ctg.icon AS categoryIcon,
                ctg.name AS categoryName,
                rls.credit_card_id is not null AS isCreditCardRelease,
                rls.description,
                ac.name AS accountName,
                cc.name AS creditCardName,
                rls.date,
                rls.amount,
                rls.type
            FROM
                release rls
                LEFT JOIN account ac ON rls.account_id = ac.id
                LEFT JOIN category ctg ON rls.category_id = ctg.id
                LEFT JOIN public.credit_card cc on rls.credit_card_id = cc.id
            WHERE
                rls.user_id = :userId
                AND rls.date between :firstDt AND :lastDt
                AND rls.type <> 'T'
                AND rls.done is false
                AND rls.is_balance_adjustment is false
            ORDER BY
                rls.date, rls.time, rls.id
            """, nativeQuery = true)
    List<HomeUpcomingRelease> getPayableAndReceivableAccounts(long userId, LocalDate firstDt, LocalDate lastDt);

    @Query(value = """
            SELECT
                *
            FROM
                release rls
            WHERE
                rls.duplicated_release_id = :duplicatedReleaseId
                AND rls.date > :date
            ORDER BY rls.id
            """, nativeQuery = true)
    List<Release> getNextDuplicatedReleases(long duplicatedReleaseId, @NonNull LocalDate date);

    @Query(value = """
            SELECT
                *
            FROM
                release rls
            WHERE
                rls.duplicated_release_id = :duplicatedReleaseId
                OR rls.id = :duplicatedReleaseId
            ORDER BY rls.id
            """, nativeQuery = true)
    List<Release> getAllDuplicatedReleases(long duplicatedReleaseId);

    @Query(value = """
            SELECT
                rls.id,
                rls.user_id AS userId,
                rls.description,
                null AS accountId,
                null AS accountName,
                cc.id AS cardId,
                cc.name AS cardName,
                cc.image AS cardImg,
                rls.amount,
                rls.type,
                rls.done,
                '' AS targetAccountId,
                '' AS targetAccountName,
                rls.category_id AS categoryId,
                ctg.name AS categoryName,
                ctg.color AS categoryColor,
                ctg.icon AS categoryIcon,
                rls.date,
                rls.time,
                rls.observation,
                rls.attachment_s3_file_name AS attachmentS3FileName,
                rls.attachment_name AS attachmentName,
                rls.duplicated_release_id AS duplicatedReleaseId,
                (CASE WHEN
                        EXISTS (SELECT 1 FROM release WHERE duplicated_release_id = rls.id)
                            OR
                        rls.duplicated_release_id IS NOT NULL
                    THEN true
                    ELSE false
                END) AS isDuplicatedRelease,
                rls.is_balance_adjustment AS isBalanceAdjustment
            FROM
                release rls
                JOIN credit_card cc ON rls.credit_card_id = cc.id
                LEFT JOIN category ctg ON rls.category_id = ctg.id
            WHERE
                rls.user_id = :userId
                AND rls.credit_card_id = :creditCardId
                AND rls.date BETWEEN :firstDt AND :lastDt
            ORDER BY
                rls.date, rls.time, rls.id
            """, nativeQuery = true)
    List<MonthlyRelease> getByInvoice(long userId, long creditCardId, @NonNull LocalDate firstDt, @NonNull LocalDate lastDt);
    // ajustar para buscar entre as datas de fechamento do cartão

    @Query(value = """
            SELECT
                *
            FROM
                release rls
            WHERE
                rls.user_id = :userId
                AND rls.date between :firstDt AND :lastDt
                AND rls.type = 'E'
                AND rls.done is true
                AND rls.is_balance_adjustment is false
            """, nativeQuery = true)
    List<Release> findReleasesForHomeSpendsCategory(
            long userId, @NonNull LocalDate firstDt, @NonNull LocalDate lastDt
    );

    @Query(value = """
            SELECT
                coalesce(sum(r.amount), 0)
            FROM
                release r
            WHERE
                r.credit_card_id = :cardId
                AND r."date" between :invoiceFirstDay AND :invoiceLastDay
                AND r.done is true
            """, nativeQuery = true)
    BigDecimal getCardInvoiceAmount(long cardId, LocalDate invoiceFirstDay, LocalDate invoiceLastDay);

    @Query(value = """
            SELECT
                coalesce(sum(r.amount), 0)
            FROM
                release r
            WHERE
                r.credit_card_id = :cardId
                AND r."date" >= :firstDay
                AND r.done is true
            """, nativeQuery = true)
    BigDecimal getCardNextReleasesAmount(long cardId, LocalDate firstDay);
}
