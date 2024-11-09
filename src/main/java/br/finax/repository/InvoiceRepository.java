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
                release r
            WHERE
                r.credit_card_id = :cardId
                AND r."date" between :invoiceFirstDay AND :invoiceLastDay
                AND r.done is true
            """, nativeQuery = true)
    BigDecimal getInvoiceAmount(long cardId, LocalDate invoiceFirstDay, LocalDate invoiceLastDay);

    @Query(value = """
            SELECT
                COALESCE(
                    GREATEST(
                        SUM(rls.amount) -
                        COALESCE((SELECT SUM(ip.payment_amount)
                        FROM invoice_payment ip
                        WHERE ip.credit_card_id = :creditCardId), 0)
                    , 0)
                , 0) AS previousBalance
            FROM
                release rls
            WHERE
                rls.user_id = :userId
                AND rls.credit_card_id = :creditCardId
                AND rls.date < :firstDt
                AND rls.done is true
            """, nativeQuery = true)
    BigDecimal getInvoicePreviousAmount(long userId, long creditCardId, @NonNull LocalDate firstDt);

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
    BigDecimal getNextInvoicesAmount(long cardId, LocalDate firstDay);
}
