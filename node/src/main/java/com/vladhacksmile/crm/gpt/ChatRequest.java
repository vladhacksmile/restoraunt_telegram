package com.vladhacksmile.crm.gpt;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class ChatRequest {

    private String model;

    private List<GPTMessage> messages;

    private int n;

    private double temperature;

    public ChatRequest(String model, String prompt) {
        this.model = model;
        this.messages = Collections.singletonList(new GPTMessage("user", prompt));
    }
}