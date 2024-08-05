package br.finax.controllers;

import br.finax.dto.InterfacesSQL.AccountBasicList;
import br.finax.models.Account;
import br.finax.services.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {

    public final AccountService accountService;

    @GetMapping("/{id}")
    public ResponseEntity<Account> findById(@PathVariable long id) {
        return ResponseEntity.ok(
                accountService.findById(id)
        );
    }

    @GetMapping("/get-by-user")
    public ResponseEntity<List<Account>> getByUser() {
        return ResponseEntity.ok(
                accountService.getByUser()
        );
    }

    @GetMapping("/basic-list")
    public ResponseEntity<List<AccountBasicList>> getBasicList() {
        return ResponseEntity.ok(
                accountService.getBasicList()
        );
    }

    @PostMapping
    public ResponseEntity<Account> createNew(@RequestBody @Valid Account account) {
        final Account savedAccount = accountService.createNew(account);

        final URI uri = URI.create("/account/" + savedAccount.getId());

        return ResponseEntity.created(uri).body(savedAccount);
    }

    @PutMapping
    public ResponseEntity<Account> edit(@RequestBody @Valid Account account) {
        return ResponseEntity.ok(
                accountService.edit(account)
        );
    }

    @PatchMapping("/adjust-balance/{id}")
    public ResponseEntity<Account> adjustBalance(@PathVariable long id, @RequestParam BigDecimal newBalance) {
        return ResponseEntity.ok(
                accountService.adjustBalance(id, newBalance)
        );
    }
}
