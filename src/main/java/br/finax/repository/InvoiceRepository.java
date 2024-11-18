package br.finax.repository;

import br.finax.models.InvoicePayment;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoicePayment, Long> {

    @Query(value = """
            SELECT
                coalesce(sum(r.amount), 0)
            FROM
                Release r
            WHERE
                r.creditCardId = :cardId
                AND r.date between :invoiceFirstDay AND :invoiceLastDay
                AND r.done is true
            """)
    BigDecimal getInvoiceAmount(long cardId, LocalDate invoiceFirstDay, LocalDate invoiceLastDay);

    @Query(value = """
            SELECT
                COALESCE(
                    GREATEST(
                        SUM(rls.amount) -
                        COALESCE((SELECT SUM(ip.paymentAmount)
                        FROM InvoicePayment ip
                        WHERE ip.creditCardId = :creditCardId), 0)
                    , 0)
                , 0) AS previousBalance
            FROM
                Release rls
            WHERE
                rls.userId = :userId
                AND rls.creditCardId = :creditCardId
                AND rls.date < :firstDt
                AND rls.done is true
            """)
    BigDecimal getInvoicePreviousAmount(long userId, long creditCardId, @NonNull LocalDate firstDt);

    @Query(value = """
            SELECT
                coalesce(sum(r.amount), 0)
            FROM
                Release r
            WHERE
                r.creditCardId = :cardId
                AND r.date >= :firstDay
                AND r.done is true
            """)
    BigDecimal getNextInvoicesAmount(long cardId, LocalDate firstDay);
}
