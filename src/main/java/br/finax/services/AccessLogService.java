package br.finax.services;

import br.finax.models.AccessLog;
import br.finax.repository.AccessLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessLogService {

    private final AccessLogRepository accessLogRepository;


    public void save(AccessLog accessLog) {
        accessLogRepository.save(accessLog);
    }
}
