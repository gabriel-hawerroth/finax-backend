package br.finax.services;

import br.finax.dto.InterfacesSQL.InvoicePaymentsPerson;
import br.finax.exceptions.NotFoundException;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.InvoicePayment;
import br.finax.repository.InvoicePaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoicePaymentService {

    private final InvoicePaymentRepository invoicePaymentRepository;

    private final CreditCardService creditCardService;

    @Transactional(readOnly = true)
    public InvoicePayment findById(Long id) {
        return invoicePaymentRepository.findById(id)
                .orElseThrow(NotFoundException::new);
    }

    @Transactional
    public InvoicePayment save(InvoicePayment payment) {
        return invoicePaymentRepository.save(payment);
    }

    @Transactional
    public void deleteById(long invoicePaymentId) {
        invoicePaymentRepository.deleteById(invoicePaymentId);
    }

    @Transactional(readOnly = true)
    public List<InvoicePaymentsPerson> getInvoicePayments(long userId, long creditCardId, String selectedMonth) {
        if (creditCardService.findUserIdById(creditCardId) != userId)
            throw new WithoutPermissionException();

        return invoicePaymentRepository.getInvoicePayments(creditCardId, selectedMonth);
    }

    @Transactional(readOnly = true)
    public double getInvoicePreviousBalance(long userId, long creditCardId, LocalDate firstDt) {
        return invoicePaymentRepository.getInvoicePreviousBalance(userId, creditCardId, firstDt);
    }
}
