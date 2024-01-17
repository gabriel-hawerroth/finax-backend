package br.finax.controllers;

import br.finax.models.Account;
import br.finax.models.CashFlow;
import br.finax.repository.AccountsRepository;
import br.finax.repository.CashFlowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/accounts")
public class AccountsController {

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private CashFlowRepository cashFlowRepository;

    @GetMapping("/get-by-user/{userId}")
    private List<Account> getByUser(@PathVariable Long userId) {
        return accountsRepository.findAllByUserIdOrderByIdAsc(userId);
    }

    @GetMapping("/{id}")
    private Account getById(@PathVariable Long id) {
        return accountsRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conta não localizada")
        );
    }

    @PostMapping
    private ResponseEntity<Account> save(@RequestBody Account account) {
        try {
            return ResponseEntity.ok().body(accountsRepository.save(account));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao salvar a conta");
        }
    }

    @GetMapping("/adjust-balance/{id}")
    private ResponseEntity<Account> adjustBalance(@PathVariable Long id, @RequestParam Double newBalance) {
        try {
            Account account = accountsRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

            CashFlow release = new CashFlow();
            release.setDescription("");
            release.setAccountId(account.getId());
            release.setAmount(newBalance > account.getBalance() ? newBalance - account.getBalance() : account.getBalance() - newBalance);
            release.setType(newBalance > account.getBalance() ? "R" : "E");
            release.setDone(true);
            release.setCategoryId(21L);
            release.setDate(LocalDate.now());

            cashFlowRepository.saveAndFlush(release);

            account.setBalance(newBalance);

            return ResponseEntity.ok().body(accountsRepository.save(account));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
