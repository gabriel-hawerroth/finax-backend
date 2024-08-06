package br.finax.controllers;

import br.finax.dto.InterfacesSQL.CardBasicList;
import br.finax.dto.InterfacesSQL.UserCreditCards;
import br.finax.models.CreditCard;
import br.finax.services.CreditCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/credit-card")
public class CreditCardController {

    private final CreditCardService creditCardService;

    @GetMapping("/get-by-user")
    public ResponseEntity<List<UserCreditCards>> getByUser() {
        return ResponseEntity.ok(
                creditCardService.getByUser()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditCard> findById(@PathVariable long id) {
        return ResponseEntity.ok(
                creditCardService.findById(id)
        );
    }

    @GetMapping("/basic-list")
    public ResponseEntity<List<CardBasicList>> getBasicList() {
        return ResponseEntity.ok(
                creditCardService.getBasicList()
        );
    }

    @PostMapping
    public ResponseEntity<CreditCard> createNew(@RequestBody @Valid CreditCard card) {
        final CreditCard creditCard = creditCardService.createNew(card);

        final URI uri = URI.create("/credit-card/" + creditCard.getId());

        return ResponseEntity.created(uri).body(creditCard);
    }

    @PutMapping
    public ResponseEntity<CreditCard> edit(@RequestBody @Valid CreditCard card) {
        return ResponseEntity.ok(
                creditCardService.edit(card)
        );
    }
}
