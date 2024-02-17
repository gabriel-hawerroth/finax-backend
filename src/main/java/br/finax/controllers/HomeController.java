package br.finax.controllers;

import br.finax.records.HomeValues;
import br.finax.services.HomeService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RequiredArgsConstructor
@RestController
@RequestMapping("/home")
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/get-home-values")
    private HomeValues getHomeAccounts(@RequestParam Date firstDt, @RequestParam Date lastDt) {
        return homeService.getHomeValues(firstDt, lastDt);
    }
}
