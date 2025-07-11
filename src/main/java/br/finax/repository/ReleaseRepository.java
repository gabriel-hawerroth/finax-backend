package br.finax.repository;

import br.finax.dto.InterfacesSQL.HomeRevenueExpense;
import br.finax.dto.InterfacesSQL.HomeUpcomingRelease;
import br.finax.enums.release.ReleaseType;
import br.finax.models.Release;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ReleaseRepository extends JpaRepository<Release, Long> {

    @Query("""
            SELECT
                (rls.duplicatedReleaseId is not null
                    OR
                EXISTS (SELECT 1 FROM Release r WHERE r.duplicatedReleaseId = rls.id))
            FROM
                Release rls
            WHERE
                rls.id = :releaseId
            """)
    boolean isDuplicatedRelease(long releaseId);

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

    @Query("""
            SELECT rls
            FROM Release rls
            WHERE
                rls.userId = :userId
                AND rls.creditCardId = :creditCardId
                AND rls.date between :firstDt and :lastDt
            ORDER BY
                rls.date, rls.time, rls.id
            """)
    List<Release> findAllByUserAndCreditCardAndDatesBetween(long userId, long creditCardId, @NonNull LocalDate firstDt, @NonNull LocalDate lastDt);

    @Query(value = """
            SELECT
                COALESCE(SUM(CASE WHEN rls.type = 'R' THEN rls.amount ELSE 0 END), 0) AS revenues,
                COALESCE(SUM(CASE WHEN rls.type = 'E' THEN rls.amount ELSE 0 END), 0) AS expenses
            FROM
                release rls
                LEFT JOIN account ac ON rls.account_id = ac.id
            WHERE
                rls.user_id = :userId
                AND rls.done = true
                AND rls.date between :firstDt and :lastDt
                AND rls.is_balance_adjustment is false
                AND case when rls.account_id is not null then ac.add_to_cash_flow else true end
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
                AND (
                    (rls.accountId is not null AND ac.active is true)
                    OR
                    (rls.creditCardId is not null AND cc.active is true)
                )
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
                rls
            FROM
                Release rls
                LEFT join Account ac on rls.accountId = ac.id
            WHERE
                rls.userId = :userId
                AND rls.date between :firstDt AND :lastDt
                AND rls.type = :releaseType
                AND rls.done is true
                AND rls.isBalanceAdjustment is false
                AND case when rls.accountId is not null then ac.addToCashFlow else true end
            """)
    List<Release> getReleasesForReleasesByCategoryReport(long userId, @NonNull LocalDate firstDt, @NonNull LocalDate lastDt, @NonNull ReleaseType releaseType);

    @Query(value = """
            SELECT
                rls
            FROM
                Release rls
                LEFT join Account ac on rls.accountId = ac.id
            WHERE
                rls.userId = :userId
                AND rls.date between :firstDt AND :lastDt
                AND rls.type = :releaseType
                AND rls.done is true
                AND rls.isBalanceAdjustment is false
                AND case when rls.accountId is not null then ac.addToCashFlow else true end
            ORDER BY
                rls.date, rls.time, rls.id
            """)
    List<Release> getReleasesForReleasesByAccountReport(long userId, @NonNull LocalDate firstDt, @NonNull LocalDate lastDt, @NonNull ReleaseType releaseType);

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
