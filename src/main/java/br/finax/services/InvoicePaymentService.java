package br.finax.services;

import br.finax.dto.InterfacesSQL;
import br.finax.exceptions.NotFoundException;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.InvoicePayment;
import br.finax.repository.InvoicePaymentRepository;
import br.finax.utils.UtilsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoicePaymentService {

    private final InvoicePaymentRepository invoicePaymentRepository;

    private final CreditCardService creditCardService;
    private final UtilsService utils;

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
    public List<InterfacesSQL.InvoicePaymentsPerson> getInvoicePayments(long creditCardId, String selectedMonth) {
        if (creditCardService.findById(creditCardId).getUser_id() != utils.getAuthUser().getId())
            throw new WithoutPermissionException();

        return invoicePaymentRepository.getInvoicePayments(creditCardId, selectedMonth);
    }

    @Transactional(readOnly = true)
    public double getInvoicePreviousBalance(long userId, long creditCardId, Date firstDt) {
        return invoicePaymentRepository.getInvoicePreviousBalance(userId, creditCardId, firstDt);
    }
}
