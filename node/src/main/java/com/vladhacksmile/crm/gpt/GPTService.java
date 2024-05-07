package com.vladhacksmile.crm.gpt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GPTService {

    @Qualifier("openaiRestTemplate")
    @Autowired
    private RestTemplate restTemplate;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiUrl;

    public String request(String prompt) {
        ChatRequest request = new ChatRequest(model, prompt);
        request.setN(1);

        ChatResponse response = restTemplate.postForObject(apiUrl, request, ChatResponse.class);

        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            return "No response";
        }

        return response.getChoices().get(0).getMessage().getContent();
//        return "[{\"productId\": 1, \"count\": 1},{\"productId\": 3, \"count\": 1}]";
    }
}

// sk-proj-SphcojtbSp6Eflb1GcYVT3BlbkFJE82RcIu52K4LF9T25Xf0