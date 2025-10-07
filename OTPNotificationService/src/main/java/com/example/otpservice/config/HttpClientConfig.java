package com.example.otpservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpClientConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public String userBaseUrl(@Value("${services.user.base-url}") String baseUrl) {
        return baseUrl;
    }

    @Bean
    public String accountBaseUrl(@Value("${services.account.base-url}") String baseUrl) {
        return baseUrl;
    }

    @Bean
    public String paymentBaseUrl(@Value("${services.payment.base-url}") String baseUrl) {
        return baseUrl;
    }
}



