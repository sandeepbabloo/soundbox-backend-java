package com.paymentalert.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "phonepe")
@Data
public class PhonePeConfig {
    
    private Merchant merchant = new Merchant();
    private String environment;
    private String baseUrl;
    private String saltKey;
    private String redirectUrl;
    private String webhookUrl;
    
    @Data
    public static class Merchant {
        private String id;
        private String name;
    }
}

