package br.finax.controllers;

import br.finax.dto.HomeValues;
import br.finax.dto.SpendByCategory;
import br.finax.services.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/home")
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/get-values")
    public ResponseEntity<HomeValues> getHomeAccounts(@RequestParam Date firstDt, @RequestParam Date lastDt) {
        return ResponseEntity.ok(
                homeService.getHomeValues(firstDt, lastDt)
        );
    }

    @GetMapping("/get-spends-by-category")
    public ResponseEntity<List<SpendByCategory>> getSpendsByCategory(@RequestParam Date firstDt, @RequestParam Date lastDt) {
        return ResponseEntity.ok(
                homeService.getSpendsByCategory(firstDt, lastDt)
        );
    }
}
