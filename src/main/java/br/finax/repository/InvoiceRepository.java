package br.finax.repository;

import br.finax.models.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query(value =
            """
            SELECT
                *
            FROM
                invoice i
            WHERE
                i.user_id = :user_id
                AND i.credit_card_id = :credit_card_id
                AND i.month_year = :month_year
            """, nativeQuery = true)
    Optional<Invoice> findByMonthYear(long user_id, long credit_card_id, String month_year);

    @Query(value =
            """
            SELECT
                COALESCE(SUM(cf.amount), 0)
            FROM
                cash_flow cf
            WHERE
                cf.invoice_id = :invoice_id
                AND cf.done = true
            """, nativeQuery = true)
    double getInvoiceAmount(long invoice_id);
}
