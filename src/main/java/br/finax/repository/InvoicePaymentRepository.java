package br.finax.repository;

import br.finax.dto.InterfacesSQL.InvoicePaymentsPerson;
import br.finax.models.InvoicePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
                ip.credit_card_id = :credit_card_id
                AND ip.invoice_month_year = :month_year
            ORDER BY
                ip.payment_date desc, ip.payment_hour desc, ip.id desc
            """, nativeQuery = true
    )
    List<InvoicePaymentsPerson> getInvoicePayments(long credit_card_id, String month_year);
}
