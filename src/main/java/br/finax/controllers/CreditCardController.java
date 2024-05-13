package br.finax.controllers;

import br.finax.dto.InterfacesSQL;
import br.finax.models.CreditCard;
import br.finax.services.CreditCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/credit-card")
public class CreditCardController {

    private final CreditCardService creditCardService;

    @GetMapping("/get-by-user")
    public List<InterfacesSQL.UserCreditCards> getByUser() {
        return creditCardService.getByUser();
    }

    @GetMapping("/{id}")
    public CreditCard getById(@PathVariable long id) {
        return creditCardService.getById(id);
    }

    @PostMapping
    public ResponseEntity<CreditCard> save(@RequestBody @Valid CreditCard card) {
        return creditCardService.save(card);
    }

    @GetMapping("/basic-list")
    public List<InterfacesSQL.CardBasicList> getBasicList() {
        return creditCardService.getBasicList();
    }
}
