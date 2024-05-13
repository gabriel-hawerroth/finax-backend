package br.finax.services;

import br.finax.dto.InvoiceMonthValues;
import br.finax.dto.InvoiceValues;
import br.finax.exceptions.CompressionErrorException;
import br.finax.exceptions.NotFoundException;
import br.finax.models.InvoicePayment;
import br.finax.repository.*;
import br.finax.utils.UtilsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoicePaymentRepository invoicePaymentRepository;
    private final CashFlowRepository cashFlowRepository;

    private final AccountsRepository accountsRepository;
    private final CategoryRepository categoryRepository;
    private final CreditCardRepository creditCardRepository;

    private final UtilsService utils;

    public InvoiceMonthValues getInvoiceAndReleases(
            long creditCardId, String selectedMonth,
            Date firstDt, Date lastDt
    ) {
        final long userId = utils.getAuthUser().getId();

        return new InvoiceMonthValues(
                invoicePaymentRepository.getInvoicePayments(creditCardId, selectedMonth),
                cashFlowRepository.getByInvoice(userId, creditCardId, firstDt, lastDt),
                invoicePaymentRepository.getInvoicePreviousBalance(userId, creditCardId, firstDt)
        );
    }

    public InvoiceValues getValues() {
        final long userId = utils.getAuthUser().getId();

        return new InvoiceValues(
                accountsRepository.getBasicList(userId),
                categoryRepository.findByUser(userId),
                creditCardRepository.getBasicList(userId)
        );
    }

    public InvoicePayment savePayment(InvoicePayment payment) {
        if (payment.getId() != null) {
            final InvoicePayment invoicePayment = invoicePaymentRepository.findById(payment.getId())
                    .orElseThrow(NotFoundException::new);

            payment.setAttachment(invoicePayment.getAttachment());
            payment.setAttachment_name(invoicePayment.getAttachment_name());
        }

        return invoicePaymentRepository.save(payment);
    }

    public ResponseEntity<Void> deletePayment(long invoicePaymentId) {
        invoicePaymentRepository.deleteById(invoicePaymentId);
        return ResponseEntity.ok().build();
    }

    public InvoicePayment saveInvoiceAttachment(long invoiceId, MultipartFile attachment) {
        if (attachment == null || attachment.isEmpty())
            throw new IllegalArgumentException("invalid attachment");

        final InvoicePayment payment = invoicePaymentRepository.findById(invoiceId)
                .orElseThrow(NotFoundException::new);

        final String fileExtension = Objects.requireNonNull(attachment.getOriginalFilename()).split("\\.")[1];

        payment.setAttachment_name(attachment.getOriginalFilename());

        try {
            switch (fileExtension) {
                case "pdf":
                    payment.setAttachment(utils.compressPdf(attachment.getBytes()));
                    break;
                case "png", "webp":
                    payment.setAttachment(attachment.getBytes());
                    break;
                default:
                    payment.setAttachment(utils.compressImage(attachment.getBytes(), true));
            }
        } catch (IOException ioException) {
            throw new CompressionErrorException();
        }

        return invoicePaymentRepository.save(payment);
    }

    public InvoicePayment removeAttachment(long invoiceId) {
        final InvoicePayment payment = invoicePaymentRepository.findById(invoiceId)
                .orElseThrow(NotFoundException::new);

        payment.setAttachment(null);
        payment.setAttachment_name(null);

        return invoicePaymentRepository.save(payment);
    }

    public ResponseEntity<byte[]> getPaymentAttachment(long invoicePaymentId) {
        return ResponseEntity.ok().body(
                invoicePaymentRepository.findById(invoicePaymentId)
                        .orElseThrow(NotFoundException::new)
                        .getAttachment()
        );
    }
}
