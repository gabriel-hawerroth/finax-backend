package br.finax.controllers;

import br.finax.models.Account;
import br.finax.models.CashFlow;
import br.finax.models.InterfacesSQL.GenericIdDs;
import br.finax.repository.AccountsRepository;
import br.finax.repository.CashFlowRepository;
import br.finax.utils.UtilsService;
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
    @Autowired
    private UtilsService utilsService;

    @GetMapping("/get-by-user")
    private List<Account> getByUser() {
        return accountsRepository.findAllByUserIdOrderByIdAsc(utilsService.getAuthUser().getId());
    }

    @GetMapping("/{id}")
    private Account getById(@PathVariable Long id) {
        return accountsRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account not found"));
    }

    @PostMapping
    private ResponseEntity<Account> save(@RequestBody Account account) {
        return ResponseEntity.ok().body(accountsRepository.save(account));
    }

    @GetMapping("/adjust-balance/{id}")
    private ResponseEntity<Account> adjustBalance(@PathVariable Long id, @RequestParam Double newBalance) {
        try {
            Account account = accountsRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

            CashFlow release = new CashFlow();
            release.setUserId(account.getUserId());
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

    @GetMapping("/basic-list")
    private List<GenericIdDs> getBasicList() {
        return accountsRepository.getBasicList(utilsService.getAuthUser().getId());
    }
}
