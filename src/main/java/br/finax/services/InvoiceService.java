package br.finax.services;

import br.finax.dto.InvoiceMonthValues;
import br.finax.dto.InvoiceValues;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.InvoicePayment;
import br.finax.utils.FileUtils;
import br.finax.utils.UtilsService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoicePaymentService invoicePaymentService;

    private final CreditCardService creditCardService;
    private final CashFlowService cashFlowService;
    private final CategoryService categoryService;
    private final AccountService accountService;

    private final UtilsService utils;
    private final FileUtils fileUtils;

    @Transactional(readOnly = true)
    public InvoiceMonthValues getInvoiceAndReleases(
            long creditCardId, String selectedMonth,
            Date firstDt, Date lastDt
    ) {
        final long userId = utils.getAuthUser().getId();

        return new InvoiceMonthValues(
                invoicePaymentService.getInvoicePayments(userId, creditCardId, selectedMonth),
                cashFlowService.getByInvoice(userId, creditCardId, firstDt, lastDt),
                invoicePaymentService.getInvoicePreviousBalance(userId, creditCardId, firstDt)
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
        checkPermission(payment);

        if (payment.getId() != null) {
            final InvoicePayment invoicePayment = invoicePaymentService.findById(payment.getId());

            payment.setAttachment(invoicePayment.getAttachment());
            payment.setAttachment_name(invoicePayment.getAttachment_name());
        }

        return invoicePaymentService.save(payment);
    }

    @Transactional
    public void deletePayment(long invoicePaymentId) {
        final InvoicePayment payment = invoicePaymentService.findById(invoicePaymentId);

        checkPermission(payment);

        invoicePaymentService.deleteById(invoicePaymentId);
    }

    @Transactional
    public InvoicePayment savePaymentAttachment(long invoiceId, @NonNull MultipartFile attachment) {
        final InvoicePayment payment = invoicePaymentService.findById(invoiceId);

        checkPermission(payment);

        payment.setAttachment(fileUtils.compressFile(attachment, true));
        payment.setAttachment_name(attachment.getOriginalFilename());

        return invoicePaymentService.save(payment);
    }

    @Transactional
    public InvoicePayment removePaymentAttachment(long invoiceId) {
        final InvoicePayment payment = invoicePaymentService.findById(invoiceId);

        checkPermission(payment);

        payment.setAttachment(null);
        payment.setAttachment_name(null);

        return invoicePaymentService.save(payment);
    }

    @Transactional(readOnly = true)
    public byte[] getPaymentAttachment(long invoicePaymentId) {
        return invoicePaymentService.findById(invoicePaymentId)
                .getAttachment();
    }

    private void checkPermission(final InvoicePayment payment) {
        if (creditCardService.findById(payment.getCredit_card_id()).getUser_id() != utils.getAuthUser().getId())
            throw new WithoutPermissionException();
    }
}
