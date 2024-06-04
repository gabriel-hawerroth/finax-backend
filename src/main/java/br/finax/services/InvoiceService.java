package br.finax.services;

import br.finax.dto.InvoiceMonthValues;
import br.finax.dto.InvoiceValues;
import br.finax.models.InvoicePayment;
import br.finax.utils.FileUtils;
import br.finax.utils.UtilsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    public InvoiceMonthValues getInvoiceAndReleases(
            long creditCardId, String selectedMonth,
            Date firstDt, Date lastDt
    ) {
        final long userId = utils.getAuthUser().getId();

        return new InvoiceMonthValues(
                invoicePaymentService.getInvoicePayments(creditCardId, selectedMonth),
                cashFlowService.getByInvoice(userId, creditCardId, firstDt, lastDt),
                invoicePaymentService.getInvoicePreviousBalance(userId, creditCardId, firstDt)
        );
    }

    public InvoiceValues getValues() {
        return new InvoiceValues(
                accountService.getBasicList(),
                categoryService.getByUser(),
                creditCardService.getBasicList()
        );
    }

    public InvoicePayment savePayment(InvoicePayment payment) {
        if (payment.getId() != null) {
            final InvoicePayment invoicePayment = invoicePaymentService.findById(payment.getId());

            payment.setAttachment(invoicePayment.getAttachment());
            payment.setAttachment_name(invoicePayment.getAttachment_name());
        }

        return invoicePaymentService.save(payment);
    }

    public void deletePayment(long invoicePaymentId) {
        invoicePaymentService.deleteById(invoicePaymentId);
    }

    public InvoicePayment saveInvoiceAttachment(long invoiceId, MultipartFile attachment) {
        final InvoicePayment payment = invoicePaymentService.findById(invoiceId);

        payment.setAttachment(fileUtils.compressFile(attachment, true));
        payment.setAttachment_name(attachment.getOriginalFilename());

        return invoicePaymentService.save(payment);
    }

    public InvoicePayment removeAttachment(long invoiceId) {
        final InvoicePayment payment = invoicePaymentService.findById(invoiceId);

        payment.setAttachment(null);
        payment.setAttachment_name(null);

        return invoicePaymentService.save(payment);
    }

    public byte[] getPaymentAttachment(long invoicePaymentId) {
        return invoicePaymentService.findById(invoicePaymentId)
                .getAttachment();
    }
}
