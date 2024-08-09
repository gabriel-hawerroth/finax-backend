package br.finax.controllers;

import br.finax.dto.HomeCreditCard;
import br.finax.dto.InterfacesSQL;
import br.finax.dto.InterfacesSQL.HomeAccount;
import br.finax.dto.InterfacesSQL.HomeUpcomingRelease;
import br.finax.dto.SpendByCategory;
import br.finax.services.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/home")
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/get-revenue-expense")
    public ResponseEntity<InterfacesSQL.HomeRevenueExpense> getReleaseExpense() {
        return ResponseEntity.ok(
                homeService.getRevenueExpense()
        );
    }

    @GetMapping("/get-accounts-list")
    public ResponseEntity<List<HomeAccount>> getAccountsList() {
        return ResponseEntity.ok(
                homeService.getAccountsList()
        );
    }

    @GetMapping("/get-upcoming-releases")
    public ResponseEntity<List<HomeUpcomingRelease>> getUpcomingReleases() {
        return ResponseEntity.ok(
                homeService.getUpcomingReleases()
        );
    }

    @GetMapping("/get-spends-by-category")
    public ResponseEntity<List<SpendByCategory>> getSpendsByCategory() {
        return ResponseEntity.ok(
                homeService.getSpendsByCategory()
        );
    }

    @GetMapping("/get-credit-cards-list")
    public ResponseEntity<List<HomeCreditCard>> getCreditCardsList() {
        return ResponseEntity.ok().body(
                homeService.getCreditCardsList()
        );
    }
}
