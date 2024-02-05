package br.finax.controllers;

import br.finax.models.CreditCard;
import br.finax.repository.CreditCardRepository;
import br.finax.utils.UtilsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/credit-card")
public class CreditCardController {

    @Autowired
    private UtilsService utilsService;
    @Autowired
    private CreditCardRepository creditCardRepository;

    @GetMapping("/get-by-user")
    private List<CreditCard> getByUser() {
        return creditCardRepository.getAllByUser(utilsService.getAuthUser().getId());
    }

    @GetMapping("/{id}")
    private CreditCard getById(@PathVariable Long id) {
        return creditCardRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credit card not found"));
    }

    @PostMapping
    private ResponseEntity<CreditCard> save(@RequestBody CreditCard card) {
        return ResponseEntity.ok().body(creditCardRepository.save(card));
    }
}
