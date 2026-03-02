package com.hinova.crm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClienteConfig {
    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }
}
