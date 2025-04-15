package com.vladhacksmile.crm.gpt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

@Service
public class GPTService {

    @Qualifier("aiRestTemplate")
    @Autowired
    private RestTemplate restTemplate;

    @Value("${ai.model}")
    private String model;

    @Value("${ai.api.url}")
    private String apiUrl;

    public String request(String prompt) {
        ChatRequest request = new ChatRequest(model, prompt);
        request.setN(1);

        ChatResponse response = restTemplate.postForObject(apiUrl, request, ChatResponse.class);

        if (response == null || CollectionUtils.isEmpty(response.getChoices())) {
            return "No response";
        }

        return response.getChoices().get(0).getMessage().getContent();
    }
}