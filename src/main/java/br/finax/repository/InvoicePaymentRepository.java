package br.finax.repository;

import br.finax.dto.InterfacesSQL.InvoicePaymentPerson;
import br.finax.models.InvoicePayment;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InvoicePaymentRepository extends JpaRepository<InvoicePayment, Long> {

    @Query(value = """
            SELECT
                ip.id AS id,
                ip.creditCardId AS creditCardId,
                ip.monthYear AS monthYear,
                ip.paymentAmount AS paymentAmount,
                ip.paymentDate AS paymentDate,
                ip.paymentHour AS paymentHour,
                ip.attachmentName AS attachmentName,
                ip.paymentAccountId AS paymentAccountId,
                ac.name AS paymentAccountName,
                ac.image AS paymentAccountImage
            FROM
                InvoicePayment ip
                JOIN Account ac ON ip.paymentAccountId = ac.id
            WHERE
                ip.creditCardId = :creditCardId
                AND ip.monthYear = :monthYear
            ORDER BY
                ip.paymentDate desc, ip.paymentHour desc, ip.id desc
            """)
    List<InvoicePaymentPerson> getInvoicePayments(long creditCardId, @NonNull String monthYear);

    @Query("""
            SELECT
                ip.s3FileName AS fileName
            FROM
                InvoicePayment ip
            WHERE
                ip.s3FileName is not null
                AND ip.s3FileName <> ''
            """)
    List<String> getAllInvoicePaymentAttachments();
}
