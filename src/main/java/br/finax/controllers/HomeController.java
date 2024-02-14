package br.finax.controllers;

import br.finax.records.HomeValues;
import br.finax.services.HomeService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/home")
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/get-home-values")
    private HomeValues getHomeAccounts() {
        return homeService.getHomeValues();
    }
}
