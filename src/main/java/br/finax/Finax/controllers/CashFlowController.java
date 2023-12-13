package br.finax.finax.controllers;

import br.finax.finax.models.CashFlow;
import br.finax.finax.models.InterfacesSQL;
import br.finax.finax.repository.CashFlowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/cash-flow")
public class CashFlowController {

    @Autowired
    private CashFlowRepository cashFlowRepository;

    @GetMapping("/{id}")
    private CashFlow getById(@PathVariable Long id) {
        return cashFlowRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lançamento não encontrado")
        );
    }

    @GetMapping
    private List<InterfacesSQL.MonthlyCashFlow> getMonthlyReleases(
            @RequestParam Long userId, @RequestParam Integer year, @RequestParam Integer month
    ) {
        return cashFlowRepository.getCashFlow(userId, year, month);
    }

    @PostMapping
    private CashFlow save(@RequestBody CashFlow cashFlow) {
        try {
            return cashFlowRepository.save(cashFlow);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
