package br.finax.services;

import br.finax.models.AccessLog;
import br.finax.repository.AccessLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccessLogService {

    private final AccessLogRepository accessLogRepository;

    @Transactional
    public void save(AccessLog accessLog) {
        accessLog.setId(null);
        accessLogRepository.save(accessLog);
    }
}
