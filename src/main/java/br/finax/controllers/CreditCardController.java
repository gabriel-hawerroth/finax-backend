package br.finax.controllers;

import br.finax.models.CreditCard;
import br.finax.records.InvoiceAndReleases;
import br.finax.services.CreditCardService;
import br.finax.utils.InterfacesSQL;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/credit-card")
public class CreditCardController {

    private final CreditCardService creditCardService;

    @GetMapping("/get-by-user")
    private List<InterfacesSQL.UserCreditCards> getByUser() {
        return creditCardService.getByUser();
    }

    @GetMapping("/{id}")
    private CreditCard getById(@PathVariable long id) {
        return creditCardService.getById(id);
    }

    @PostMapping
    private ResponseEntity<CreditCard> save(@RequestBody CreditCard card) {
        return creditCardService.save(card);
    }

    @GetMapping("/basic-list")
    private List<InterfacesSQL.CardBasicList> getBasicList() {
        return creditCardService.getBasicList();
    }

    @GetMapping("/invoice-and-releases")
    private InvoiceAndReleases getInvoiceAndReleases(@RequestParam long creditCardId, @RequestParam String selectedMonth) {
        return creditCardService.getInvoiceAndReleases(creditCardId, selectedMonth);
    }
}
