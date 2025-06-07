package br.finax.controllers;

import br.finax.dto.AppErrorLog;
import br.finax.services.AppService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/service")
public class AppController {

    private final AppService appService;

    @Async
    @PostMapping("/log-app-error")
    public void logAppError(@RequestBody AppErrorLog appErrorLog) {
        appService.logAppError(appErrorLog);
    }
}
