package br.finax.utils;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ServiceUrls {

    private final String siteUrl;

    private final String apiUrl;

    ServiceUrls(@Value("${finax.urls.website}") String websiteUrl, @Value("${finax.urls.api}") String apiUrl) {
        siteUrl = websiteUrl;
        this.apiUrl = apiUrl;
    }
}
