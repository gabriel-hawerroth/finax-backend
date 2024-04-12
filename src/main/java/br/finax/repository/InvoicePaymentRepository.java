package br.finax.repository;

import br.finax.models.InvoicePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InvoicePaymentRepository extends JpaRepository<InvoicePayment, Long> {

    @Query(value = """
            SELECT
                *
            FROM
                invoice_payment ip
            WHERE
                ip.credit_card_id = :credit_card_id
                AND ip.invoice_month_year = :month_year
            ORDER BY
                ip.payment_date desc, ip.payment_hour desc, ip.id desc
            """, nativeQuery = true
    )
    List<InvoicePayment> getInvoicePayments(long credit_card_id, String month_year);
}
