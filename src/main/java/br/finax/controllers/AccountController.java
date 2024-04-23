package br.finax.controllers;

import br.finax.dto.InterfacesSQL.AccountBasicList;
import br.finax.exceptions.NotFoundException;
import br.finax.models.Account;
import br.finax.services.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/get-by-user")
    private List<Account> getByUser() {
        return accountService.getByUser();
    }

    @GetMapping("/{id}")
    private Account getById(@PathVariable long id) {
        return accountService.getById(id);
    }

    @PostMapping
    private ResponseEntity<Account> save(@RequestBody Account account) {
        return accountService.save(account);
    }

    @GetMapping("/adjust-balance/{id}")
    private ResponseEntity<Account> adjustBalance(@PathVariable long id, @RequestParam double newBalance) {
        return accountService.adjustBalance(id, newBalance);
    }

    @GetMapping("/basic-list")
    private List<AccountBasicList> getBasicList() {
        return accountService.getBasicList();
    }

    @GetMapping("/teste")
    private ResponseEntity<Map<String, String>> teste() {
        throw new NotFoundException("Teste");
    }
}
