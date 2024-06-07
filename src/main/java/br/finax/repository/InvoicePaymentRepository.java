package br.finax.repository;

import br.finax.dto.InterfacesSQL.InvoicePaymentsPerson;
import br.finax.models.InvoicePayment;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface InvoicePaymentRepository extends JpaRepository<InvoicePayment, Long> {

    @Query(value = """
            SELECT
                ip.*,
                ba.name AS payment_account_name,
                ba.image AS payment_account_image
            FROM
                invoice_payment ip
                JOIN bank_account ba ON ip.payment_account_id = ba.id
            WHERE
                ip.creditCardId = :creditCardId
                AND ip.invoice_month_year = :monthYear
            ORDER BY
                ip.payment_date desc, ip.payment_hour desc, ip.id desc
            """, nativeQuery = true
    )
    List<InvoicePaymentsPerson> getInvoicePayments(long creditCardId, @NonNull String monthYear);

    @Query(value = """
            SELECT
                COALESCE(
                    GREATEST(
                    SUM(cf.amount) -
                    (SELECT SUM(ip.payment_amount)
                    FROM invoice_payment ip
                    WHERE ip.creditCardId = :creditCardId)
                    , 0)
                , 0) AS previousBalance
            FROM
                cash_flow cf
            WHERE
                cf.userId = :userId
                AND cf.creditCardId = :creditCardId
                AND cf.date < :firstDt
                AND cf.done is true
            """, nativeQuery = true
    )
    double getInvoicePreviousBalance(long userId, long creditCardId, @NonNull Date firstDt);
}
