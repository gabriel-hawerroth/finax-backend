package br.finax.controllers;

import java.net.URI;
import java.util.List;

import br.finax.dto.credit_card.SaveCreditCardDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.finax.dto.InterfacesSQL.BasicCard;
import br.finax.dto.InterfacesSQL.UserCreditCard;
import br.finax.models.CreditCard;
import br.finax.services.CreditCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/credit-card")
public class CreditCardController {

    private final CreditCardService creditCardService;

    @GetMapping("/get-by-user")
    public ResponseEntity<List<UserCreditCard>> getByUser() {
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
    public ResponseEntity<List<BasicCard>> getBasicList() {
        return ResponseEntity.ok(
                creditCardService.getBasicList()
        );
    }

    @PostMapping
    public ResponseEntity<CreditCard> createNew(@RequestBody @Valid SaveCreditCardDto card) {
        final CreditCard creditCard = creditCardService.createNew(card);

        final URI uri = URI.create("/credit-card/" + creditCard.getId());

        return ResponseEntity.created(uri).body(creditCard);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CreditCard> edit(@PathVariable long id, @RequestBody @Valid SaveCreditCardDto card) {
        return ResponseEntity.ok(
                creditCardService.edit(id, card)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        creditCardService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/inactivate/{id}")
    public ResponseEntity<Void> inactivate(@PathVariable long id) {
        creditCardService.inactivateCard(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/activate/{id}")
    public ResponseEntity<Void> activate(@PathVariable long id) {
        creditCardService.activateCard(id);
        return ResponseEntity.ok().build();
    }
}
