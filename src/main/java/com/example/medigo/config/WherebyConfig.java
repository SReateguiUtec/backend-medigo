package com.example.medigo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WherebyConfig {

    @Value("${whereby.api.key}")
    private String apiKey;

    @Value("${whereby.api.url:https://api.whereby.dev/v1}")
    private String apiUrl;

    public String getApiKey() {
        return apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }
}