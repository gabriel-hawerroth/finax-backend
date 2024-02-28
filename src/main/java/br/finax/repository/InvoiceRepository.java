package br.finax.repository;

import br.finax.models.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query(value =
            """
            SELECT
                i.id
            FROM
                invoice i
            WHERE
                i.user_id = :user_id
                AND i.credit_card_id = :credit_card_id
                AND i.month_year = :month_year
            LIMIT 1
            """, nativeQuery = true)
    Long checkIfExists(long user_id, long credit_card_id, String month_year);
}
