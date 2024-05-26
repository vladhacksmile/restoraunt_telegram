package com.vladhacksmile.crm.gpt;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ChatAIRestTemplateConfig {

    @Value("${ai.api.key}")
    private String aiApiKey;

    @Bean
    @Qualifier("aiRestTemplate")
    public RestTemplate AIRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("Authorization", "Bearer " + aiApiKey);
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}