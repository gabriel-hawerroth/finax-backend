package br.finax.controllers;

import br.finax.dto.InterfacesSQL.AccountBasicList;
import br.finax.models.Account;
import br.finax.services.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/accounts")
public class AccountController {

    public final AccountService accountService;

    @GetMapping("/get-by-user")
    public ResponseEntity<List<Account>> getByUser() {
        return ResponseEntity.ok(
                accountService.getByUser()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getById(@PathVariable long id) {
        return ResponseEntity.ok(
                accountService.getById(id)
        );
    }

    @GetMapping("/basic-list")
    public ResponseEntity<List<AccountBasicList>> getBasicList() {
        return ResponseEntity.ok(
                accountService.getBasicList()
        );
    }

    @PostMapping
    public ResponseEntity<Account> save(@RequestBody @Valid Account account) {
        return ResponseEntity.ok(
                accountService.save(account)
        );
    }

    @PostMapping("/adjust-balance/{id}")
    public ResponseEntity<Account> adjustBalance(@PathVariable long id, @RequestParam BigDecimal newBalance) {
        return ResponseEntity.ok(
                accountService.adjustBalance(id, newBalance)
        );
    }
}
