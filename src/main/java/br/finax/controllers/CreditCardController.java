package br.finax.controllers;

import br.finax.models.CreditCard;
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
    private CreditCard getById(@PathVariable Long id) {
        return creditCardService.getById(id);
    }

    @PostMapping
    private ResponseEntity<CreditCard> save(@RequestBody CreditCard card) {
        return creditCardService.save(card);
    }
}
