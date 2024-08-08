package br.finax.services;

import br.finax.dto.InvoiceMonthValues;
import br.finax.dto.InvoiceValues;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.CreditCard;
import br.finax.models.InvoicePayment;
import br.finax.utils.FileUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

import static br.finax.utils.DateUtils.getCurrentMonthYear;
import static br.finax.utils.InvoiceUtils.getInvoiceCloseAndFirstDay;
import static br.finax.utils.UtilsService.getAuthUser;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoicePaymentService invoicePaymentService;

    private final CreditCardService creditCardService;
    private final ReleaseService releaseService;
    private final CategoryService categoryService;
    private final AccountService accountService;

    @Transactional(readOnly = true)
    public InvoiceMonthValues getInvoiceAndReleases(long creditCardId, String selectedMonth) {
        final long userId = getAuthUser().getId();

        final CreditCard card = creditCardService.findById(creditCardId);

        checkCardPermission(card);

        final var closeAndFirstDayInvoice = getInvoiceCloseAndFirstDay(selectedMonth, card.getCloseDay());
        final LocalDate firstDay = closeAndFirstDayInvoice.firstDay();
        final LocalDate closeDay = closeAndFirstDayInvoice.closeDay();

        return new InvoiceMonthValues(
                invoicePaymentService.getInvoicePayments(userId, creditCardId, selectedMonth),
                releaseService.getByInvoice(userId, creditCardId, firstDay, closeDay),
                invoicePaymentService.getInvoicePreviousBalance(userId, creditCardId, firstDay)
        );
    }

    @Transactional(readOnly = true)
    public InvoiceValues getValues() {
        return new InvoiceValues(
                accountService.getBasicList(),
                categoryService.getByUser(),
                creditCardService.getBasicList()
        );
    }

    @Transactional
    public InvoicePayment savePayment(InvoicePayment payment) {
        checkPaymentPermission(payment);

        if (payment.getId() != null) {
            final InvoicePayment invoicePayment = invoicePaymentService.findById(payment.getId());

            payment.setAttachment(invoicePayment.getAttachment());
            payment.setAttachmentName(invoicePayment.getAttachmentName());
        }

        return invoicePaymentService.save(payment);
    }

    @Transactional
    public void deletePayment(long invoicePaymentId) {
        final InvoicePayment payment = invoicePaymentService.findById(invoicePaymentId);

        checkPaymentPermission(payment);

        invoicePaymentService.deleteById(invoicePaymentId);
    }

    @Transactional
    public InvoicePayment savePaymentAttachment(long invoiceId, @NonNull MultipartFile attachment) {
        final InvoicePayment payment = invoicePaymentService.findById(invoiceId);

        checkPaymentPermission(payment);

        payment.setAttachment(FileUtils.compressFile(attachment, true));
        payment.setAttachmentName(attachment.getOriginalFilename());

        return invoicePaymentService.save(payment);
    }

    @Transactional
    public InvoicePayment removePaymentAttachment(long invoiceId) {
        final InvoicePayment payment = invoicePaymentService.findById(invoiceId);

        checkPaymentPermission(payment);

        payment.setAttachment(null);
        payment.setAttachmentName(null);

        return invoicePaymentService.save(payment);
    }

    @Transactional(readOnly = true)
    public byte[] getPaymentAttachment(long invoicePaymentId) {
        final InvoicePayment payment = invoicePaymentService.findById(invoicePaymentId);

        checkPaymentPermission(payment);

        return payment.getAttachment();
    }

    @Transactional(readOnly = true)
    public BigDecimal getCurrentInvoiceAmount(CreditCard card) {
        final var invoiceDays = getInvoiceCloseAndFirstDay(getCurrentMonthYear(), card.getCloseDay());

        return releaseService.getCurrentCardInvoiceAmount(
                card.getId(),
                invoiceDays.firstDay(),
                invoiceDays.closeDay()
        );
    }

    private void checkCardPermission(final CreditCard card) {
        if (!card.getUserId().equals(getAuthUser().getId()))
            throw new WithoutPermissionException();
    }

    private void checkPaymentPermission(final InvoicePayment payment) {
        checkCardPermission(creditCardService.findById(payment.getCreditCardId()));
    }
}
