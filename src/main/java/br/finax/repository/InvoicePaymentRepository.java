package br.finax.repository;

import br.finax.models.InvoicePayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoicePaymentRepository extends JpaRepository<InvoicePayment, Long> {

    List<InvoicePayment> findAllByInvoiceIdOrderByPaymentDateDescPaymentHourDesc(long invoiceId);
}
