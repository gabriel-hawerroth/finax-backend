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
                ip.id,
                ip.credit_card_id AS creditCardId,
                ip.month_year AS monthYear,
                ip.payment_amount AS paymentAmount,
                ip.payment_date AS paymentDate,
                ip.payment_hour AS paymentHour,
                ip.attachment_name AS attachmentName,
                ip.payment_account_id AS paymentAccountId,
                ac.name AS paymentAccountName,
                ac.image AS paymentAccountImage
            FROM
                invoice_payment ip
                JOIN account ac ON ip.payment_account_id = ac.id
            WHERE
                ip.credit_card_id = :creditCardId
                AND ip.month_year = :monthYear
            ORDER BY
                ip.payment_date desc, ip.payment_hour desc, ip.id desc
            """, nativeQuery = true)
    List<InvoicePaymentsPerson> getInvoicePayments(long creditCardId, @NonNull String monthYear);

    @Query(value = """
            SELECT
                COALESCE(
                    GREATEST(
                    SUM(rls.amount) -
                    (SELECT SUM(ip.payment_amount)
                    FROM invoice_payment ip
                    WHERE ip.credit_card_id = :creditCardId)
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
    double getInvoicePreviousBalance(long userId, long creditCardId, @NonNull Date firstDt);
}
