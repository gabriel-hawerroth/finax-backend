package br.finax.services;

import br.finax.models.CreditCard;
import br.finax.models.Invoice;
import br.finax.records.InvoiceAndReleases;
import br.finax.repository.CashFlowRepository;
import br.finax.repository.CreditCardRepository;
import br.finax.repository.InvoiceRepository;
import br.finax.utils.InterfacesSQL;
import br.finax.utils.UtilsService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;
    private final InvoiceRepository invoiceRepository;
    private final CashFlowRepository cashFlowRepository;
    private final UtilsService utilsService;

    public List<InterfacesSQL.UserCreditCards> getByUser() {
        return creditCardRepository.getAllByUser(utilsService.getAuthUser().getId());
    }

    public CreditCard getById(long id) {
        return creditCardRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credit card not found"));
    }

    public ResponseEntity<CreditCard> save(CreditCard card) {
        return ResponseEntity.ok().body(creditCardRepository.save(card));
    }

    public List<InterfacesSQL.CardBasicList> getBasicList() {
        return creditCardRepository.getBasicList(utilsService.getAuthUser().getId());
    }

    public InvoiceAndReleases getInvoiceAndReleases(long creditCardId, String selectedMonth) {
        final Invoice invoice =
                invoiceRepository.findByMonthYear(utilsService.getAuthUser().getId(), creditCardId, selectedMonth)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice not found"));

        return new InvoiceAndReleases(
                invoice,
                cashFlowRepository.getByInvoice(invoice.getId())
        );
    }
}
