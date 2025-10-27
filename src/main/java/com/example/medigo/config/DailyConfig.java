package com.example.medigo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DailyConfig {

    @Value("${daily.api.key}")
    private String apiKey;

    @Value("${daily.domain}")
    private String dailyDomain;

    @Value("${daily.api.url:https://api.daily.co/v1}")
    private String apiUrl;

    public String getApiKey() {
        return apiKey;
    }

    public String getDailyDomain() {
        return dailyDomain;
    }

    public String getApiUrl() {
        return apiUrl;
    }
}
