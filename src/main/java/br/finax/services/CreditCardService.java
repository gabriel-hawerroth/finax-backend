package br.finax.services;

import br.finax.dto.InterfacesSQL.BasicCard;
import br.finax.dto.InterfacesSQL.UserCreditCard;
import br.finax.exceptions.NotFoundException;
import br.finax.models.CreditCard;
import br.finax.repository.CreditCardRepository;
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
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;
    private final InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public CreditCard findById(long id) {
        final CreditCard card = creditCardRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        checkPermission(card);

        return card;
    }

    @Transactional(readOnly = true)
    public List<UserCreditCard> getByUser() {
        return creditCardRepository.getAllByUser(getAuthUser().getId());
    }

    @Transactional
    public CreditCard createNew(CreditCard card) {
        card.setId(null);
        card.setUserId(getAuthUser().getId());
        return creditCardRepository.save(card);
    }

    @Transactional
    public CreditCard edit(CreditCard card) {
        checkPermission(card);

        return creditCardRepository.save(card);
    }

    @Transactional(readOnly = true)
    public BigDecimal getCardAvailableLimit(CreditCard card) {
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
        final BigDecimal nextInvoicesAmount = invoiceRepository.getNextInvoicesAmount(
                card.getId(),
                invoiceDays.lastDay().plusDays(1)
        );

        return card.getCardLimit()
                .subtract(actualInvoiceAmount)
                .subtract(previousInvoicesAmount)
                .subtract(nextInvoicesAmount)
                .max(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public List<BasicCard> getBasicList() {
        return creditCardRepository.getBasicList(getAuthUser().getId());
    }

    @Transactional(readOnly = true)
    public long findUserIdById(long id) {
        return creditCardRepository.findUserIdById(id);
    }

    @Transactional(readOnly = true)
    public List<CreditCard> getByUser(long userId) {
        return creditCardRepository.findAllByUserId(userId);
    }

    private void checkPermission(CreditCard card) {
        if (!card.getUserId().equals(getAuthUser().getId()))
            throw new NotFoundException();
    }
}
