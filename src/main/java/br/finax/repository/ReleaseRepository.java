package br.finax.repository;

import br.finax.dto.InterfacesSQL.HomeRevenueExpense;
import br.finax.dto.InterfacesSQL.HomeUpcomingRelease;
import br.finax.dto.InterfacesSQL.MonthlyRelease;
import br.finax.models.Release;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ReleaseRepository extends JpaRepository<Release, Long> {

    @Query(value = """
            SELECT
                rls.id AS id,
                rls.userId AS userId,
                rls.description AS description,
                rls.accountId AS accountId,
                ac.name AS accountName,
                rls.creditCardId AS cardId,
                cc.name AS cardName,
                cc.image AS cardImg,
                rls.amount AS amount,
                rls.type AS type,
                rls.done AS done,
                rls.targetAccountId AS targetAccountId,
                ac2.name AS targetAccountName,
                rls.categoryId AS categoryId,
                ctg.name AS categoryName,
                ctg.color AS categoryColor,
                ctg.icon AS categoryIcon,
                rls.date AS date,
                rls.time AS time,
                rls.observation AS observation,
                rls.s3FileName AS attachmentS3FileName,
                rls.attachmentName AS attachmentName,
                rls.duplicatedReleaseId AS duplicatedReleaseId,
                (CASE WHEN
                        EXISTS (SELECT 1 FROM Release r WHERE r.duplicatedReleaseId = rls.id)
                            OR
                        rls.duplicatedReleaseId IS NOT NULL
                    THEN true
                    ELSE false
                END) AS isDuplicatedRelease,
                rls.isBalanceAdjustment AS isBalanceAdjustment
            FROM
                Release rls
                LEFT JOIN Account ac ON rls.accountId = ac.id
                LEFT JOIN Account ac2 ON rls.targetAccountId  = ac2.id
                LEFT JOIN CreditCard cc ON rls.creditCardId = cc.id
                LEFT JOIN Category ctg ON rls.categoryId = ctg.id
            WHERE
                rls.userId = :userId
                AND rls.date between :firstDt and :lastDt
            ORDER BY
                rls.date, rls.time, rls.id
            """)
    List<MonthlyRelease> getMonthlyReleases(long userId, @NonNull LocalDate firstDt, @NonNull LocalDate lastDt);

    @Query("""
            SELECT rls
            FROM Release rls
            WHERE
                rls.userId = :userId
                AND rls.date between :firstDt and :lastDt
            ORDER BY
                rls.date, rls.time, rls.id
            """)
    List<Release> findAllByUserAndDatesBetween(long userId, @NonNull LocalDate firstDt, @NonNull LocalDate lastDt);

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
                rls.creditCardId is not null AS isCreditCardRelease,
                rls.description AS description,
                ac.name AS accountName,
                cc.name AS creditCardName,
                rls.date AS date,
                rls.amount AS amount,
                rls.type AS type
            FROM
                Release rls
                LEFT JOIN Account ac ON rls.accountId = ac.id
                LEFT JOIN Category ctg ON rls.categoryId = ctg.id
                LEFT JOIN CreditCard cc on rls.creditCardId = cc.id
            WHERE
                rls.userId = :userId
                AND rls.date between :firstDt AND :lastDt
                AND rls.type <> 'T'
                AND rls.done is false
                AND rls.isBalanceAdjustment is false
            ORDER BY
                rls.date, rls.time, rls.id
            """)
    List<HomeUpcomingRelease> getPayableAndReceivableAccounts(long userId, LocalDate firstDt, LocalDate lastDt);

    @Query(value = """
            SELECT
                rls
            FROM
                Release rls
            WHERE
                rls.duplicatedReleaseId = :duplicatedReleaseId
                AND rls.date > :date
            ORDER BY rls.id
            """)
    List<Release> getNextDuplicatedReleases(long duplicatedReleaseId, @NonNull LocalDate date);

    @Query(value = """
            SELECT
                rls
            FROM
                Release rls
            WHERE
                rls.duplicatedReleaseId = :duplicatedReleaseId
                OR rls.id = :duplicatedReleaseId
            ORDER BY rls.id
            """)
    List<Release> getAllDuplicatedReleases(long duplicatedReleaseId);

    @Query(value = """
            SELECT
                rls.id AS id,
                rls.userId AS userId,
                rls.description AS description,
                null AS accountId,
                null AS accountName,
                cc.id AS cardId,
                cc.name AS cardName,
                cc.image AS cardImg,
                rls.amount AS amount,
                rls.type AS type,
                rls.done AS done,
                '' AS targetAccountId,
                '' AS targetAccountName,
                rls.categoryId AS categoryId,
                ctg.name AS categoryName,
                ctg.color AS categoryColor,
                ctg.icon AS categoryIcon,
                rls.date AS date,
                rls.time AS time,
                rls.observation AS observation,
                rls.s3FileName AS attachmentS3FileName,
                rls.attachmentName AS attachmentName,
                rls.duplicatedReleaseId AS duplicatedReleaseId,
                (CASE WHEN
                        EXISTS (SELECT 1 FROM Release r WHERE r.duplicatedReleaseId = rls.id)
                            OR
                        rls.duplicatedReleaseId IS NOT NULL
                    THEN true
                    ELSE false
                END) AS isDuplicatedRelease,
                rls.isBalanceAdjustment AS isBalanceAdjustment
            FROM
                Release rls
                JOIN CreditCard cc ON rls.creditCardId = cc.id
                LEFT JOIN Category ctg ON rls.categoryId = ctg.id
            WHERE
                rls.userId = :userId
                AND rls.creditCardId = :creditCardId
                AND rls.date BETWEEN :firstDt AND :lastDt
            ORDER BY
                rls.date, rls.time, rls.id
            """)
    List<MonthlyRelease> getByInvoice(long userId, long creditCardId, @NonNull LocalDate firstDt, @NonNull LocalDate lastDt);

    @Query(value = """
            SELECT
                rls
            FROM
                Release rls
            WHERE
                rls.userId = :userId
                AND rls.date between :firstDt AND :lastDt
                AND rls.type = 'E'
                AND rls.done is true
                AND rls.isBalanceAdjustment is false
            """)
    List<Release> getReleasesForHomeSpendsCategory(long userId, @NonNull LocalDate firstDt, @NonNull LocalDate lastDt);

    @Query("""
            SELECT
                r.s3FileName AS attachmentS3FileName
            FROM
                Release r
            WHERE
            	r.s3FileName is not null
            	AND r.s3FileName <> ''
            """)
    List<String> getAllReleaseAttachments();
}
