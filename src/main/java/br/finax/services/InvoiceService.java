package br.finax.services;

import br.finax.dto.InvoiceMonthValues;
import br.finax.dto.InvoiceValues;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.CreditCard;
import br.finax.utils.InvoiceUtils.InvoiceCloseAndFirstDay;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public BigDecimal getCurrentInvoiceAmount(CreditCard card) {
        final var invoiceDays = getInvoiceCloseAndFirstDay(getNextMonthYear(), card.getCloseDay());

        return releaseService.getCardInvoiceAmount(
                card.getId(),
                invoiceDays.firstDay(),
                invoiceDays.closeDay()
        );
    }

    @Transactional(readOnly = true)
    public BigDecimal getCardNextInvoicesAmount(CreditCard card) {
        final InvoiceCloseAndFirstDay invoiceDays = getInvoiceCloseAndFirstDay(getNextMonthYear(), card.getCloseDay());
        final LocalDate firstDay = invoiceDays.closeDay().plusDays(1);

        return releaseService.getCardNextInvoicesAmount(
                card.getId(),
                firstDay
        );
    }

    private void checkCardPermission(final CreditCard card) {
        if (!card.getUserId().equals(getAuthUser().getId()))
            throw new WithoutPermissionException();
    }

    private String getNextMonthYear() {
        final String currentMonthYear = getCurrentMonthYear();

        final String month = currentMonthYear.split("/")[0];

        final String nextMonth;
        if (month.equals("12")) {
            nextMonth = "01";
        } else {
            final int intMonth = Integer.parseInt(month) + 1;
            nextMonth = intMonth < 10 ? "0" + intMonth : String.valueOf(intMonth);
        }

        return nextMonth + "/" + currentMonthYear.split("/")[1];
    }
}
