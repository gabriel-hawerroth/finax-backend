package br.finax.services;

import br.finax.dto.InterfacesSQL;
import br.finax.exceptions.NotFoundException;
import br.finax.models.InvoicePayment;
import br.finax.repository.InvoicePaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoicePaymentService {

    private final InvoicePaymentRepository invoicePaymentRepository;

    public InvoicePayment findById(Long id) {
        return invoicePaymentRepository.findById(id)
                .orElseThrow(NotFoundException::new);
    }

    public InvoicePayment save(InvoicePayment payment) {
        return invoicePaymentRepository.save(payment);
    }

    public void deleteById(long invoicePaymentId) {
        invoicePaymentRepository.deleteById(invoicePaymentId);
    }

    public List<InterfacesSQL.InvoicePaymentsPerson> getInvoicePayments(long creditCardId, String selectedMonth) {
        return invoicePaymentRepository.getInvoicePayments(creditCardId, selectedMonth);
    }

    public double getInvoicePreviousBalance(long userId, long creditCardId, Date firstDt) {
        return invoicePaymentRepository.getInvoicePreviousBalance(userId, creditCardId, firstDt);
    }
}
