package br.finax.services;

import br.finax.dto.InvoiceMonthValues;
import br.finax.dto.InvoiceValues;
import br.finax.dto.cash_flow.MonthlyRelease;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.CreditCard;
import br.finax.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static br.finax.utils.DateUtils.getNextMonthYear;
import static br.finax.utils.InvoiceUtils.getInvoiceCloseAndFirstDay;
import static br.finax.utils.UtilsService.getAuthUser;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    private final InvoicePaymentService invoicePaymentService;
    private final CreditCardService creditCardService;
    private final ReleaseService releaseService;
    private final CategoryService categoryService;
    private final AccountService accountService;

    @Transactional(readOnly = true)
    public InvoiceMonthValues getMonthValues(long creditCardId, String selectedMonth) {
        final long userId = getAuthUser().getId();
        final CreditCard card = creditCardService.findById(creditCardId);

        checkCardPermission(card);

        final var invoiceDays = getInvoiceCloseAndFirstDay(selectedMonth, card.getCloseDay());

        final List<MonthlyRelease> releases = releaseService.getByInvoice(userId, creditCardId, invoiceDays.firstDay(), invoiceDays.lastDay());
        final BigDecimal previousAmount = invoiceRepository.getInvoicePreviousAmount(userId, creditCardId, invoiceDays.firstDay());

        final BigDecimal amount = releases.stream()
                .filter(MonthlyRelease::done)
                .map(MonthlyRelease::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(previousAmount);

        return new InvoiceMonthValues(
                invoicePaymentService.getInvoicePayments(userId, creditCardId, selectedMonth),
                releases,
                amount
        );
    }

    @Transactional(readOnly = true)
    public InvoiceValues getValues() {
        return new InvoiceValues(
                accountService.getBasicList(false),
                categoryService.findAllActiveByUser(),
                creditCardService.getBasicList()
        );
    }

    @Transactional(readOnly = true)
    public BigDecimal getCurrentInvoiceAmount(CreditCard card) {
        final var invoiceDays = getInvoiceCloseAndFirstDay(getNextMonthYear(), card.getCloseDay());

        final BigDecimal actualInvoiceAmount = invoiceRepository.getInvoiceAmount(
                card.getId(),
                invoiceDays.firstDay(),
                invoiceDays.lastDay()
        );
        final BigDecimal previousInvoicesAmount = invoiceRepository.getInvoicePreviousAmount(
                card.getUserId(),
                card.getId(),
                invoiceDays.firstDay()
        );

        return actualInvoiceAmount.add(previousInvoicesAmount);
    }

    private void checkCardPermission(final CreditCard card) {
        if (!card.getUserId().equals(getAuthUser().getId()))
            throw new WithoutPermissionException();
    }
}
