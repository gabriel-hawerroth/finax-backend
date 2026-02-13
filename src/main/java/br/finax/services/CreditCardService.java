package br.finax.services;

import static br.finax.utils.DateUtils.getNextMonthYear;
import static br.finax.utils.InvoiceUtils.getInvoiceCloseAndFirstDay;
import static br.finax.utils.UtilsService.getAuthUser;

import java.math.BigDecimal;
import java.util.List;

import br.finax.dto.credit_card.SaveCreditCardDto;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.finax.dto.InterfacesSQL.BasicCard;
import br.finax.dto.InterfacesSQL.UserCreditCard;
import br.finax.enums.ErrorCategory;
import br.finax.exceptions.NotFoundException;
import br.finax.exceptions.ServiceException;
import br.finax.models.CreditCard;
import br.finax.repository.CreditCardRepository;
import br.finax.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class CreditCardService {

    private final CreditCardService service;
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
    public List<CreditCard> findByIdIn(List<Long> ids) {
        final List<CreditCard> card = creditCardRepository.findAllById(ids);
        card.forEach(this::checkPermission);
        return card;
    }

    @Transactional(readOnly = true)
    public List<UserCreditCard> getByUser() {
        return creditCardRepository.getAllByUser(getAuthUser().getId());
    }

    @Transactional
    public CreditCard createNew(SaveCreditCardDto cardDto) {
        final CreditCard card = cardDto.toEntity();
        card.setUserId(getAuthUser().getId());
        card.setActive(true);
        return creditCardRepository.save(card);
    }

    @Transactional
    public CreditCard edit(long cardId, SaveCreditCardDto cardDto) {
        final CreditCard card = cardDto.toEntity();

        final CreditCard oldCard = service.findById(cardId);

        card.setId(cardId);
        card.setActive(oldCard.isActive());
        card.setUserId(oldCard.getUserId());

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

    @Transactional
    public void delete(long cardId) {
        try {
            creditCardRepository.deleteById(cardId);
        } catch (DataIntegrityViolationException _) {
            throw new ServiceException(ErrorCategory.BAD_REQUEST, "linked registers, cannot exclude");
        }
    }

    @Transactional
    public void inactivateCard(long cardId) {
        final var _ = service.findById(cardId);

        creditCardRepository.inactivateCard(cardId);
    }

    @Transactional
    public void activateCard(long cardId) {
        final var _ = service.findById(cardId);

        creditCardRepository.activateCard(cardId);
    }

    private void checkPermission(CreditCard card) {
        if (!card.getUserId().equals(getAuthUser().getId()))
            throw new NotFoundException();
    }
}
