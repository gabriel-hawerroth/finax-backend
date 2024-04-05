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

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/home")
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/get-values")
    private HomeValues getHomeAccounts(@RequestParam Date firstDt, @RequestParam Date lastDt) {
        return homeService.getHomeValues(firstDt, lastDt);
    }

    @GetMapping("/get-spends-by-category")
    private ResponseEntity<List<SpendByCategory>> getSpendsByCategory(@RequestParam Date firstDt, @RequestParam Date lastDt) {
        return homeService.getSpendsByCategory(firstDt, lastDt);
    }
}
