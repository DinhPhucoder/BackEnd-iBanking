package com.example.paymentservice.config;

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

    // Expose base URLs làm bean để các client inject
    @Bean
    public String accountBaseUrl(@Value("${services.account.base-url}") String baseUrl) {
        return baseUrl;
    }

    @Bean
    public String tuitionBaseUrl(@Value("${services.tuition.base-url}") String baseUrl) {
        return baseUrl;
    }

    @Bean
    public String otpBaseUrl(@Value("${services.otp.base-url}") String baseUrl) {
        return baseUrl;
    }
}