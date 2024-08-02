package br.finax.services;

import br.finax.dto.InvoiceMonthValues;
import br.finax.dto.InvoiceValues;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.CreditCard;
import br.finax.models.InvoicePayment;
import br.finax.utils.FileUtils;
import br.finax.utils.UtilsService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoicePaymentService invoicePaymentService;

    private final CreditCardService creditCardService;
    private final ReleaseService releaseService;
    private final CategoryService categoryService;
    private final AccountService accountService;

    private final UtilsService utils;
    private final FileUtils fileUtils;

    private InvoiceCloseAndFirstDay getInvoiceDays(String monthYear, int closingDay) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.parse(monthYear, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format, expected MM/yyyy");
        }

        // Calculate the last day of the invoice
        LocalDate closeDay;
        if (closingDay > yearMonth.lengthOfMonth()) {
            closeDay = yearMonth.atEndOfMonth().plusDays((long) closingDay - yearMonth.lengthOfMonth());
        } else {
            closeDay = yearMonth.atDay(closingDay);
        }

        // Calculate the first day of the next invoice period
        LocalDate firstDay = closeDay.plusDays(1);

        // Adjust the first day for the edge cases
        if (closingDay >= 28 && closeDay.getMonthValue() != yearMonth.getMonthValue()) {
            firstDay = yearMonth.plusMonths(1).atDay(2);
        }

        return new InvoiceCloseAndFirstDay(closeDay, firstDay);
    }

    @Transactional(readOnly = true)
    public InvoiceMonthValues getInvoiceAndReleases(
            long creditCardId, String selectedMonth,
            Date firstDt, Date lastDt
    ) {
        final long userId = utils.getAuthUser().getId();

        final CreditCard card = creditCardService.findById(creditCardId);

        final var closeAndFirstDayInvoice = getInvoiceDays(selectedMonth, card.getCloseDay());
        final LocalDate firstDay = closeAndFirstDayInvoice.firstDay();
        final LocalDate closeDay = closeAndFirstDayInvoice.closeDay();

        return new InvoiceMonthValues(
                invoicePaymentService.getInvoicePayments(userId, creditCardId, selectedMonth),
                releaseService.getByInvoice(userId, creditCardId, firstDay, closeDay),
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
            payment.setAttachmentName(invoicePayment.getAttachmentName());
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
        payment.setAttachmentName(attachment.getOriginalFilename());

        return invoicePaymentService.save(payment);
    }

    @Transactional
    public InvoicePayment removePaymentAttachment(long invoiceId) {
        final InvoicePayment payment = invoicePaymentService.findById(invoiceId);

        checkPermission(payment);

        payment.setAttachment(null);
        payment.setAttachmentName(null);

        return invoicePaymentService.save(payment);
    }

    @Transactional(readOnly = true)
    public byte[] getPaymentAttachment(long invoicePaymentId) {
        return invoicePaymentService.findById(invoicePaymentId)
                .getAttachment();
    }

    private void checkPermission(final InvoicePayment payment) {
        if (creditCardService.findById(payment.getCreditCardId()).getUserId() != utils.getAuthUser().getId())
            throw new WithoutPermissionException();
    }

    private record InvoiceCloseAndFirstDay(
            LocalDate closeDay,
            LocalDate firstDay
    ) {
    }
}
