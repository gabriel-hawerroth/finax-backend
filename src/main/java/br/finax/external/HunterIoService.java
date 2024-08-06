package br.finax.external;

import br.finax.dto.HunterResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class HunterIoService {

    private final String apiKey;

    public HunterIoService(@Value("${hunter.api.key}") String apiKey) {
        this.apiKey = apiKey;
    }

    public ResponseEntity<HunterResponse> sendEmailVerifier(String email) {
        return RestClient.create().get()
                .uri("https://api.hunter.io/v2/email-verifier?email=" + email + "&api_key=" + apiKey)
                .retrieve()
                .toEntity(HunterResponse.class);
    }
}
