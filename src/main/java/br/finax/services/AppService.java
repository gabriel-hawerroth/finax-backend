package br.finax.services;

import br.finax.dto.AppErrorLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AppService {

    public void logAppError(AppErrorLog appErrorLog) {
        log.error("App Error: {}", appErrorLog.message());
        log.error("Stack Trace: {}", appErrorLog.stackTrace());
    }
}
