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
    public List<Account> getByUser() {
        return accountService.getByUser();
    }

    @GetMapping("/{id}")
    public Account getById(@PathVariable long id) {
        return accountService.getById(id);
    }

    @PostMapping
    public ResponseEntity<Account> save(@RequestBody @Valid Account account) {
        return accountService.save(account);
    }

    @GetMapping("/adjust-balance/{id}")
    public ResponseEntity<Account> adjustBalance(@PathVariable long id, @RequestParam BigDecimal newBalance) {
        return accountService.adjustBalance(id, newBalance);
    }

    @GetMapping("/basic-list")
    public List<AccountBasicList> getBasicList() {
        return accountService.getBasicList();
    }
}
