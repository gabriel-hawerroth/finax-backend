package br.finax.services;

import br.finax.dto.InvoiceMonthValues;
import br.finax.dto.InvoiceValues;
import br.finax.models.InvoicePayment;
import br.finax.repository.*;
import br.finax.utils.UtilsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import static br.finax.utils.UtilsService.compressImage;
import static br.finax.utils.UtilsService.compressPdf;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoicePaymentRepository invoicePaymentRepository;
    private final CashFlowRepository cashFlowRepository;

    private final AccountsRepository accountsRepository;
    private final CategoryRepository categoryRepository;
    private final CreditCardRepository creditCardRepository;

    private final UtilsService utilsService;

    public InvoiceMonthValues getInvoiceAndReleases(
            long creditCardId, String selectedMonth,
            Date firstDt, Date lastDt
    ) {
        final long userId = utilsService.getAuthUser().getId();

        return new InvoiceMonthValues(
                invoicePaymentRepository.getInvoicePayments(creditCardId, selectedMonth),
                cashFlowRepository.getByInvoice(userId, creditCardId, firstDt, lastDt)
        );
    }

    public InvoiceValues getValues() {
        final long userId = utilsService.getAuthUser().getId();

        return new InvoiceValues(
                accountsRepository.getBasicList(userId),
                categoryRepository.findByUser(userId),
                creditCardRepository.getBasicList(userId)
        );
    }

    public InvoicePayment savePayment(InvoicePayment payment) {
        try {
            if (payment.getId() != null) {
                final InvoicePayment invoicePayment = invoicePaymentRepository.findById(payment.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "invoice payment not found"));

                payment.setAttachment(invoicePayment.getAttachment());
                payment.setAttachment_name(invoicePayment.getAttachment_name());
            }

            return invoicePaymentRepository.save(payment);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error saving invoice payment");
        }
    }

    public ResponseEntity<Void> deletePayment(long invoicePaymentId) {
        try {
            invoicePaymentRepository.deleteById(invoicePaymentId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "error deleting invoice payment");
        }
    }

    public InvoicePayment saveInvoiceAttachment(long invoiceId, MultipartFile attachment) {
        try {
            if (attachment == null || attachment.isEmpty())
                throw new IllegalArgumentException("invalid attachment");

            final InvoicePayment payment = invoicePaymentRepository.findById(invoiceId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "invoice payment not found"));

            final String fileExtension = Objects.requireNonNull(attachment.getOriginalFilename()).split("\\.")[1];

            payment.setAttachment_name(attachment.getOriginalFilename());

            switch (fileExtension) {
                case "pdf":
                    payment.setAttachment(compressPdf(attachment.getBytes()));
                    break;
                case "png", "webp":
                    payment.setAttachment(attachment.getBytes());
                    break;
                default:
                    payment.setAttachment(compressImage(attachment.getBytes(), true));
            }

            return invoicePaymentRepository.save(payment);
        } catch (RuntimeException | IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public InvoicePayment removeAttachment(long invoiceId) {
        try {
            final InvoicePayment payment = invoicePaymentRepository.findById(invoiceId)
                    .orElseThrow(() -> new RuntimeException("Invoice not found"));

            payment.setAttachment(null);
            payment.setAttachment_name(null);

            return invoicePaymentRepository.save(payment);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public ResponseEntity<byte[]> getPaymentAttachment(long invoicePaymentId) {
        return ResponseEntity.ok().body(
                invoicePaymentRepository.findById(invoicePaymentId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "invoice payment not found"))
                        .getAttachment()
        );
    }
}
