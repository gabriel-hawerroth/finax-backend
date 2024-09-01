package br.finax.utils;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ServiceUrls {

    private final String API_URL;

    private final String SITE_URL;

    ServiceUrls(Environment environment) {
        if (environment.getActiveProfiles().length > 0 && Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
            API_URL = "http://localhost:8080";
            SITE_URL = "http://localhost:4200";
            return;
        }

        API_URL = "https://api.appfinax.com.br";
        SITE_URL = "https://appfinax.com.br";
    }

    public String getApiUrl() {
        return API_URL;
    }

    public String getSiteUrl() {
        return SITE_URL;
    }
}
